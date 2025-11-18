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

package qupath.ext.tseg;

import javafx.scene.control.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.tseg.config.PreferenceManager;
import qupath.ext.tseg.ui.WindowManager;
import qupath.ext.tseg.util.Utils;
import qupath.lib.common.Version;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.GitHubProject;
import qupath.lib.gui.extensions.QuPathExtension;

import java.util.Properties;

public class TSEGExtension implements QuPathExtension, GitHubProject {

    private static final Logger LOGGER = LoggerFactory.getLogger(TSEGExtension.class);
    private static final Properties METADATA = Utils.loadConfig("qupath/ext/tseg/metadata.properties");

    private static final String EXTENSION_NAME = METADATA.getProperty("extension.name");
    private static final GitHubRepo EXTENSION_REPOSITORY = GitHubRepo.create(
            EXTENSION_NAME,
            METADATA.getProperty("extension.owner"),
            METADATA.getProperty("extension.repo")
    );
    private static final String EXTENSION_DESCRIPTION = METADATA.getProperty("extension.description");
    private static final Version EXTENSION_QUPATH_VERSION = Version.parse(
            METADATA.getProperty("extension.qupath.version")
    );

    private final WindowManager windowManager = new WindowManager();
    private boolean isInstalled = false;

    @Override
    public void installExtension(QuPathGUI qupath) {
        if (isInstalled) {
            LOGGER.debug("{} is already installed", getName());
            return;
        }
        isInstalled = true;
        addMenuItem(qupath);
        PreferenceManager.addPreferencesToPane(qupath, EXTENSION_NAME);
    }

    private void addMenuItem(QuPathGUI qupath) {
        var extensionsMenu = qupath.getMenu("Extensions", true);
        MenuItem tsegItem = new MenuItem(EXTENSION_NAME);
        tsegItem.setOnAction(e -> windowManager.showMainWindow());
        extensionsMenu.getItems().add(tsegItem);
    }

    @Override
    public String getName() {
        return EXTENSION_NAME;
    }

    @Override
    public String getDescription() {
        return EXTENSION_DESCRIPTION;
    }

    @Override
    public Version getQuPathVersion() {
        return EXTENSION_QUPATH_VERSION;
    }

    @Override
    public GitHubRepo getRepository() {
        return EXTENSION_REPOSITORY;
    }

}
