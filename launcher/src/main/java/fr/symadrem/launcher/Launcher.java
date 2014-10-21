
package fr.symadrem.launcher;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
        final LauncherPane pane = new LauncherPane();
        final Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Symadrem");
        primaryStage.show();
    }
}
