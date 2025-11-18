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
import qupath.ext.tseg.inference.InferenceDirectory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

/**
 * Handles downloading of models.
 */
public final class ModelDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelDownloader.class);

    /**
     * Downloads a single model from the given URL.
     */
    public static boolean downloadModel(String modelUrl, String modelName) {
        try {
            var modelsDir = InferenceDirectory.DEFAULT.models();
            var targetModelPath = modelsDir.resolve(modelName);

            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(modelUrl))
                    .GET()
                    .build();

            HttpResponse<Path> response = client.send(
                    request, HttpResponse.BodyHandlers.ofFile(targetModelPath)
            );

            if (response.statusCode() == 200) {
                return true;
            } else {
                LOGGER.error("Failed to download model: HTTP {}", response.statusCode());
                return false;
            }

        } catch (Exception e) {
            LOGGER.error("Error downloading model {}: {}", modelName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Downloads multiple models from the given URLs.
     */
    public static boolean downloadModels(String[] modelUrls, String[] modelNames) {
        if (modelUrls.length != modelNames.length) {
            LOGGER.error("Model URLs and names arrays must have the same length.");
            return false;
        }

        boolean allSuccess = true;
        for (int i = 0; i < modelUrls.length; i++) {
            if (!downloadModel(modelUrls[i], modelNames[i])) {
                allSuccess = false;
            }
        }

        return allSuccess;
    }
}