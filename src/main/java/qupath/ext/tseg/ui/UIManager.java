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

import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import qupath.ext.tseg.config.PreferenceManager;
import qupath.ext.tseg.inference.InferenceDirectory;
import qupath.ext.tseg.util.Utils;
import qupath.fx.dialogs.Dialogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class UIManager {

    private static final ResourceBundle STRING_BUNDLE = ResourceBundle.getBundle(
            "qupath.ext.tseg.ui.strings"
    );

    private static final double PAUSE_AFTER_RUN = 1.0;

    /**
     * Sets MPP spinner using preferred (model) MPP value.
     */
    public static void setDefaultTargetMPP(Spinner<Double> spinner) {
        if (spinner.getValueFactory() != null)
            spinner.getValueFactory().setValue(PreferenceManager.TILE_TARGET_MPP.getValue());
    }

    /**
     * Sets confidence spinner using preferred confidence value.
     */
    public static void setDefaultConfidence(Spinner<Double> spinner) {
        if (spinner.getValueFactory() != null)
            spinner.getValueFactory().setValue(PreferenceManager.CONFIDENCE.getValue());
    }

    /**
     * Gets the path of the selected model.
     */
    public static Path getModelPath(ComboBox<String> comboBox) {
        String modelName = comboBox.getSelectionModel().getSelectedItem();
        if (modelName == null) {
            return null;
        }
        return InferenceDirectory.DEFAULT.models().resolve(modelName);
    }

    /**
     * Updates the preferred model preference.
     */
    public static void updatePreferredModel(ComboBox<String> comboBox) {
        var modelPath = getModelPath(comboBox);
        if (modelPath == null) return;

        if (Utils.checkModelFormat(modelPath)) {
            PreferenceManager.DEFAULT_MODEL.setValue(modelPath.toString());
        } else {
            Dialogs.showWarningNotification("TSEG Warning", "Unsupported model format.");
        }
    }

    /**
     * Refreshes the model list in the combo box.
     */
    public static void refreshModels(ComboBox<String> comboBox) {
        var modelsDir = InferenceDirectory.DEFAULT.models();
        comboBox.getItems().clear();
        try (Stream<Path> files = Files.list(modelsDir)) {
            files.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .forEach(comboBox.getItems()::add);
        } catch (IOException e) {
            Dialogs.showWarningNotification("TSEG Error", "Failed to load models.");
        }
    }

    /**
     * Creates a file chooser for selecting models.
     */
    public static FileChooser createModelChooser() {
        FileChooser modelChooser = new FileChooser();
        modelChooser.setTitle("Add a Model");
        modelChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("ONNX models (*.onnx)", "*.onnx")
        );
        return modelChooser;
    }

    /**
     * Resets the run button and hides the status indicator after a short delay.
     */
    public static void resetStatusAfterDelay(Button runButton, ProgressIndicator statusIndicator) {
        PauseTransition pause = new PauseTransition(Duration.seconds(PAUSE_AFTER_RUN));
        pause.setOnFinished(e -> {
            runButton.setDisable(false);
            runButton.setText(STRING_BUNDLE.getString("button.segment"));
            statusIndicator.setVisible(false);
        });
        pause.play();
    }
}
