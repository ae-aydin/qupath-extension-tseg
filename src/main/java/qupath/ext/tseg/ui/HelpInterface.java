package qupath.ext.tseg.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.ResourceBundle;

// Class for creating help page for extension.
public class HelpInterface {

    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.tseg.ui.strings");

    public static void showHelp() {
        Stage helpStage = new Stage();
        Image helpIcon = new Image(Objects.requireNonNull(HelpInterface.class.getResourceAsStream("images/help_icon.png")));
        helpStage.getIcons().add(helpIcon);
        VBox helpvBox = new VBox();
        helpvBox.setPrefWidth(580);
        helpvBox.setPrefHeight(300);
        helpvBox.setPadding(new Insets(10));

        String helpStr = resources.getString("help.instructions");
        TextArea helpText = new TextArea(helpStr);
        helpText.setEditable(false);
        helpText.setWrapText(true);
        helpText.prefWidthProperty().bind(helpvBox.widthProperty());
        helpText.prefHeightProperty().bind(helpvBox.heightProperty());
        helpvBox.getChildren().add(helpText);

        helpStage.setScene(new Scene(helpvBox));
        helpStage.setResizable(false);
        helpStage.setTitle("How to install and use?");
        helpStage.show();
    }

}