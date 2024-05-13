package qupath.ext.tseg.ui;

import qupath.ext.tseg.YoloExtension;
import qupath.lib.images.ImageData;
import qupath.lib.images.writers.TileExporter;
import qupath.lib.io.PathIO;
import qupath.lib.objects.PathObject;
import qupath.lib.regions.ImageRegion;
import qupath.lib.roi.interfaces.ROI;
import qupath.lib.scripting.QP;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

// Class for managing I/O within QuPath.
public class QPImage {

    private final ImageData<BufferedImage> imageData;
    private final ROI ROI;
    private final PathObject ROIObject;
    private final double downsample;
    private final String imageExtension;
    private final int tileSize;
    private final double overlapPercentage;

    public QPImage() {
        this.imageData = QP.getCurrentImageData();
        this.ROI = QP.getSelectedROI();
        this.ROIObject = QP.getSelectedObject();
        this.downsample = YoloExtension.downsampleProperty.getValue(); // should match with model
        this.imageExtension = "." + YoloExtension.imageExtProperty.getValue();
        this.tileSize = YoloExtension.tileSizeProperty.getValue(); // should match with model
        this.overlapPercentage = YoloExtension.overlapProperty.getValue();
    }

    // Save selected ROI as tiles at given path.
    public void saveROI(String roiPath) throws IOException {
        ImageRegion requestROI = ImageRegion.createInstance(ROI);
        TileExporter tileExporter = new TileExporter(imageData);
        tileExporter.downsample(downsample)
                .imageExtension(imageExtension)
                .tileSize(tileSize)
                .overlap((int) (tileSize * overlapPercentage))
                .region(requestROI)
                .includePartialTiles(true)
                .writeTiles(roiPath);
    }

    // Read and import predictions geoJson file from given path.
    public void importGeojson(File geojson) throws IOException {
        InputStream geojsonStream = new FileInputStream(geojson);
        List<PathObject> annotations = PathIO.readObjectsFromGeoJSON(geojsonStream);
        for (PathObject pathObject : annotations) {
            pathObject.setLocked(true);
        }
        QP.addObjects(annotations);
        ROIObject.addChildObjects(annotations);
        ROIObject.setLocked(true);
    }

    public ROI getROI() {return ROI;}

    public double getDownsample() {return downsample;}

    public int getTileSize() {return tileSize;}

}
