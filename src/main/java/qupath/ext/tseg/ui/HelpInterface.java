package qupath.ext.tseg.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class HelpInterface {

    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.tseg.ui.strings");

    public static void showHelp() {
        Stage helpStage = new Stage();
        VBox helpvBox = new VBox();
        helpvBox.setPrefWidth(300);
        helpvBox.setPrefHeight(300);
        helpvBox.setPadding(new Insets(10));

        String helpStr = resources.getString("help_instructions");
        Text helpText = new Text(helpStr);
        helpText.setFill(Color.WHITE);
        helpText.setWrappingWidth(300);
        helpvBox.getChildren().add(helpText);

        helpStage.setScene(new Scene(helpvBox));
        helpStage.setResizable(false);
        helpStage.setTitle("How to use?");
        helpStage.show();
    }

}