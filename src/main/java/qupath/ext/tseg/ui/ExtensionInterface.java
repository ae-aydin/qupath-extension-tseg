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

package qupath.ext.tseg.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.tseg.inference.InferenceDirectory;
import qupath.ext.tseg.inference.InferenceManager;
import qupath.ext.tseg.inference.io.TileIO;
import qupath.ext.tseg.setup.SetupManager;
import qupath.ext.tseg.util.Utils;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.scripting.QP;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class ExtensionInterface extends VBox {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionInterface.class);
    private static final ResourceBundle STRING_BUNDLE = ResourceBundle.getBundle(
            "qupath.ext.tseg.ui.strings"
    );

    @FXML
    private Spinner<Double> confidenceSpinner;
    @FXML
    private Spinner<Double> targetMPPSpinner;
    @FXML
    private ComboBox<String> modelComboBox;
    @FXML
    private Button runButton;
    @FXML
    private ProgressIndicator statusIndicator;

    /**
     * Creates an instance of the extension interface.
     */
    public ExtensionInterface() throws IOException {
        var url = ExtensionInterface.class.getResource("interface.fxml");
        if (url == null) {
            throw new IOException("Could not find interface.fxml resource");
        }
        FXMLLoader loader = new FXMLLoader(url, STRING_BUNDLE);
        loader.setRoot(this);
        loader.setController(this);
        loader.load();
    }

    public static ExtensionInterface createInstance() throws IOException {
        try {
            return new ExtensionInterface();
        } catch (IOException e) {
            LOGGER.error("Failed to create ExtensionInterface instance", e);
            throw e;
        }
    }

    /**
     * Initializes the controller class.
     * This method is automatically called after the fxml file has been loaded
     * and all @FXML injected fields are populated.
     */
    @FXML
    private void initialize() {
        UIManager.setDefaultTargetMPP(targetMPPSpinner);
        UIManager.setDefaultConfidence(confidenceSpinner);

        if (!SetupManager.hasCompletedSetup()) {
            runButton.setDisable(true);
            new WindowManager().showSetupWindow();
            if (SetupManager.hasCompletedSetup()) {
                runButton.setDisable(false);
            } else {
                Dialogs.showWarningNotification(
                        "TSEG Setup",
                        "Extension setup was not completed successfully."
                );
            }
        }
        // Set default model
        UIManager.refreshModels(modelComboBox);
        modelComboBox.getSelectionModel().selectFirst();
        UIManager.updatePreferredModel(modelComboBox);
    }

    /**
     * Handles the action of selecting a model from the combo box.
     */
    @FXML
    public void onModelSelected() {
        UIManager.updatePreferredModel(modelComboBox);
    }

    /**
     * Runs the inference process in a background thread.
     */
    @FXML
    public void runInference() {
        var selectedArea = QP.getSelectedObject();
        var targetMPP = targetMPPSpinner.getValue();
        var confidence = confidenceSpinner.getValue();

        if (selectedArea == null) {
            Dialogs.showWarningNotification("TSEG Warning", "Please select an area.");
            return;
        }

        runButton.setDisable(true);
        runButton.setText("");
        statusIndicator.setVisible(true);

        Task<Path> task = new Task<>() {
            @Override
            protected Path call() throws Exception {
                return InferenceManager.runInference(UIManager.getModelPath(modelComboBox), targetMPP, confidence);
            }
        };

        task.setOnSucceeded(e -> {
            Path polygons = task.getValue();
            try {
                TileIO.importGeoJson(selectedArea, polygons);
                new Thread(() -> {
                    try {
                        Utils.clearDir(InferenceDirectory.DEFAULT.roi());
                        Utils.clearDir(InferenceDirectory.DEFAULT.output());
                    } catch (Exception ex) {
                        LOGGER.error("Error clearing inference directories", ex);
                    }
                }, "tseg-cleanup").start();

                LOGGER.info("Imported polygons from {}.", polygons);
                UIManager.resetStatusAfterDelay(runButton, statusIndicator);
            } catch (Exception ex) {
                LOGGER.error("Import failed.", ex);
                UIManager.resetStatusAfterDelay(runButton, statusIndicator);
            }
        });

        task.setOnFailed(e -> {
            LOGGER.error("Inference failed.", task.getException());
            Dialogs.showErrorNotification("TSEG Error", "Inference failed. Check log.");
            UIManager.resetStatusAfterDelay(runButton, statusIndicator);
        });

        Thread t = new Thread(task, "tseg-inference");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Opens a file chooser to add a new model to the "models" directory.
     */
    @FXML
    public void addModel() {
        Window owner = modelComboBox.getScene().getWindow();
        FileChooser modelChooser = UIManager.createModelChooser();
        File model = modelChooser.showOpenDialog(owner);
        if (model == null) return;

        var source = model.toPath();
        if (!Utils.checkModelFormat(source)) {
            Dialogs.showWarningNotification("TSEG Warning", "Unsupported model format.");
            return;
        }

        var target = InferenceDirectory.DEFAULT.models().resolve(source.getFileName());
        try {
            if (!source.equals(target)) {
                Files.copy(source, target);
                Dialogs.showPlainNotification("TSEG", "Model successfully added.");
            } else {
                Dialogs.showWarningNotification("TSEG Warning", "Model already in destination.");
            }

            Platform.runLater(() -> {
                UIManager.refreshModels(modelComboBox);
                if (modelComboBox != null) {
                    modelComboBox.getSelectionModel().select(target.getFileName().toString());
                }
                UIManager.updatePreferredModel(modelComboBox);
            });

        } catch (FileAlreadyExistsException e) {
            Dialogs.showWarningNotification(
                    "TSEG Warning",
                    "A model with this name already exists. Please rename your model."
            );
        } catch (IOException e) {
            Dialogs.showWarningNotification("TSEG Warning", "Add model operation failed.");
        }
    }

}
