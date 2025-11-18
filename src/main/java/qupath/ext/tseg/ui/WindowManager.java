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

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.tseg.setup.ui.SetupInterface;
import qupath.fx.dialogs.Dialogs;

import java.io.IOException;
import java.util.ResourceBundle;

public class WindowManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindowManager.class);
    private static final ResourceBundle STRING_BUNDLE = ResourceBundle.getBundle(
            "qupath.ext.tseg.ui.strings"
    );

    private Stage segmentStage;
    private Stage setupStage;

    /**
     * Shows the main extension window.
     */
    public void showMainWindow() {
        if (segmentStage == null) {
            try {
                segmentStage = buildMainStage();
            } catch (IOException e) {
                Dialogs.showErrorMessage("Extension Error", "GUI loading failed");
                LOGGER.error("Unable to load extension interface FXML", e);
                return;
            }
        }
        segmentStage.show();
    }

    /**
     * Shows the setup window and waits for it to be closed.
     */
    public void showSetupWindow() {
        if (setupStage == null) {
            try {
                setupStage = buildSetupStage();
            } catch (IOException e) {
                Dialogs.showErrorMessage("Extension Error", "Setup GUI loading failed");
                LOGGER.error("Unable to load setup interface FXML", e);
                return;
            }
        }
        setupStage.showAndWait();
    }

    /**
     * Builds and configures the setup window stage.
     */
    private Stage buildSetupStage() throws IOException {
        Stage stage = new Stage();
        stage.setTitle(STRING_BUNDLE.getString("window.title.setup"));
        stage.initModality(Modality.APPLICATION_MODAL);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("setup-interface.fxml"), STRING_BUNDLE);
        loader.setClassLoader(this.getClass().getClassLoader());
        VBox root = loader.load();

        SetupInterface controller = loader.getController();
        controller.setStage(stage);

        Scene scene = new Scene(root);
        var stylesheetUrl = ExtensionInterface.class.getResource("interface-style.css");

        if (stylesheetUrl != null)
            scene.getStylesheets().add(stylesheetUrl.toExternalForm());

        stage.setScene(scene);
        stage.setResizable(false);

        Image icon = loadMainIcon();
        if (icon != null) {
            stage.getIcons().add(icon);
        }

        return stage;
    }

    /**
     * Builds and configures the main extension window stage.
     */
    private Stage buildMainStage() throws IOException {
        Stage stage = new Stage();
        stage.setTitle(STRING_BUNDLE.getString("window.title"));
        Scene scene = new Scene(ExtensionInterface.createInstance());
        var stylesheetUrl = ExtensionInterface.class.getResource("interface-style.css");

        if (stylesheetUrl != null)
            scene.getStylesheets().add(stylesheetUrl.toExternalForm());

        stage.setScene(scene);
        stage.setResizable(false);

        Image icon = loadMainIcon();
        if (icon != null) {
            stage.getIcons().add(icon);
        }

        return stage;
    }

    /**
     * Loads the main application icon.
     */
    private Image loadMainIcon() {
        var iconUrl = WindowManager.class.getResource("images/icon.png");
        if (iconUrl == null) {
            LOGGER.warn("Icon not found");
            return null;
        }
        return new Image(iconUrl.toExternalForm());
    }
}
