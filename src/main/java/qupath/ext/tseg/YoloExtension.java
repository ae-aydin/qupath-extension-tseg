package qupath.ext.tseg;

import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.tseg.ui.ExtensionInterface;
import qupath.ext.tseg.ui.HelpInterface;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.common.Version;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.GitHubProject;
import qupath.lib.gui.extensions.QuPathExtension;
import java.io.IOException;

public class YoloExtension implements QuPathExtension, GitHubProject {
	
	private static final Logger logger = LoggerFactory.getLogger(YoloExtension.class);

	private static final String EXTENSION_NAME = "YOLO Tumor Segmentation";

	private static final String EXTENSION_DESCRIPTION = "Tumor Segmentation with YOLO-seg Models - HU AIN Final Project";

	private static final Version EXTENSION_QUPATH_VERSION = Version.parse("v0.5.0");

	private static final GitHubRepo EXTENSION_REPOSITORY = GitHubRepo.create(
			EXTENSION_NAME, "ae-aydin", "qupath-extension-tseg");

	private boolean isInstalled = false;

	private Stage segmentStage;

    @Override
	public void installExtension(QuPathGUI qupath) {
		if (isInstalled) {
			logger.debug("{} is already installed", getName());
			return;
		}
		isInstalled = true;
		addMenuItem(qupath);
	}

	private void addMenuItem(QuPathGUI qupath) {
		var menu = qupath.getMenu("Extensions>" + "TSEG", true);
		MenuItem helpItem = new MenuItem("Help");
		MenuItem segmentItem = new MenuItem("Segment");
		helpItem.setOnAction(e -> HelpInterface.showHelp());
		segmentItem.setOnAction(e -> createSegmentationWindow(qupath));
		menu.getItems().add(helpItem);
		menu.getItems().add(segmentItem);
	}

	private void createSegmentationWindow(QuPathGUI qupath) {
		if (segmentStage == null) {
			try {
				segmentStage = new Stage();
				Scene segmentScene = new Scene(ExtensionInterface.createInstance(qupath));
				segmentStage.setScene(segmentScene);
				segmentStage.setResizable(false);
				segmentStage.setTitle("YOLO Tumor Segmentation");
			} catch (IOException e) {
				Dialogs.showErrorMessage("Extension Error", "GUI loading failed");
				logger.error("Unable to load extension interface FXML", e);
			}
		}
		segmentStage.show();
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
