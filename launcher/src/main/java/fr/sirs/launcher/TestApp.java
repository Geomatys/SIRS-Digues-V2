/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.launcher;

import fr.sirs.util.FXDirectoryTextField;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class TestApp extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception {
            BorderPane borderPane = new BorderPane(new FXDirectoryTextField());
            final Stage stage = new Stage();
            stage.setScene(new Scene(borderPane));
            stage.setWidth(400);
            stage.setHeight(600);
            stage.setTitle("test stage");
            stage.setOnCloseRequest((WindowEvent event) -> {
                System.exit(0);
            });
            stage.show();
        }

        public static void main(final String[] args) {
            launch(args);
        }
    
}
