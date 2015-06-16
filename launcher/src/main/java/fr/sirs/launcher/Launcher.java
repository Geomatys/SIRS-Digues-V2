
package fr.sirs.launcher;

import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.core.plugins.PluginLoader;
import java.io.IOException;

import java.util.UUID;
import java.util.logging.Level;

import org.slf4j.bridge.SLF4JBridgeHandler;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.geotoolkit.internal.GeotkFX;

/**
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.getIcons().add(SIRS.ICON);
        SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.6.5)

        // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
        // the initialization phase of your application
        SLF4JBridgeHandler.install();
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            final String errorCode = UUID.randomUUID().toString();
            SirsCore.LOGGER.log(Level.SEVERE, errorCode, e);
            // deactivates to avoid being bothered for minor UI errors.
//            final Runnable exceptionDialog = () -> {
//                SIRS.newExceptionDialog("Une erreur inattendue est survenue.Code d'erreur : "+errorCode, e).show();
//            };
//            if (Platform.isFxApplicationThread()) exceptionDialog.run();
//            else Platform.runLater(exceptionDialog);
        });

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setBackground(Background.EMPTY);
        final Label splashLabel = new Label("Initialisation de la base EPSG");
        final VBox vbox = new VBox(progressIndicator, splashLabel);
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);
        StackPane stackPane = new StackPane(vbox);
        stackPane.setBackground(Background.EMPTY);
        final Scene scene = new Scene(stackPane);
        scene.setFill(null);

        String version = SIRS.getVersion();
        if (version == null)
            version = "";
        
        final Stage splashStage = new Stage();
        splashStage.getIcons().add(SIRS.ICON);
        splashStage.setTitle("SIRS "+version);
        splashStage.initStyle(StageStyle.TRANSPARENT);
        splashStage.setScene(scene);

        primaryStage.setTitle("SIRS "+version);
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            System.exit(0);
        });

        splashStage.show();

        /*
         * Initialize / create EPSG db. A loader is displayed while the task is 
         * running, preventing application launch.
         */
        final Task<Boolean> epsgIniter = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                SirsCore.initEpsgDB();
                final ClassLoader scl = ClassLoader.getSystemClassLoader();
                if (scl instanceof PluginLoader) {
                    Platform.runLater(()-> splashLabel.setText("Chargement des plugins"));
                    ((PluginLoader)scl).loadPlugins();
                }
                return true;
            }
        };
        epsgIniter.setOnSucceeded((WorkerStateEvent event) -> {
            splashLabel.setText("Lancement de l'application");
            
            FXLauncherPane launcherPane;
            try {
                launcherPane = new FXLauncherPane();
                primaryStage.setScene(new Scene(launcherPane));
                splashStage.close();
                primaryStage.show();
            } catch (IOException ex) {
                GeotkFX.newExceptionDialog("impossible de se connecter au serveur CouchDb local.", ex).showAndWait();
                System.exit(1);
            }
        });

        epsgIniter.setOnFailed((WorkerStateEvent event) -> {
            GeotkFX.newExceptionDialog("Impossible de se connecter à la base EPSG.", event.getSource().getException()).showAndWait();
            System.exit(1);
        });

        epsgIniter.setOnCancelled((WorkerStateEvent event) -> {
            System.exit(0);
        });
        new Thread(epsgIniter).start();

    }

    @Override
    public void stop() throws Exception {
        SirsCore.getTaskManager().close();
    }
    
    
}
