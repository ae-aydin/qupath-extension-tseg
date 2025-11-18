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

package qupath.ext.tseg.config;

import qupath.ext.tseg.util.Utils;

/**
 * Configuration for scripts.
 */
public record SetupConfig(
        String mainInferenceDirName,
        String repoArchiveURL,
        String repoName,
        String modelUrls,
        String modelNames
) {
    public static final SetupConfig DEFAULT = loadFromProperties();

    /**
     * Loads configuration from properties file.
     */
    private static SetupConfig loadFromProperties() {
        var properties = Utils.loadConfig("qupath/ext/tseg/setup_config.properties");
        return new SetupConfig(
                properties.getProperty("inference.mainInferenceDirName"),
                properties.getProperty("inference.repoArchiveURL"),
                properties.getProperty("inference.repoName"),
                properties.getProperty("inference.model.urls"),
                properties.getProperty("inference.model.names")
        );
    }
}