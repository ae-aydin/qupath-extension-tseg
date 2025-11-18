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
import qupath.ext.tseg.inference.InferenceDirectory;
import qupath.lib.gui.prefs.PathPrefs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * Manages the setup of the inference environment.
 */
public class SetupManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetupManager.class);
    private static final String SUCCESS_FLAG_FILENAME = ".setup_successful";
    private static final String SETUP_SCRIPT_DIR = "qupath";
    private static final String WINDOWS_SCRIPT = "setup.bat";
    private static final String UNIX_SCRIPT = "setup.sh";
    private static final String REPO_SETUP_LOG = "setup.log";

    /**
     * Checks if the current operating system is Windows.
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Checks if the setup has been completed.
     */
    public static boolean hasCompletedSetup() {
        String userPathStr = PathPrefs.userPathProperty().getValue();
        var mainInferenceDir = SetupConfig.DEFAULT.mainInferenceDirName();
        if (userPathStr == null || userPathStr.isEmpty() || mainInferenceDir == null) {
            return false;
        }
        Path mainWorkingDir = Paths.get(userPathStr).resolve(mainInferenceDir);
        Path successFlagFile = mainWorkingDir.resolve(SUCCESS_FLAG_FILENAME);
        return Files.exists(successFlagFile);
    }

    /**
     * Executes the setup script in the repository directory.
     */
    private static boolean executeRepoSetupScript(Path inferenceDir, Path repoDir, Consumer<String> messageSink) {
        Path setupScriptDirPath = repoDir.resolve(SETUP_SCRIPT_DIR);
        String scriptName = isWindows() ? WINDOWS_SCRIPT : UNIX_SCRIPT;
        Path setupScriptPath = setupScriptDirPath.resolve(scriptName);
        Path setupLogPath = inferenceDir.resolve(REPO_SETUP_LOG);

        if (!Files.exists(setupScriptPath)) {
            messageSink.accept(String.format("ERROR: Setup script not found at %s", setupScriptPath));
            return false;
        }

        messageSink.accept("Executing inference setup script...");
        try {
            ProcessBuilder pb;

            if (isWindows()) {
                pb = new ProcessBuilder("cmd.exe", "/c", scriptName, setupLogPath.toString());
            } else {
                pb = new ProcessBuilder("bash", scriptName, setupLogPath.toString());
            }

            pb.directory(setupScriptDirPath.toFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                messageSink.accept("Repo setup completed successfully.");
                return true;
            }

            messageSink.accept(String.format("ERROR: Repo setup script failed with exit code: %d", exitCode));
            return false;
        } catch (Exception e) {
            messageSink.accept("ERROR: Could not execute repo setup script.");
            return false;
        }
    }

    /**
     * Downloads the default models from configuration.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean downloadDefaultModels(Consumer<String> messageSink) {
        try {
            String modelUrlsStr = SetupConfig.DEFAULT.modelUrls();
            String modelNamesStr = SetupConfig.DEFAULT.modelNames();

            if (modelUrlsStr == null || modelNamesStr == null ||
                    modelUrlsStr.isEmpty() || modelNamesStr.isEmpty()) {
                messageSink.accept("No default models configured, skipping model download.");
                return true;
            }

            String[] modelUrls = modelUrlsStr.split(",");
            String[] modelNames = modelNamesStr.split(",");

            for (int i = 0; i < modelUrls.length; i++) {
                modelUrls[i] = modelUrls[i].trim();
                modelNames[i] = modelNames[i].trim();
                try {
                    new URI(modelUrls[i]).toURL();
                } catch (java.net.URISyntaxException | java.net.MalformedURLException e) {
                    messageSink.accept("ERROR: Invalid model URL: " + modelUrls[i]);
                    return false;
                }
            }

            if (modelUrls.length != modelNames.length) {
                messageSink.accept("ERROR: Model URLs and names count mismatch in properties file");
                return false;
            }

            messageSink.accept("Downloading " + modelUrls.length + " model(s)...");

            if (ModelDownloader.downloadModels(modelUrls, modelNames)) {
                messageSink.accept("All models downloaded successfully.");
                return true;
            } else {
                messageSink.accept("Some models failed to download.");
                return false;
            }

        } catch (Exception e) {
            messageSink.accept("ERROR: Model download failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates the success flag file.
     */
    private static boolean createSuccessFlag(Path successFlagFile) {
        try {
            Files.createFile(successFlagFile);
            return true;
        } catch (IOException e) {
            LOGGER.warn("Failed to create success flag file", e);
            return false;
        }
    }

    /**
     * Sets up the inference directory and environment.
     */
    public static boolean setupInferenceDir(Consumer<String> messageSink) {
        var mainInferenceDir = InferenceDirectory.DEFAULT.main();
        var inferenceRepoDir = InferenceDirectory.DEFAULT.repo();
        Path successFlagFile = mainInferenceDir.resolve(SUCCESS_FLAG_FILENAME);

        try {
            messageSink.accept("Creating inference environment at QuPath user directory...");
            Files.createDirectories(mainInferenceDir);
        } catch (IOException e) {
            messageSink.accept("ERROR: Failed to create inference environment.");
            return false;
        }

        messageSink.accept("Downloading and preparing inference repository...");
        if (!ArchiveDownloader.downloadAndExtract(mainInferenceDir)) {
            messageSink.accept("ERROR: Inference repository setup failed.");
            return false;
        }

        messageSink.accept("Setting up inference repo environment...");
        if (!executeRepoSetupScript(mainInferenceDir, inferenceRepoDir, messageSink)) {
            messageSink.accept("ERROR: Repo setup script execution failed. See log for details.");
            return false;
        }

        messageSink.accept("Downloading default models...");
        if (!downloadDefaultModels(messageSink)) {
            messageSink.accept("ERROR: Model download failed.");
            return false;
        }

        if (createSuccessFlag(successFlagFile)) {
            messageSink.accept("Extension setup finished successfully.");
            return true;
        } else {
            messageSink.accept("ERROR: Extension setup failed.");
            return false;
        }
    }
}
