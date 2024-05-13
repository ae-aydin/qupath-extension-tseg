package qupath.ext.tseg.ui;

import javafx.concurrent.Task;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.ResourceBundle;

// Class for managing Python script operations.
public class ScriptManager extends Task<Void> {

    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.tseg.ui.strings");
    private final PathConfig pathConfig;
    private final QPImage QPImage;
    private final double confidence;
    private final double iou;
    private final TextArea outputTextArea;

    public ScriptManager(PathConfig pathConfig, QPImage QPImage, double confidence, double iou, TextArea outputTextArea) {
        this.pathConfig = pathConfig;
        this.QPImage = QPImage;
        this.confidence = confidence;
        this.iou = iou;
        this.outputTextArea = outputTextArea;
    }

    // Run Python script with given arguments. Read back script's output.
    @Override
    protected Void call() throws Exception {
        // Python script arguments.
        String roiXBoundStr = String.valueOf(QPImage.getROI().getBoundsX());
        String roiYBoundStr = String.valueOf(QPImage.getROI().getBoundsY());
        String roiWidthStr = String.valueOf(QPImage.getROI().getBoundsWidth());
        String roiHeightStr = String.valueOf(QPImage.getROI().getBoundsHeight());
        String tileSizeStr = String.valueOf(QPImage.getTileSize());
        String downsampleStr = String.valueOf(QPImage.getDownsample());
        String confidenceStr = String.valueOf(confidence);
        String iouStr = String.valueOf(iou);

        // Save ROI, initialize output area and timers.
        appendToOutputTextArea(resources.getString("run.tiles") + "\n");
        long startTime = System.currentTimeMillis();
        QPImage.saveROI(pathConfig.getRoiPath());
        double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
        String elapsedStr = String.format(resources.getString("run.tiles_done") + "\n", elapsed);
        appendToOutputTextArea(elapsedStr);

        appendToOutputTextArea(resources.getString("run.script_run") + "\n");

        // Run Python script using ProcessBuilder.
        ProcessBuilder processBuilder = new ProcessBuilder(pathConfig.getPythonPath(),
                pathConfig.getScriptPath(),
                pathConfig.getBasePath(),
                roiXBoundStr,
                roiYBoundStr,
                roiWidthStr,
                roiHeightStr,
                tileSizeStr,
                downsampleStr,
                confidenceStr,
                iouStr);

        // Direct script output to Java, show it on a text field.
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                appendToOutputTextArea("  " + line + "\n");
            }
        } catch (IOException e) {
            appendToOutputTextArea("  " + e.getMessage() + "\n");
        }
        process.waitFor();

        // Read predictions, and clean temporary files.
        appendToOutputTextArea(resources.getString("run.import") + "\n");
        boolean cleanedJSON = readGeojson();
        boolean cleanedROI = cleanDirectory(new File(pathConfig.getRoiPath()));

        if (!(cleanedJSON && cleanedROI))
            appendToOutputTextArea(resources.getString("run.warning") + "\n");

        return null;
    }

    // Logging through TextField.
    private void appendToOutputTextArea(String text) {
        outputTextArea.appendText(text);
        outputTextArea.positionCaret(outputTextArea.getText().length());
    }

    // Import model predictions as geoJson. Clean it afterward.
    private boolean readGeojson() throws IOException {
        File geojson = new File(pathConfig.getGeojsonPath());
        boolean geojsonDeleted = false;
        if (geojson.exists() && geojson.isFile()) {
            QPImage.importGeojson(geojson);
            geojsonDeleted = geojson.delete();
        }
        return geojsonDeleted;
    }

    // Clean given directory.
    private boolean cleanDirectory(File dir) {
        boolean cleaned = false;
        for (File file: Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory())
                cleanDirectory(file);
            cleaned = file.delete();
        }
        return cleaned;
    }

}
