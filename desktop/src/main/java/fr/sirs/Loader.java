package fr.sirs;


import static fr.sirs.SIRS.hexaMD5;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import javax.imageio.ImageIO;

import org.geotoolkit.factory.Hints;
import org.geotoolkit.image.jai.Registry;
import org.geotoolkit.lang.Setup;
import org.geotoolkit.sld.xml.JAXBSLDUtilities;
import org.geotoolkit.sld.xml.StyleXmlIO;

import fr.sirs.core.SirsCore;
import fr.sirs.core.component.DatabaseRegistry;
import fr.sirs.core.component.UtilisateurRepository;
import fr.sirs.core.h2.H2Helper;
import fr.sirs.core.model.Role;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.util.SirsStringConverter;
import java.util.List;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.scene.control.DialogEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.apache.sis.util.ArgumentChecks;
import org.controlsfx.dialog.ExceptionDialog;
import org.geotoolkit.internal.GeotkFX;
import org.springframework.context.ConfigurableApplicationContext;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class Loader extends Application {

    private final Stage splashStage;
    private final String databaseName;

    public Loader(String databaseName) {
        ArgumentChecks.ensureNonEmpty("Database name", databaseName);
        this.databaseName = databaseName;
        // Initialize splash screen
        splashStage = new Stage(StageStyle.TRANSPARENT);
        splashStage.setTitle("SIRS-Digues V2");
        splashStage.getIcons().add(SIRS.ICON);
        splashStage.initStyle(StageStyle.TRANSPARENT);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if(primaryStage!=null) primaryStage.getIcons().add(SIRS.ICON);
        // perform initialization and plugin loading tasks
        final Task initTask = new LoadingTask();
        showLoadingStage(initTask);
        new Thread(initTask).start();
    }

    public void showSplashStage() {
        splashStage.show();
    }

    /**
     * Display splash screen.
     *
     * @param task
     * @throws IOException
     */
    private void showLoadingStage(Task task) throws IOException {

        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/sirs/FXSplashscreen.fxml"));
        final GridPane root = loader.load();
        final FXSplashscreen controller = loader.getController();
        controller.uiCancel.setVisible(false);
        controller.uiProgressLabel.textProperty().bind(task.messageProperty());
        controller.uiProgressBar.progressProperty().bind(task.progressProperty());

        final Scene scene = new Scene(root);
        scene.getStylesheets().add("/fr/sirs/splashscreen.css");
        scene.setFill(new Color(0, 0, 0, 0));

        splashStage.setScene(scene);
        splashStage.show();

        task.stateProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue == Worker.State.SUCCEEDED) {
                    splashStage.toFront();
                    controller.uiLoadingPane.setVisible(false);
                    controller.uiLoginPane.setVisible(true);
                } else if (newValue == Worker.State.CANCELLED) {
                    controller.uiProgressLabel.getStyleClass().remove("label");
                    controller.uiProgressLabel.getStyleClass().add("label-error");
                    controller.uiCancel.setVisible(true);
                }
            }
        });

        controller.uiPassword.setOnAction((ActionEvent e)-> controller.uiConnexion.fire());
        controller.uiConnexion.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.uiConnexion.setDisable(true);
                try {
                    final Session session = Injector.getBean(Session.class);
                    final UtilisateurRepository utilisateurRepository = (UtilisateurRepository) session.getRepositoryForClass(Utilisateur.class);

                    controller.uiLogInfo.setText("Recherche…");
                    final List<Utilisateur> candidateUsers = utilisateurRepository.getByLogin(controller.uiLogin.getText());

                    if (candidateUsers.isEmpty()) {
                        controller.uiLogInfo.setText("Identifiants erronés.");
                        controller.uiPassword.setText("");
                        return;
                    }

                    // Database passwords are encrypted, so we encrypt input password to compare both.
                        final String passwordText = controller.uiPassword.getText();
                        final String encryptedPassword;
                        if (passwordText == null || passwordText.isEmpty()) {
                            encryptedPassword = null;
                        } else {
                            encryptedPassword = hexaMD5(passwordText);
                        }

                        Utilisateur user = null;
                        for (final Utilisateur candidate : candidateUsers) {
                            if (Objects.equals(encryptedPassword, candidate.getPassword())) {
                                user = candidate;
                                break;
                            }
                        }

                    if (user == null) {
                        controller.uiLogInfo.setText("Identifiants erronés.");
                        controller.uiPassword.setText("");

                    } else {
                        session.setUtilisateur(user);
                        controller.uiLogInfo.setText("Identifiants valides.");
                        final FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), root);
                        fadeSplash.setFromValue(1.0);
                        fadeSplash.setToValue(0.0);
                        fadeSplash.setOnFinished(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                splashStage.hide();
                                root.setOpacity(1.0);
                                try {
                                    createMainStage();
                                } catch (Throwable ex) {
                                    try {
                                        SIRS.LOGGER.log(Level.WARNING, "Erreur inattendue lors de l'initialisation du panneau principal.", ex);
                                        ExceptionDialog exDialog = GeotkFX.newExceptionDialog("L'application a rencontré une erreur inattendue et doit fermer.", ex);
                                        exDialog.setOnHidden((DialogEvent de) -> System.exit(1));
                                        exDialog.show();

                                    } catch (Throwable e) {
                                        SIRS.LOGGER.log(Level.WARNING, "Cannot show error dialog to user", e);
                                        System.exit(1);
                                    }
                                }
                            }
                        });
                        fadeSplash.play();
                    }
                } finally {
                    controller.uiConnexion.setDisable(false);
                }
            }
        });
    }

    /**
     * Display the main frame.
     */
    private static synchronized void createMainStage() throws IOException {
        final Session session = Injector.getSession();
        final FXMainFrame frame = new FXMainFrame();
        session.setFrame(frame);
        Scene mainScene = new Scene(frame);

        final Stage mainStage = new Stage();
        mainStage.getIcons().add(SIRS.ICON);
        mainStage.titleProperty().bind(Bindings.createStringBinding(() -> {
            StringBuilder builder = new StringBuilder("SIRS-Digues 2");
            final String version = SIRS.getVersion();
            if (version != null && !version.isEmpty()) {
                builder.append(" v").append(version);
            }
            builder.append(" - Utilisateur ");
            Utilisateur user = session.utilisateurProperty().get();
            if (user == null || user.equals(UtilisateurRepository.GUEST_USER)) {
                builder.append("invité");
            } else {
                builder.append(user.getLogin());
            }
            builder.append(" (rôle ");
            if (user == null || user.getRole() == null) {
                builder.append(new SirsStringConverter().toString(Role.GUEST));
            } else {
                builder.append(new SirsStringConverter().toString(user.getRole()));
            }
            builder.append(")");
            return builder.toString();
        }, session.utilisateurProperty()));

        mainStage.setScene(mainScene);
        mainStage.setMaximized(true);
        mainStage.setMinWidth(800);
        mainStage.setMinHeight(600);
        mainStage.setOnCloseRequest((WindowEvent event) -> {
            System.exit(0);
        });

        mainStage.sizeToScene();
        mainStage.show();
        frame.getMapTab().show();

        frame.showAlertsPopup();
    }

    private final class LoadingTask extends Task {

        @Override
        protected Object call() throws InterruptedException {
            try {
                updateMessage("Recherche des plugins");
                int inc = 0;
                final Plugin[] plugins = Plugins.getPlugins();
                final int total = 9 + plugins.length;

                // EPSG DATABASE ///////////////////////////////////////////////
                updateProgress(inc++, total);
                updateMessage("Creation de la base EPSG...");
                // try to create it, won't do anything if already exist
                SirsCore.initEpsgDB();

                // IMAGE ///////////////////////////////////////////////////////
                updateProgress(inc++, total);
                updateMessage("Chargement des lecteurs d'images...");
                Registry.setDefaultCodecPreferences();
                // global initialization
                ImageIO.scanForPlugins();

                // GEOTK ///////////////////////////////////////////////////////
                updateProgress(inc++, total);
                updateMessage("Chargement de Geotoolkit...");
                // work in lazy mode, do your best for lenient datum shift
                Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
                // Geotoolkit startup. We specify that we don't want to use Java preferences,
                // because it does not work on most systems anyway.
                final Properties noJavaPrefs = new Properties();
                noJavaPrefs.put("platform", "server");
                Setup.initialize(noJavaPrefs);

                // DATABASE ////////////////////////////////////////////////////
                updateProgress(inc++, total);
                updateMessage("Chargement des pilotes pour base de données...");
                // loading drivers, some plugin systems requiere this call ,
                // like netbeans RCP
                Class.forName("org.postgresql.Driver").newInstance();
                Class.forName("org.h2.Driver").newInstance();

                // JAXB ////////////////////////////////////////////////////////
                updateProgress(inc++, total);
                updateMessage("Chargement des parseurs XML/JSON...");
                // force loading marshallers
                JAXBSLDUtilities.getMarshallerPoolSLD110();
                JAXBSLDUtilities.getMarshallerPoolSLD100();
                final StyleXmlIO io = new StyleXmlIO();

                // LOAD SIRS DATABASE //////////////////////////////////////////
                updateProgress(inc++, total);
                updateMessage("Chargement et création des index ...");
                final ConfigurableApplicationContext context =
                        new DatabaseRegistry().connectToSirsDatabase(databaseName, true, true, true);

                // LOAD PLUGINS ////////////////////////////////////////////////
                for (Plugin plugin : plugins) {
                    updateProgress(inc++, total);
                    updateMessage("Chargement du plugin "
                            + plugin.getLoadingMessage().getValue());
                    plugin.load();
                }

                // MAP INITIALISATION //////////////////////////////////////////
                //Affiche le contexte carto et le déplace à la date du jour
                updateProgress(inc++, total);
                updateMessage("Initialisation de la carte");
                Injector.getSession().getMapContext().getAreaOfInterest();

                // COUCHDB TO SQL //////////////////////////////////////////////
                updateProgress(inc++, total);
                updateMessage("Export vers la base RDBMS");
                H2Helper.init();
                
                // VÉRIFICATION DES RÉFÉRENCES
                updateProgress(inc++, total);
                updateMessage("Synchronisation des listes de références");
                Injector.getSession().getReferenceChecker().call();

                // OVER
                updateProgress(total, total);
                updateMessage("Chargement terminé.");
                Thread.sleep(400);
            } catch (Throwable ex) {
                updateMessage("Une erreur inattendue est survenue : "
                        + ex.getLocalizedMessage());
                SIRS.LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                cancel();
            }
            return null;
        }
    }
}
