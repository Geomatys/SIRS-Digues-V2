
package fr.sirs.launcher;

import fr.sirs.core.SirsCore;
import java.io.PrintStream;
import java.util.UUID;
import java.util.logging.Level;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Redirect uncaught exceptions to error file, and give them an ID.
        System.setOut(new PrintStream(SirsCore.LOGS_PATH.toFile()));
        System.setOut(new PrintStream(SirsCore.ERR_LOGS_PATH.toFile()));
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            final String errorCode = UUID.randomUUID().toString();
            SirsCore.LOGGER.log(Level.SEVERE, errorCode, e);
            new Alert(Alert.AlertType.ERROR, "Une erreur inattendue est survenue. Code d'erreur : "+errorCode, ButtonType.CLOSE).showAndWait();
        });
        
        final FXLauncherPane pane = new FXLauncherPane();
        final Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("SIRS");
        primaryStage.setOnCloseRequest((WindowEvent event) -> {System.exit(0);});
        primaryStage.show();
    }
}
