
package fr.sirs.launcher;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        final FXLauncherPane pane = new FXLauncherPane();
        final Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("SIRS");
        primaryStage.setOnCloseRequest((WindowEvent event) -> {System.exit(0);});
        primaryStage.show();
    }
}
