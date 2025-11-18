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

package qupath.ext.tseg.setup.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.tseg.setup.SetupManager;

import java.util.function.Consumer;

public class SetupInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetupInterface.class);

    @FXML
    private TextArea setupLogArea;

    @FXML
    private Button closeButton;

    private Stage stage;

    /**
     * Initializes the controller after its root element has been completely processed.
     */
    @FXML
    private void initialize() {
        runSetup();
    }

    /**
     * Sets the stage for this interface.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Runs the setup process in a background thread.
     */
    private void runSetup() {
        LOGGER.info("TSEG setup started.");
        closeButton.setDisable(true);

        Task<Boolean> setupTask = new Task<>() {
            @Override
            protected Boolean call() {
                Consumer<String> setupSink = msg -> Platform.runLater(() -> setupLogArea.appendText(msg + "\n"));
                return SetupManager.setupInferenceDir(setupSink);
            }
        };

        setupTask.setOnSucceeded(e -> {
            if (setupTask.getValue()) {
                setupLogArea.appendText("Setup complete. You may now close this window.\n");
                LOGGER.info("Extension setup successful.");
            } else {
                setupLogArea.appendText("Extension setup failed.\n");
                LOGGER.error("Extension setup failed.");
            }
            closeButton.setDisable(false);
        });

        setupTask.setOnFailed(e -> {
            setupLogArea.appendText("An error occurred during setup. See main log.\n");
            LOGGER.error("Exception during setup", setupTask.getException());
            closeButton.setDisable(false);
        });

        Thread t = new Thread(setupTask, "tseg-setup");
        t.setDaemon(true);
        t.start();
    }


    /**
     * Closes the setup window.
     */
    @FXML
    private void close() {
        if (stage != null) {
            stage.close();
        }
    }
}
