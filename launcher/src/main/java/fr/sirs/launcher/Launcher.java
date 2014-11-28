
package fr.sirs.launcher;

import fr.sirs.core.SirsCore;
import java.io.PrintStream;
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
        System.setOut(new PrintStream(SirsCore.LOGS_PATH.toFile()));
        System.setOut(new PrintStream(SirsCore.ERR_LOGS_PATH.toFile()));
        final FXLauncherPane pane = new FXLauncherPane();
        final Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("SIRS");
        primaryStage.setOnCloseRequest((WindowEvent event) -> {System.exit(0);});
        primaryStage.show();
    }
}
