package qupath.ext.tseg.ui;

import qupath.lib.images.servers.ImageServer;
import qupath.lib.io.PathIO;
import qupath.lib.objects.PathObject;
import qupath.lib.regions.RegionRequest;
import qupath.lib.roi.interfaces.ROI;
import qupath.lib.scripting.QP;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class QPImage {

    private final ImageServer<BufferedImage> server;
    private final ROI ROI;
    private final PathObject ROIObject;
    private final double downsample;

    public QPImage() {
        this.server = (ImageServer<BufferedImage>) QP.getCurrentServer();
        this.ROI = QP.getSelectedROI();
        this.ROIObject = QP.getSelectedObject();
        this.downsample = 3.0;
    }

    public String getXROIString() {
        return String.valueOf(ROI.getBoundsX());
    }

    public String getYROIString() {
        return String.valueOf(ROI.getBoundsY());
    }

    public void saveROI(String roiPath) throws IOException {
        RegionRequest requestROI = RegionRequest.createInstance(server.getPath(), downsample, ROI);
        QP.writeImageRegion(server, requestROI, roiPath);
    }

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

    public String getDownsampleString() {
        return String.valueOf(downsample);
    }

    public ROI getROI() {return ROI;}

}
