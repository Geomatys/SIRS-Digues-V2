package fr.sirs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import javax.sql.DataSource;

import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.image.jai.Registry;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.lang.Setup;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.factory.epsg.EpsgInstaller;
import org.geotoolkit.sld.xml.JAXBSLDUtilities;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.opengis.util.FactoryException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import fr.sirs.core.CouchDBInit;
import fr.sirs.core.h2.H2Helper;
import fr.sirs.util.json.GeometryDeserializer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class Loader extends Application {

    private static String DATABASE_URL = "http://geouser:geopw@localhost:5984";
    private static String DATABASE_NAME = "sirs";
    
    private String databaseUrl;
    private String databaseName;
    
    public Loader() {
        this(DATABASE_URL, DATABASE_NAME);
    }
    
    public Loader(String databaseUrl, String databaseName) {
        ArgumentChecks.ensureNonEmpty("Database URL", databaseUrl);
        ArgumentChecks.ensureNonEmpty("Database name", databaseName);
        this.databaseUrl = databaseUrl;
        this.databaseName = databaseName;
    }
    
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * TODO : Remove the main, we should always launch application using 
     * Launcher module.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        if(args.length==0){
             try{
                final File propFile = new File("params.run");
                final Properties prop = new Properties();
                prop.load(new FileInputStream(propFile));
                args = new String[]{
                    prop.getProperty("url"),
                    prop.getProperty("database")
                };
            }catch(FileNotFoundException ex){
                 System.out.println(ex.getMessage());
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
        
        try {
            //wait a little in case the launcher is a bit long to close and release the derby database
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Starting application with : "+Arrays.toString(args));
        if(args.length>0 && args[0]!=null) DATABASE_URL = args[0];
        if(args.length>1 && args[1]!=null) DATABASE_NAME = args[1];
        
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // perform initialization and plugin loading tasks
        final Task initTask = new LoadingTask();
        showLoadingStage(initTask);
        new Thread(initTask).start();
    }

    /**
     * Display splash screen.
     *
     * @param task
     * @throws IOException
     */
    private void showLoadingStage(Task task) throws IOException {

        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/sirs/FXSplashscreen.fxml"));
        final Parent root = loader.load();
        final FXSplashscreen controller = loader.getController();
        controller.uiCancel.setVisible(false);
        controller.uiProgressLabel.textProperty().bind(task.messageProperty());
        controller.uiProgressBar.progressProperty().bind(task.progressProperty());

        final Scene scene = new Scene(root);
        scene.setFill(null);
        scene.getStylesheets().add("/fr/sirs/splashscreen.css");

        final Stage splashStage = new Stage();
        splashStage.setTitle("SIRS-Digues V2");
        splashStage.initStyle(StageStyle.TRANSPARENT);
        splashStage.setScene(scene);
        splashStage.show();

        task.stateProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue == Worker.State.SUCCEEDED) {
                    splashStage.toFront();
                    final FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), root);
                    fadeSplash.setFromValue(1.0);
                    fadeSplash.setToValue(0.0);
                    fadeSplash.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            splashStage.hide();
                            try {
                                showMainStage();
                            } catch (IOException ex) {
                                SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
                            }
                        }
                    });
                    fadeSplash.play();
                } else if (newValue == Worker.State.CANCELLED) {
                    controller.uiProgressLabel.getStyleClass().remove("label");
                    controller.uiProgressLabel.getStyleClass().add("label-error");
                    controller.uiCancel.setVisible(true);
                }
            }
        });

    }

    /**
     * Display the main frame.
     */
    private void showMainStage() throws IOException {
                
        final FXMainFrame frame = new FXMainFrame();
        final Session session = Injector.getBean(Session.class);
        session.setFrame(frame);
        final Stage stage = new Stage();
        stage.setTitle("SIRS-Digues V2");
        stage.setScene(new Scene(frame));
        stage.setOnCloseRequest((WindowEvent event) -> {System.exit(0);});
        stage.setMaximized(true);
        stage.show();
    }

    private final class LoadingTask extends Task {

        @Override
        protected Object call() throws InterruptedException {
            try {
                updateMessage("Recherche des plugins");
                int inc = 0;
                final Plugin[] plugins = Plugins.getPlugins();
                final int total = 7 + plugins.length;

                // EPSG DATABASE ///////////////////////////////////////////////
                updateProgress(inc++, total);
                updateMessage("Creation de la base EPSG...");
                // create a database in user directory
                final Path storageFolder = SIRS.CONFIGURATION_PATH
                        .resolve("EPSG");
                final String url = "jdbc:derby:" + storageFolder.toString()
                        + File.separator + "EPSG;create=true";
                final DataSource ds = new DefaultDataSource(url);
                Hints.putSystemDefault(Hints.EPSG_DATA_SOURCE, ds);

                // try to create it, won't do anything if already exist
                createEpsgDB(storageFolder, url);
                // force loading epsg
                CRS.decode("EPSG:3395");

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
                // Geotoolkit startup
                Setup.initialize(null);
                // work in lazy mode, do your best for lenient datum shift
                Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);

                // DATABASE ////////////////////////////////////////////////////
                updateProgress(inc++, total);
                updateMessage("Chargement des pilotes pour base de données...");
                // loading drivers, some plugin systems requiere this call ,
                // like netbeans RCP
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver")
                        .newInstance();
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
                final ClassPathXmlApplicationContext context = CouchDBInit
                        .create(databaseUrl,
                                databaseName,
                                "classpath:/fr/sirs/spring/application-context.xml",
                                false, true);

                // LOAD PLUGINS ////////////////////////////////////////////////
                for (Plugin plugin : plugins) {
                    updateProgress(inc++, total);
                    updateMessage("Chargement du plugin "
                            + plugin.getLoadingMessage().getValue());
                    plugin.load();
                }

                updateMessage("Exporting data to RDBMS");

                H2Helper.exportDataToRDBMS(context.getBean(CouchDbConnector.class));

                updateProgress(inc++, total);

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

    private static void createEpsgDB(final Path folder, final String url)
            throws FactoryException, IOException {
        Files.createDirectories(folder);
        final EpsgInstaller installer = new EpsgInstaller();
        installer.setDatabase(url);
        if (!installer.exists()) {
            installer.call();
        }
    }

}
