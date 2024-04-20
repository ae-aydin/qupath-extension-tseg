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
import qupath.lib.gui.QuPathGUI;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

public class ExtensionInterface extends GridPane {

    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.tseg.ui.strings");
    @FXML
    private TextField pyProjectField;
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

    public static ExtensionInterface createInstance(QuPathGUI qupath) throws IOException {
        return new ExtensionInterface(qupath);
    }

    private ExtensionInterface(QuPathGUI qupath) throws IOException {
        var url = ExtensionInterface.class.getResource("interface.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        loader.setRoot(this);
        loader.setController(this);
        loader.load();
        loadSavedDirectoryPath();
    }

    @FXML
    private void selectPyProject() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(resources.getString("dir_chooser"));
        File selectedDir = directoryChooser.showDialog(null);
        if (selectedDir != null) {
            String selectedPath = selectedDir.getAbsolutePath();
            pyProjectField.setText(selectedPath);
            ConfigManager.saveProperty("pyProjectPath", selectedPath);
        }
    }

    private void loadSavedDirectoryPath() {
        String savedPath = ConfigManager.getProperty("pyProjectPath");
        if (savedPath != null) {
            pyProjectField.setText(savedPath);
        }
    }

    @FXML
    private void runScript() {
        String pyProjectPath = pyProjectField.getText();
        PathConfig pathConfig = new PathConfig(pyProjectPath);
        double confidence = confSlider.getValue();
        double iou = iouSlider.getValue();

        QPImage QPImage = new QPImage();
        if (QPImage.getROI() == null) {
            scriptOutput.appendText(resources.getString("no_roi") + "\n");
            return;
        }

        scriptOutput.appendText(resources.getString("run") + "\n");
        ScriptManager scriptManager = new ScriptManager(pathConfig, QPImage, confidence, iou, scriptOutput);
        scriptManager.setOnSucceeded(event -> scriptOutput.appendText(resources.getString("done") + "\n"));

        scriptManager.setOnFailed(event -> {
            Throwable exception = scriptManager.getException();
            scriptOutput.appendText("ERROR:" + exception.getMessage() + "\n");
        });

        Thread thread = new Thread(scriptManager);
        thread.start();
    }

}
