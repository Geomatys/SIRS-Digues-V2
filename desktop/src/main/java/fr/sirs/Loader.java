package fr.sirs;

import static fr.sirs.core.model.Role.ADMIN;
import static fr.sirs.core.model.Role.USER;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import org.ektorp.CouchDbConnector;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.image.jai.Registry;
import org.geotoolkit.lang.Setup;
import org.geotoolkit.sld.xml.JAXBSLDUtilities;
import org.geotoolkit.sld.xml.StyleXmlIO;

import fr.sirs.core.SirsCore;
import fr.sirs.core.component.DatabaseRegistry;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.component.UtilisateurRepository;
import fr.sirs.core.h2.H2Helper;
import static fr.sirs.core.model.Role.EXTERN;
import static fr.sirs.core.model.Role.GUEST;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.json.GeometryDeserializer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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
        splashStage.initStyle(StageStyle.TRANSPARENT);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
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
                    final UtilisateurRepository utilisateurRepository = session.getUtilisateurRepository();

                    controller.uiLogInfo.setText("Recherche…");
                    final List<Utilisateur> candidateUsers = utilisateurRepository.getByLogin(controller.uiLogin.getText());

                    if (candidateUsers.isEmpty()) {
                        controller.uiLogInfo.setText("Identifiants erronés.");
                        controller.uiPassword.setText("");
                    } else {

                        Utilisateur user = null;
                        MessageDigest messageDigest = null;
                        String encryptedPassword = null;
                        try {
                            messageDigest = MessageDigest.getInstance("MD5");
                            encryptedPassword = new String(messageDigest.digest(controller.uiPassword.getText().getBytes()));
                        } catch (NoSuchAlgorithmException ex) {
                            Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        for (final Utilisateur candidate : candidateUsers) {
                            if (candidate.getPassword().equals(encryptedPassword)) {
                                user = candidate;
                                break;
                            }
                        }

                        if (user == null) {
                            controller.uiLogInfo.setText("Identifiants erronés.");
                            controller.uiPassword.setText("");
                        } else {
                            session.setUtilisateur(user);
                            if (session.getRole() == ADMIN
                                    || session.getRole() == USER
                                    || session.getRole() == GUEST
                                    || session.getRole() == EXTERN) {
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
                                            showMainStage();
                                        } catch (Throwable ex) {
                                            try {
                                                SIRS.LOGGER.log(Level.WARNING, "Erreur inattendue lors de l'initialisation du panneau principal.", ex);
                                                ExceptionDialog exDialog = GeotkFX.newExceptionDialog("L'application a rencontré une erreur inattendue et doit fermer.", ex);
                                                exDialog.setOnHidden((DialogEvent de)-> System.exit(1));
                                                exDialog.show();
                                                
                                            } catch (Throwable e) {
                                                SIRS.LOGGER.log(Level.WARNING, "Cannot show error dialofg to user", e);
                                                System.exit(1);
                                            }
                                        }
                                    }
                                });
                                fadeSplash.play();

                                session.getTaskManager().submit(session.getReferenceChecker());
                                    
                            }
                        }
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
    private static synchronized void showMainStage() throws IOException {
        final Session session = Injector.getSession();
        FXMainFrame frame = session.getFrame();
        if (frame == null) {
            frame = new FXMainFrame();
            session.setFrame(frame);
        }
        
        Scene mainScene = frame.getScene();
        if (mainScene == null) {
            mainScene = new Scene(frame);
        }
        
        final Stage mainStage;
        if (mainScene.getWindow() instanceof Stage) {
            mainStage = (Stage) mainScene.getWindow();
        } else {
            mainStage = new Stage();
            String userInfo = "";
            String login = session.getUtilisateur().getLogin();
            String role = new SirsStringConverter().toString(session.getRole());
            if(!"".equals(login) || !"".equals(role)){
                userInfo += " - Connecté en tant que "+session.getUtilisateur().getLogin()+ " ("+role+")";
            }
            mainStage.setTitle("SIRS-Digues V2"+userInfo);
            mainStage.setScene(mainScene);
            mainStage.setMaximized(true);
            mainStage.setMinWidth(800);
            mainStage.setMinHeight(600);
            mainStage.setOnCloseRequest((WindowEvent event) -> {System.exit(0);});
        }
            
        mainStage.show();
        frame.getMapTab().show();
    }

    private final class LoadingTask extends Task {

        @Override
        protected Object call() throws InterruptedException {
            try {
                updateMessage("Recherche des plugins");
                int inc = 0;
                final Plugin[] plugins = Plugins.getPlugins();
                final int total = 8 + plugins.length;

                // EPSG DATABASE ///////////////////////////////////////////////
                updateProgress(inc++, total);
                updateMessage("Creation de la base EPSG...");
                // try to create it, won't do anything if already exist
                SirsCore.initEpsgDB();

                // GEOMETRY / JSON Converter
                GeometryDeserializer.class.newInstance();

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
                Injector.getSession().setApplicationContext(context);

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
                updateMessage("Export vers la base RDBMS");
                TaskManager.INSTANCE.submit(new H2Helper.ExportToRDBMS(context.getBean(CouchDbConnector.class)));                

                updateProgress(inc++, total);
                
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
