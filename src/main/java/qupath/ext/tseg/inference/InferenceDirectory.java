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

package qupath.ext.tseg.inference;

import qupath.ext.tseg.config.SetupConfig;
import qupath.lib.gui.prefs.PathPrefs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents directories for inference operations.
 */
public record InferenceDirectory(Path main, Path repo, Path models, Path roi, Path output) {

    public static final InferenceDirectory DEFAULT = createDefault();

    /**
     * Creates the default InferenceDirectory.
     */
    private static InferenceDirectory createDefault() {
        try {
            return create(
                    SetupConfig.DEFAULT.mainInferenceDirName(),
                    SetupConfig.DEFAULT.repoName()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize InferenceDirectory", e);
        }
    }

    /**
     * Gets the QuPath user directory.
     */
    private static Path getQuPathUserDir() {
        String userPathStr = PathPrefs.userPathProperty().getValue();
        if (userPathStr == null || userPathStr.isBlank()) {
            throw new IllegalStateException("QuPath User Path not set");
        }
        return Paths.get(userPathStr).toAbsolutePath().normalize();
    }

    /**
     * Creates an InferenceDirectory with the given names.
     */
    public static InferenceDirectory create(String mainInferenceDirName, String repoName) throws IOException {
        Path userDir = getQuPathUserDir();
        Path main = userDir.resolve(mainInferenceDirName);
        Path repo = main.resolve(repoName);
        Path models = main.resolve("models");
        Path output = main.resolve(".output");
        Path roi = main.resolve(".roi");

        for (Path dir : new Path[]{main, models, output, roi}) {
            Files.createDirectories(dir);
        }

        return new InferenceDirectory(main, repo, models, roi, output);
    }
}
