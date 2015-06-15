package fr.sirs.ui.calendar;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * Created by cedr on 12/06/15.
 */
public class Test extends Application {
    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final GridPane grid = new GridPane();

        final Button control1 = new Button();
        control1.setPrefWidth(150);
        control1.setPrefHeight(100);
        control1.setMaxWidth(Region.USE_PREF_SIZE);
        control1.setMaxHeight(Region.USE_PREF_SIZE);
        final GridPane gridBtn = new GridPane();
        gridBtn.add(new Label("1"), 2, 0);
//        gridBtn.add(new Label(""), 0, 1);
//        gridBtn.add(new Label("EVENT 1"), 1, 1);
//        gridBtn.add(new Label("EVENT 2"), 1, 2);
        gridBtn.getColumnConstraints().add(new ColumnConstraints(Region.USE_PREF_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_PREF_SIZE,
                Priority.NEVER, HPos.RIGHT, true));
        gridBtn.getColumnConstraints().add(new ColumnConstraints(Region.USE_PREF_SIZE, Region.USE_COMPUTED_SIZE, Double.MAX_VALUE,
                Priority.ALWAYS, HPos.LEFT, true));
        gridBtn.getColumnConstraints().add(new ColumnConstraints(Region.USE_PREF_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_PREF_SIZE,
                Priority.NEVER, HPos.RIGHT, true));
        control1.setGraphic(gridBtn);
        control1.setBackground(Background.EMPTY);
        control1.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
        grid.add(control1, 0, 0);

        final Button control2 = new Button();
        control2.setPrefWidth(150);
        control2.setPrefHeight(100);
        control2.setMaxWidth(Region.USE_PREF_SIZE);
        control2.setMaxHeight(Region.USE_PREF_SIZE);
        control2.setText("2");
        control2.setBackground(Background.EMPTY);
        control2.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
        grid.add(control2, 0, 1);

        final Button control3 = new Button();
        control3.setPrefWidth(150);
        control3.setPrefHeight(100);
        control3.setMaxWidth(Region.USE_PREF_SIZE);
        control3.setMaxHeight(Region.USE_PREF_SIZE);
        control3.setText("3");
        control3.setBackground(Background.EMPTY);
        control3.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
        grid.add(control3, 1, 0);

        final Button control4 = new Button();
        control4.setPrefWidth(150);
        control4.setPrefHeight(100);
        control4.setMaxWidth(Region.USE_PREF_SIZE);
        control4.setMaxHeight(Region.USE_PREF_SIZE);
        control4.setText("4");
        control4.setBackground(Background.EMPTY);
        control4.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
        grid.add(control4, 1, 1);

        primaryStage.setScene(new Scene(new BorderPane(grid), 500, 400));
        primaryStage.show();
    }
}
