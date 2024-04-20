package qupath.ext.tseg.ui;

import java.io.File;

public class PathConfig {

    private final String basePath;
    private final String pythonPath;
    private final String scriptPath;
    private final String roiPath;
    private final String geojsonPath;

    public PathConfig(String basePath) {
        this.basePath = basePath;
        this.pythonPath = prepPythonPath();
        this.scriptPath = basePath + File.separator + "main.py";
        this.roiPath = basePath + File.separator + "rois" + File.separator + "roi.png";
        this.geojsonPath = basePath + File.separator + "preds" + File.separator + "roi.geojson";
    }

    private String prepPythonPath() {
        String osName = System.getProperty("os.name").toLowerCase();
        String venvPath = this.basePath + File.separator + ".venv" + File.separator;
        if (osName.contains("windows")) {
            return venvPath + "Scripts" + File.separator + "python.exe";
        } else {
            return venvPath + "bin" + File.separator + "python3";
        }
    }

    public String getBasePath() {
        return basePath;
    }

    public String getPythonPath() {
        return pythonPath;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public String getRoiPath() {
        return roiPath;
    }

    public String getGeojsonPath() {
        return geojsonPath;
    }
}
