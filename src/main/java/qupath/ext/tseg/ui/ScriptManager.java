package qupath.ext.tseg.ui;

import javafx.concurrent.Task;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ScriptManager extends Task<Void> {

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

    @Override
    protected Void call() throws Exception {
        String xROI = QPImage.getXROIString();
        String yROI = QPImage.getYROIString();
        String ds = QPImage.getDownsampleString();
        String confStr = String.valueOf(confidence);
        String iouStr = String.valueOf(iou);
        QPImage.saveROI(pathConfig.getRoiPath());

        ProcessBuilder processBuilder = new ProcessBuilder(pathConfig.getPythonPath(),
                pathConfig.getScriptPath(), pathConfig.getBasePath(), xROI, yROI, ds, confStr, iouStr);

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                appendToOutputTextArea(line + "\n");
            }
        } catch (IOException e) {
            appendToOutputTextArea(e.getMessage() + "\n");
        }
        process.waitFor();
        readGeojson();
        return null;
    }

    private void appendToOutputTextArea(String text) {
        outputTextArea.appendText(text);
        outputTextArea.positionCaret(outputTextArea.getText().length());
    }

    private void readGeojson() throws IOException {
        File geojson = new File(pathConfig.getGeojsonPath());
        if (geojson.exists() && geojson.isFile()) {
            QPImage.importGeojson(geojson);
        }
        geojson.delete();
    }

}
