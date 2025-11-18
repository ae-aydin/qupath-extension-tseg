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

package qupath.ext.tseg.inference;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.tseg.config.ExportConfig;
import qupath.ext.tseg.config.PreferenceManager;
import qupath.ext.tseg.inference.io.TileIO;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.images.ImageData;
import qupath.lib.roi.interfaces.ROI;
import qupath.lib.scripting.QP;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages inference scripts and execution.
 */
public class InferenceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(InferenceManager.class);
    private static final String INFERENCE_LOG = "infer.log";
    private static final String INFERENCE_SCRIPT = "infer.py";
    private static final String POLYGONS_FILE = "polygons.geojson";
    private static final String UV_COMMAND = "uv";
    private static final String UV_RUN = "run";

    /**
     * Runs inference on the selected area with the given model.
     */
    public static Path runInference(
            Path modelPath,
            double targetMPP,
            double confidence
    ) throws IOException, InterruptedException {
        ImageData<BufferedImage> imageData = QP.getCurrentImageData();
        if (imageData == null) throw new IllegalStateException("No image loaded");

        var roi = QP.getSelectedROI();
        if (roi == null) throw new IllegalStateException("No ROI selected");

        var spec = exportSelectedAreaAsTiles(imageData, roi, targetMPP);

        int roiX = (int) Math.round(roi.getBoundsX());
        int roiY = (int) Math.round(roi.getBoundsY());
        int roiW = (int) Math.round(roi.getBoundsWidth());
        int roiH = (int) Math.round(roi.getBoundsHeight());

        var script = InferenceDirectory.DEFAULT.repo().resolve(INFERENCE_SCRIPT);
        if (!Files.isRegularFile(script))
            throw new IOException(INFERENCE_SCRIPT + " not found at " + script);

        return runInferenceScript(script, modelPath, spec, confidence, roiX, roiY, roiW, roiH);
    }

    /**
     * Executes the inference script with the given parameters.
     */
    private static Path runInferenceScript(
            Path script,
            Path modelPath,
            ExportConfig spec,
            double confidence,
            int roiX, int roiY, int roiW, int roiH
    ) throws IOException, InterruptedException {

        var repoDir = InferenceDirectory.DEFAULT.repo();
        var tileDir = InferenceDirectory.DEFAULT.roi();
        var outputDir = InferenceDirectory.DEFAULT.output();
        var inferLogPath = InferenceDirectory.DEFAULT.main().resolve(INFERENCE_LOG);

        var pb = new ProcessBuilder(
                UV_COMMAND, UV_RUN, script.toString(),
                "--model-path", modelPath.toString(),
                "--tile-dir", tileDir.toString(),
                "--output-dir", outputDir.toString(),
                "--roi-x", String.valueOf(roiX),
                "--roi-y", String.valueOf(roiY),
                "--roi-width", String.valueOf(roiW),
                "--roi-height", String.valueOf(roiH),
                "--downsample-rate", String.valueOf(spec.downsample()),
                "--tile-size", String.valueOf(spec.tileSize()),
                "--confidence", String.valueOf(confidence),
                "--log-file", inferLogPath.toString()
        );

        pb.directory(repoDir.toFile());
        LOGGER.info("Starting inference.");
        var proc = pb.start();
        int exit = proc.waitFor();

        String jsonOutput = new String(proc.getInputStream().readAllBytes());
        String errorOutput = new String(proc.getErrorStream().readAllBytes());

        if (!errorOutput.isBlank()) {
            LOGGER.warn("Inference Script Error: {}", errorOutput);
        }

        // Failure Case
        if (exit != 0) {
            LOGGER.error("Inference script failed with exit code {}", exit);
            String errorMessage = "Inference run failed. Check log file: " + inferLogPath;

            if (!jsonOutput.isBlank()) {
                try {
                    JsonObject json = JsonParser.parseString(jsonOutput).getAsJsonObject();
                    if (json.has("message")) {
                        errorMessage = json.get("message").getAsString();
                    }
                } catch (JsonSyntaxException | IllegalStateException e) {
                    LOGGER.warn("Could not parse error JSON.", e);
                    errorMessage = "Inference failed with non-JSON output. Check log file.";
                }
            }
            Dialogs.showErrorNotification("TSEG Error", errorMessage);
            throw new RuntimeException(errorMessage);
        }

        // Success Case
        LOGGER.info("Inference script successful.");
        String successMessage = "Inference complete.";
        try {
            JsonObject json = JsonParser.parseString(jsonOutput).getAsJsonObject();
            if (json.has("runtime_sec") & json.has("n_polygons")) {
                double runtime = json.get("runtime_sec").getAsDouble();
                int nPolygons = json.get("n_polygons").getAsInt();
                successMessage = String.format("Found %d polygon(s) in %.3fs.", nPolygons, runtime);
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            LOGGER.warn("Could not parse success JSON", e);
        }
        Dialogs.showPlainNotification("TSEG Inference", successMessage);

        var inferredPolygonsPath = outputDir.resolve(POLYGONS_FILE);
        if (Files.exists(inferredPolygonsPath)) {
            return inferredPolygonsPath;
        }
        throw new IOException("Inference finished, but output file not found: " + inferredPolygonsPath);
    }

    /**
     * Exports the selected area as tiles according to the specifications.
     */
    private static ExportConfig exportSelectedAreaAsTiles(
            ImageData<BufferedImage> imageData,
            ROI roi, double targetMPP
    ) throws IOException {
        var pixelSize = imageData.getServerMetadata().getAveragedPixelSize();

        var spec = new ExportConfig(
                roi,
                targetMPP,
                pixelSize,
                PreferenceManager.TILE_SIZE.getValue(),
                PreferenceManager.TILE_OVERLAP.getValue(),
                "." + PreferenceManager.TILE_IMAGE_FORMAT.getValue()
        );

        TileIO.export(imageData, spec, InferenceDirectory.DEFAULT.roi());
        return spec;
    }
}
