package qupath.ext.tseg.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import qupath.ext.tseg.YoloExtension;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

public class ExtensionInterface extends GridPane {

    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.tseg.ui.strings");
    @FXML
    private TextField pyScriptDirField;
    @FXML
    private Slider confSlider;
    @FXML
    private Slider iouSlider;
    @FXML
    private Text infoText;
    @FXML
    private Button runButton;
    @FXML
    private TextArea scriptOutput;

    public static ExtensionInterface createInstance() throws IOException {
        return new ExtensionInterface();
    }

    private ExtensionInterface() throws IOException {
        var url = ExtensionInterface.class.getResource("interface.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        loader.setRoot(this);
        loader.setController(this);
        loader.load();
        loadSavedDirectoryPath();
    }

    @FXML
    private void selectScriptDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(resources.getString("ext.dir_chooser"));
        File selectedDir = directoryChooser.showDialog(null);
        if (selectedDir != null) {
            String selectedPath = selectedDir.getAbsolutePath();
            pyScriptDirField.setText(selectedPath);
            YoloExtension.defaultPathProperty.set(selectedPath);
        }
    }

    private void loadSavedDirectoryPath() {
        String savedPath = YoloExtension.defaultPathProperty.getValue();
        if (savedPath != null) {
            pyScriptDirField.setText(savedPath);
        }
    }

    @FXML
    private void runScript() {
        runButton.setDisable(true);
        scriptOutput.clear();
        scriptOutput.appendText(resources.getString("run.running") + "\n");
        String pyScriptDirPath = pyScriptDirField.getText();
        PathConfig pathConfig = new PathConfig(pyScriptDirPath);
        double confidence = confSlider.getValue();
        double iou = iouSlider.getValue();

        QPImage QPImage = new QPImage();
        if (QPImage.getROI() == null) {
            scriptOutput.appendText(resources.getString("run.no_roi") + "\n");
            runButton.setDisable(false);
            return;
        }

        ScriptManager scriptManager = new ScriptManager(pathConfig, QPImage, confidence, iou, scriptOutput);
        scriptManager.setOnSucceeded(event -> {
            scriptOutput.appendText(resources.getString("run.done") + "\n");
            runButton.setDisable(false);
        });

        scriptManager.setOnFailed(event -> {
            Throwable exception = scriptManager.getException();
            scriptOutput.appendText("ERROR:" + exception.getMessage() + "\n");
            scriptOutput.appendText(resources.getString("run.fail") + "\n");
            runButton.setDisable(false);
        });

        Thread thread = new Thread(scriptManager);
        thread.start();
    }

}
