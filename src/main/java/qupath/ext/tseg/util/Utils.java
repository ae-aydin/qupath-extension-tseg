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

package qupath.ext.tseg.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Utility methods.
 */
public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    /**
     * Loads properties from a config file.
     */
    public static Properties loadConfig(String fileName) {
        var props = new Properties();
        try (var in = Utils.class.getResourceAsStream("/" + fileName)) {
            if (in != null) {
                props.load(in);
            } else {
                throw new RuntimeException("Config file not found: " + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading config: " + fileName + " - " + e.getMessage(), e);
        }
        return props;
    }

    /**
     * Checks if the model file is in ONNX format.
     */
    public static boolean checkModelFormat(Path source) {
        String modelFileName = source.getFileName().toString().toLowerCase();
        return modelFileName.endsWith(".onnx");
    }

    /**
     * Clears content of the directory.
     */
    public static void clearDir(Path dir) {
        if (dir == null || !Files.isDirectory(dir)) {
            return;
        }

        try (var entries = Files.list(dir)) {
            entries.forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    LOGGER.warn("Failed to delete {}: {}", path, e.getMessage());
                }
            });
        } catch (IOException e) {
            LOGGER.warn("Failed to list directory contents for {}: {}", dir, e.getMessage());
        }
    }
}
