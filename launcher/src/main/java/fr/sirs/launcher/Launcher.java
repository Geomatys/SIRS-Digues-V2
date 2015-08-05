
package fr.sirs.launcher;

import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.core.SirsCore.UpdateInfo;
import static fr.sirs.core.SirsCore.browseURL;
import fr.sirs.core.plugins.PluginLoader;
import fr.sirs.core.authentication.SIRSAuthenticator;
import fr.sirs.util.SystemProxySelector;
import java.net.Authenticator;
import java.net.ProxySelector;
import java.util.Optional;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import org.slf4j.bridge.SLF4JBridgeHandler;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
        // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
        // the initialization phase of your application
        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.6.5)
            SLF4JBridgeHandler.install();
        } catch (Exception e) {
            SirsCore.LOGGER.log(Level.SEVERE, "Cannot initialize log dumping (logbak)", e);
            // We allow starting program without log writing.
        }

        try {
            Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
                if ((e instanceof OutOfMemoryError) || (e.getCause() instanceof OutOfMemoryError)) {
                    try {
                        SirsCore.LOGGER.log(Level.SEVERE, null, e);
                    } finally {
                        System.exit(1);
                    }
                } else {
                    SirsCore.LOGGER.log(Level.SEVERE, "Uncaught error !", e);
                }
            });
        } catch (Exception e) {
            SirsCore.LOGGER.log(Level.SEVERE, "Cannot initialize uncaught exception management.", e);
            // We allow starting program without that feature.
        }

        String version = null;
        try {
            version = SIRS.getVersion();
        } catch (Exception e) {
            SirsCore.LOGGER.log(Level.WARNING, "Cannot retrieve application version.", e);
        }
        if (version == null)
            version = "";

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setBackground(Background.EMPTY);
        final Label splashLabel = new Label();
        final VBox vbox = new VBox(progressIndicator, splashLabel);
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);
        StackPane stackPane = new StackPane(vbox);
        stackPane.setBackground(Background.EMPTY);
        final Scene scene = new Scene(stackPane);
        scene.setFill(null);

        final Stage splashStage = new Stage();
        splashStage.getIcons().add(SIRS.ICON);
        splashStage.setTitle("SIRS "+version);
        splashStage.initStyle(StageStyle.TRANSPARENT);
        splashStage.setScene(scene);

        primaryStage.getIcons().add(SIRS.ICON);
        primaryStage.setTitle("SIRS "+version);
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            System.exit(0);
        });

        splashStage.show();

        /*
         * Initialize / create EPSG db. A loader is displayed while the task is
         * running, preventing application launch.
         */
        final Task<Boolean> initer = new Initer();
        splashLabel.textProperty().bind(initer.messageProperty());
        splashLabel.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            splashStage.sizeToScene();
        });

        initer.setOnSucceeded((WorkerStateEvent event) -> {
            FXLauncherPane launcherPane;
            try {
                launcherPane = new FXLauncherPane();
                primaryStage.setScene(new Scene(launcherPane));
                splashStage.close();
                primaryStage.show();
            } catch (Exception ex) {
                GeotkFX.newExceptionDialog("impossible de se connecter au serveur CouchDb local.", ex).showAndWait();
                SirsCore.LOGGER.log(Level.SEVERE, "Impossible d'initialiser le launcher.", ex);
                System.exit(1);
            }
        });

        initer.setOnFailed((WorkerStateEvent event) -> {
            GeotkFX.newExceptionDialog("Impossible de se connecter à la base EPSG.", event.getSource().getException()).showAndWait();
            System.exit(1);
        });

        initer.setOnCancelled((WorkerStateEvent event) -> {
            System.exit(0);
        });
        new Thread(initer).start();
    }

    @Override
    public void stop() throws Exception {
        SirsCore.getTaskManager().close();
    }

    /**
     * Check if there's any update available on SIRS server. If any, user is asked for download.
     */
    private static boolean checkUpdate() throws InterruptedException, ExecutionException {
        final UpdateInfo info;
        try {
            info = SirsCore.checkUpdate().get();
        } catch (Exception ex) {
            SirsCore.LOGGER.log(Level.WARNING, "Impossible de charger le numéro de version de l'application.", ex);
            return false;
        }
        if (info != null) {
            // Now that we found that an update is available, we can redirect user on package URL.
            final Task<Boolean> askForUpdate = new Task<Boolean>() {

                @Override
                protected Boolean call() throws Exception {
                    final Alert alert = new Alert(
                            Alert.AlertType.INFORMATION,
                            "Une mise à jour de l'application est disponible (" + info.localVersion + " vers " + info.distantVersion + "). Voulez-vous l'installer ?",
                            ButtonType.NO, ButtonType.YES);
                    alert.setWidth(400);
                    alert.setHeight(300);
                    alert.setResizable(true);
                    final Optional<ButtonType> choice = alert.showAndWait();
                    if (ButtonType.YES.equals(choice.orElse(ButtonType.NO))) {
                        browseURL(info.updateURL, "Mise à jour", true);
                        // Once downloaded, we stop the system to allow user to install its new package.
                        return true;
                    }
                    return false;
                }
            };
            Platform.runLater(() -> askForUpdate.run());
            // Shutdown program to allow user installing software update without any conflict.
            if (Boolean.TRUE.equals(askForUpdate.get())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Task which takes care of components initialisation, as EPSG database and plugins.
     */
    private static final class Initer extends Task<Boolean> {

        @Override
        protected Boolean call() throws Exception {
            updateMessage("Analyse des configurations réseau");
            ProxySelector.setDefault(new SystemProxySelector());
            Authenticator.setDefault(new SIRSAuthenticator());

            updateMessage("Vérification des mises à jour");
            boolean updateRequired = checkUpdate();
            if (updateRequired) {
                cancel();
                return false;
            }

            updateMessage("Initialisation de la base EPSG");
            SirsCore.initEpsgDB();

            final ClassLoader scl = ClassLoader.getSystemClassLoader();
            if (scl instanceof PluginLoader) {
                updateMessage("Chargement des plugins");
                ((PluginLoader) scl).loadPlugins();
            }
            updateMessage("Lancement de l'application");
            return true;
        }
    }
}
