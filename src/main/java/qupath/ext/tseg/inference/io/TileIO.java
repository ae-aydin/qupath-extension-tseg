/*
 * QuPath TSEG Extension for Tumor Area Segmentation
 * Copyright (C) 2025 Arif Enes Aydın
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package qupath.ext.tseg.inference.io;

import qupath.ext.tseg.config.ExportConfig;
import qupath.lib.images.ImageData;
import qupath.lib.images.writers.TileExporter;
import qupath.lib.io.PathIO;
import qupath.lib.objects.PathObject;
import qupath.lib.regions.ImageRegion;
import qupath.lib.scripting.QP;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for tile input/output operations.
 */
public final class TileIO {

    private static final boolean ALLOW_PARTIAL_TILES = true;

    /**
     * Exports image tiles according to the specification.
     */
    public static void export(
            ImageData<BufferedImage> imageData,
            ExportConfig spec, Path outputDir
    ) throws IOException {
        var region = ImageRegion.createInstance(spec.roi());

        new TileExporter(imageData)
                .downsample(spec.downsample())
                .imageExtension(spec.imageExtension())
                .tileSize(spec.tileSize())
                .overlap(spec.overlapPixels())
                .region(region)
                .includePartialTiles(ALLOW_PARTIAL_TILES)
                .writeTiles(outputDir.toString());
    }

    /**
     * Imports annotations from a GeoJSON file.
     */
    public static void importGeoJson(PathObject selectedArea, Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            var annotations = PathIO.readObjectsFromGeoJSON(in);
            annotations.forEach(a -> a.setLocked(true));
            QP.addObjects(annotations);
            selectedArea.addChildObjects(annotations);
            selectedArea.setLocked(true);
        }
    }
}
