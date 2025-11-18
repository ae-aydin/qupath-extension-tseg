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

package qupath.ext.tseg.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.tseg.config.SetupConfig;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

/**
 * Utility class for downloading and extracting repository archives.
 */
public final class ArchiveDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveDownloader.class);

    /**
     * Downloads and extracts the repository archive to the target directory.
     */
    public static boolean downloadAndExtract(Path targetDir) {
        var archiveURL = SetupConfig.DEFAULT.repoArchiveURL();
        var repoName = SetupConfig.DEFAULT.repoName();
        var repoDir = targetDir.resolve(repoName);

        if (Files.exists(repoDir)) {
            LOGGER.warn("Repository already exists at {}", repoDir);
            return true;
        }

        Path zipFile = targetDir.resolve(repoName + ".zip");

        try {
            try (BufferedInputStream in = new BufferedInputStream(URI.create(archiveURL).toURL().openStream())) {
                Files.copy(in, zipFile, StandardCopyOption.REPLACE_EXISTING);
            }

            extract(zipFile, targetDir);

            Path extractedDirectory = findExtractedDirectory(targetDir, repoName);
            if (extractedDirectory == null) {
                LOGGER.error("Could not find the extracted folder after download.");
                return false;
            }
            Files.move(extractedDirectory, repoDir, StandardCopyOption.REPLACE_EXISTING);
            return true;

        } catch (IOException e) {
            LOGGER.error("Could not download or extract archive.");
            return false;
        } finally {
            try {
                Files.deleteIfExists(zipFile);
            } catch (IOException e) {
                LOGGER.warn("Failed to delete temporary archive.");
            }
        }
    }

    /**
     * Extracts the contents of a zip file to the destination directory.
     */
    private static void extract(Path zipFile, Path destDir) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            var zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                Path newPath = destDir.resolve(zipEntry.getName());

                if (!newPath.normalize().startsWith(destDir.normalize())) {
                    throw new IOException("Zip entry is trying to escape the destination directory: " + zipEntry.getName());
                }

                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    if (newPath.getParent() != null) {
                        Files.createDirectories(newPath.getParent());
                    }
                    try (FileOutputStream fos = new FileOutputStream(newPath.toFile())) {
                        zipInputStream.transferTo(fos);
                    }
                }
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
        }
    }

    /**
     * Finds the extracted repository directory in the parent directory.
     */
    private static Path findExtractedDirectory(Path parentDir, String repoName) throws IOException {
        try (Stream<Path> stream = Files.list(parentDir)) {
            return stream
                    .filter(p -> Files.isDirectory(p) && p.getFileName().toString().startsWith(repoName + "-"))
                    .findFirst()
                    .orElse(null);
        }
    }
}