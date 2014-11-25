
package fr.sirs.launcher;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import fr.sirs.Loader;

import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.core.CouchDBInit;
import fr.sirs.core.DatabaseRegistry;
import static fr.sirs.core.CouchDBInit.DB_CONNECTOR;
import fr.sirs.core.SirsCore;
import fr.sirs.maj.PluginInfo;
import fr.sirs.maj.PluginInstaller;
import fr.sirs.maj.PluginList;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

import org.apache.sis.util.logging.Logging;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXLauncherPane extends BorderPane {

    private static final Logger LOGGER = Logging.getLogger(FXLauncherPane.class);
    private static final String URL_LOCAL = "http://geouser:geopw@localhost:5984";
    
    @FXML private TabPane uiTabPane;
    
    // onglet base locales
    @FXML private TableView<String> uiLocalBaseTable;
    @FXML private Button uiConnectButton;
    
    // onglet base distantes    
    @FXML private TextField uiDistantName;
    @FXML private TextField uiDistantUrl;
    @FXML private CheckBox uiDistantSync;
    
    // onglet base creation
    @FXML private TextField uiNewName;
    @FXML private ProgressBar uiProgressCreate;    
    @FXML private Button uiCreateButton;
    @FXML private TextField uiImportName;
    @FXML private TextField uiImportDBData;
    @FXML private TextField uiImportDBCarto;
    @FXML private ProgressBar uiProgressImport;
    @FXML private Button uiImportButton;
    
    // onglet mise à jour
    @FXML private Button uiMaj;
    @FXML private TextField uiMajServerURL;
    @FXML private Label uiMajCoreVersion;
    @FXML private TableView<PluginInfo> uiPluginTable;
           
    
    private URL serverURL;
    private PluginList local = new PluginList();
    private PluginList distant = new PluginList();
        
    public FXLauncherPane() {
        final Class cdtClass = getClass();
        final String fxmlpath = "/"+cdtClass.getName().replace('.', '/')+".fxml";
        final FXMLLoader loader = new FXMLLoader(cdtClass.getResource(fxmlpath));
        loader.setController(this);
        loader.setRoot(this);
        //in special environement like osgi or other, we must use the proper class loaders
        //not necessarly the one who loaded the FXMLLoader class
        loader.setClassLoader(cdtClass.getClassLoader());
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        
        uiProgressImport.visibleProperty().bindBidirectional(uiImportButton.disableProperty());
        uiProgressCreate.visibleProperty().bindBidirectional(uiCreateButton.disableProperty());
        
        final TableColumn<String,String> column = new TableColumn<>("Base de données");
        column.setCellValueFactory((TableColumn.CellDataFeatures<String, String> param) -> new SimpleObjectProperty<>(param.getValue()));
        
        uiLocalBaseTable.getColumns().clear();
        uiLocalBaseTable.getColumns().add(column);
        uiLocalBaseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        uiLocalBaseTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        
        final TableColumn<PluginInfo,String> colName = new TableColumn<>("Plugin");
        colName.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<PluginInfo, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<PluginInfo, String> param) {
                return param.getValue().nameProperty();
            }
        });
        
        final TableColumn<PluginInfo,String> colVersion = new TableColumn<>();
        colVersion.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<PluginInfo, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<PluginInfo, String> param) {
                final PluginInfo info = param.getValue();
                final String version = info.getVersionMajor()+"."+info.getVersionMinor();
                return new SimpleObjectProperty<>(version);
            }
        });
        
        final TableColumn<PluginInfo,String> colInstall = new TableColumn<>();
        colInstall.setCellValueFactory((TableColumn.CellDataFeatures<PluginInfo, String> param) -> param.getValue().nameProperty());
        colInstall.setCellFactory((TableColumn<PluginInfo, String> param) -> new UpdateCell());
        
        uiPluginTable.getColumns().add(colName);
        uiPluginTable.getColumns().add(colVersion);
        uiPluginTable.getColumns().add(colInstall);
        
        
        updateLocalDbList();
        updatePluginList(null);
    }
    
    private void updateLocalDbList(){
        final ObservableList<String> names = FXCollections.observableList(listLocalDatabase());
        uiLocalBaseTable.setItems(names);
        if(!names.isEmpty()){
            uiLocalBaseTable.getSelectionModel().select(0);
        }
        uiConnectButton.setDisable(names.isEmpty());
    }
    
    @FXML
    void updatePluginList(ActionEvent event) {
        final String majURL = uiMajServerURL.getText();
        if (majURL == null || majURL.isEmpty()) return;
        try{
            serverURL = new URL(uiMajServerURL.getText());
            local = PluginInstaller.listLocalPlugins();
            distant = PluginInstaller.listDistantPlugins(serverURL);
        }catch(Exception ex){
            SirsCore.LOGGER.log(Level.WARNING,ex.getMessage(),ex);
        }
        
        // comparaison du plugin core
        PluginInfo localCore = local.getPluginInfo(PluginInstaller.PLUGIN_CORE);
        PluginInfo distantCore = distant.getPluginInfo(PluginInstaller.PLUGIN_CORE);
        if(localCore==null) localCore = new PluginInfo();
        if(distantCore==null) distantCore = new PluginInfo();
        
        uiMaj.setDisable(!localCore.isOlderOrSame(distantCore));
        uiMajCoreVersion.setText(distantCore.getVersionMajor()+"."+distantCore.getVersionMinor());
        
        
        //Merge de la liste des plugins
        final Set<PluginInfo> plugins = new LinkedHashSet<>();
        plugins.addAll(local.getPlugins());
        plugins.addAll(distant.getPlugins());
        
        uiPluginTable.setItems(FXCollections.observableArrayList(plugins));
        
    }
        
    
    @FXML
    void connectLocal(ActionEvent event) {
        final String db = uiLocalBaseTable.getSelectionModel().getSelectedItem();
        runDesktop(URL_LOCAL, db);
        this.setDisabled(true);
        Window currentWindow = getScene().getWindow();
        if (currentWindow instanceof Stage) {
            ((Stage)currentWindow).close();
        } else {
            currentWindow.hide();
        }
        
    }

    @FXML
    void connectDistant(ActionEvent event) {
        if(uiDistantName.getText().trim().isEmpty()){
            new Alert(Alert.AlertType.ERROR,"Veuillez remplir le nom de la base de donnée",ButtonType.OK).showAndWait();
            return;
        }
        
        final String distantUrl = uiDistantUrl.getText();
        final String localUrl = URL_LOCAL+"/"+uiDistantName.getText();
        
        try {
            DatabaseRegistry.newLocalDBFromRemote(distantUrl, localUrl, uiDistantSync.isSelected());
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            new Alert(Alert.AlertType.ERROR,ex.getMessage(),ButtonType.OK).showAndWait();
        }
        
        //aller au panneau principale
        Platform.runLater(() -> {
            uiTabPane.getSelectionModel().clearAndSelect(0);
            updateLocalDbList();
        });
    }

    @FXML
    void createEmpty(ActionEvent event) {
        if(uiNewName.getText().trim().isEmpty()){
            new Alert(Alert.AlertType.ERROR,"Veuillez remplir le nom de la base de donnée",ButtonType.OK).showAndWait();
            return;
        }
        
        uiCreateButton.setDisable(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                   
                    final HttpClient httpClient = new StdHttpClient.Builder().url(URL_LOCAL).build();
                    final CouchDbInstance couchsb = new StdCouchDbInstance(httpClient);
                    final CouchDbConnector connector = couchsb.createConnector(uiNewName.getText(),true);

                    final ClassPathXmlApplicationContext applicationContextParent = new ClassPathXmlApplicationContext();
                    applicationContextParent.refresh();
                    applicationContextParent.getBeanFactory().registerSingleton(DB_CONNECTOR, connector);

                    final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext( new String[]{
                        "classpath:/fr/sirs/spring/couchdb-context.xml"}, applicationContextParent);
                    
                    applicationContext.close();
                    
                    //aller au panneau principale
                    Platform.runLater(() -> {
                        uiTabPane.getSelectionModel().clearAndSelect(0);
                        updateLocalDbList();
                    });
                    
                }catch(Exception ex){
                    LOGGER.log(Level.WARNING, ex.getMessage(),ex);
                    new Alert(Alert.AlertType.ERROR,ex.getMessage(),ButtonType.CLOSE).showAndWait();
                }finally{
                    Platform.runLater(() -> {uiCreateButton.setDisable(false);});
                }
            }
        }).start();
    }

    @FXML
    void createFromAccess(ActionEvent event) {
        if(uiImportName.getText().trim().isEmpty()){
            new Alert(Alert.AlertType.ERROR,"Veuillez remplir le nom de la base de donnée",ButtonType.OK).showAndWait();
            return;
        }
        
        uiImportButton.setDisable(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    final File mainDbFile = new File(uiImportDBData.getText());
                    final File cartoDbFile = new File(uiImportDBCarto.getText());
                    
                    final ClassPathXmlApplicationContext applicationContext = CouchDBInit.create(
                            URL_LOCAL, uiImportName.getText().trim(), "classpath:/fr/sirs/spring/couchdb-context.xml",true,false);
                    final CouchDbConnector couchDbConnector = applicationContext.getBean(CouchDbConnector.class);
                    DbImporter importer = new DbImporter(couchDbConnector);
                    importer.setDatabase(DatabaseBuilder.open(mainDbFile),
                            DatabaseBuilder.open(cartoDbFile));
                    importer.cleanDb();
                    importer.importation();
                    
                    //aller au panneau principale
                    Platform.runLater(() -> {
                        uiTabPane.getSelectionModel().clearAndSelect(0);
                        updateLocalDbList();
                    });
                    
                }catch(IOException | AccessDbImporterException ex){
                    LOGGER.log(Level.WARNING, ex.getMessage(),ex);
                    new Alert(Alert.AlertType.ERROR,ex.getMessage(),ButtonType.CLOSE).showAndWait();
                }finally{
                    Platform.runLater(() -> {uiImportButton.setDisable(false);});
                }
            }
        }).start();
        
    }

    @FXML
    void chooseMainDb(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        final File prevPath = getPreviousPath();
        if (prevPath != null) {
            fileChooser.setInitialDirectory(prevPath);
        }
        final File file = fileChooser.showOpenDialog(getScene().getWindow());
        if(file!=null){
            setPreviousPath(file.getParentFile());
            uiImportDBData.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void chooseCartoDb(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        final File prevPath = getPreviousPath();
        if (prevPath != null) {
            fileChooser.setInitialDirectory(prevPath);
        }
        final File file = fileChooser.showOpenDialog(getScene().getWindow());
        if(file!=null){
            setPreviousPath(file.getParentFile());
            uiImportDBCarto.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void updateApp(ActionEvent event) {
        try {
            restartCore();
            final PluginInfo corePlugin = distant.getPluginInfo(PluginInstaller.PLUGIN_CORE);
            if(corePlugin != null){
                PluginInstaller.install(serverURL, corePlugin);
            }
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
        
    private static void runDesktop(final String serverUrl, final String database){
                try {
                    //Loader.main(new String[]{serverUrl, database});
                    new Loader(serverUrl, database).start(null);
                    //System.exit(0);
                    
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Cannot run desktop application with database "+serverUrl+":"+database, ex);
                    new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
                }
        
    }
    
    private static File getPreviousPath() {
        final Preferences prefs = Preferences.userNodeForPackage(FXLauncherPane.class);
        final String str = prefs.get("path", null);
        if(str!=null){
            final File file = new File(str);
            if(file.isDirectory()){
                return file;
            }
        }
        return null;
    }

    private static void setPreviousPath(final File path) {
        final Preferences prefs = Preferences.userNodeForPackage(FXLauncherPane.class);
        prefs.put("path", path.getAbsolutePath());
    }
 
    private static List<String> listLocalDatabase() {
        List<String> dbs = new ArrayList<>();
        
        try {
            dbs = DatabaseRegistry.listSirsDatabase(new URL(URL_LOCAL));
        } catch (MalformedURLException ex) {
            Logger.getLogger(FXLauncherPane.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return dbs;
    }
    
    private static void restartCore() throws URISyntaxException, IOException {
        final Pattern jarPattern = Pattern.compile("(?i).*\\.jar");
        final Path javaHome = Paths.get(System.getProperty("java.home"));
        final Path javaBin = javaHome.resolve("bin").resolve("java")
                .toAbsolutePath();

        /*
         * If we cannot find java executable in java home, it means application
         * has been deployed and started from a native package context. We have
         * to find the native executable and launch it.
         * Into native package, jre is located into : 
         * $APP_DIR/runtime/jre
         * Application executable should be named sirs-launcher* and located in :
         * $APP_DIR/
         * TODO : Find a better way to retrieve application executable
         */
        final List<String> args = new ArrayList<>();
        if (!Files.isExecutable(javaBin)) {
            final Path appDir = javaHome.getParent().getParent();
            LOGGER.log(Level.INFO, "Application directory : " + appDir.toString());
            // we seek for a sirs + something executable file which is not the
            // uninstaller.
            Optional<Path> appExec = Files.walk(appDir, 1).filter(path -> {
                final String str = path.getFileName().toString();
                return str.matches("(?i)sirs.*") && 
                        !str.toLowerCase().contains("unins")
                        && Files.isExecutable(path) 
                        && Files.isRegularFile(path); // Check needed, cause a diectory can be marked executable...
            }).findFirst();
            if (appExec.isPresent()) {
            LOGGER.log(Level.INFO, "Application executable " + appExec.toString());
                args.add(appExec.get().toString());
            } else {
                throw new IOException("No executable file can be found to restart SIRS application.");
            }

        } else {
            args.add(javaBin.toString());
            String command = System.getProperty("sun.java.command");
            /* If java command has not been saved (which is really unlikely to 
             * happen), we must retrieve the Launcher application context (jar
             * or class).
             */
            if (command == null || command.isEmpty()) {
                final String applJar = Paths.get(
                        Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                        .toAbsolutePath().toString();
                if (jarPattern.matcher(applJar).matches()) {
                    command = applJar;
                } else {
                    command = Launcher.class.getName();
                }
            } else {
                final String[] splitted = command.split(" ");
                if (splitted != null && splitted.length > 0) {
                    command = splitted[0];
                }
            }
            args.add("-classpath");
            args.add(System.getProperty("java.class.path"));
            LOGGER.log(Level.INFO, command);
            if (jarPattern.matcher(command).matches()) {
                args.add("-jar");
            }
            args.add(command);
        }
        
        final ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(new File(System.getProperty("user.dir")));
        builder.start();
        System.exit(0);
    }
    
    private final class UpdateCell extends TableCell<PluginInfo, String> {

        private final Button button = new Button("Install");
        private String name;

        public UpdateCell() {
             button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    PluginInfo pluginInfo = distant.getPluginInfo(name);
                    PluginInstaller.install(serverURL, pluginInfo);
                    updatePluginList(null);
                }
            });
        }
        
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty) {
                button.setDisable(true);
                return;
            }

            final PluginInfo localPlugin = local.getPluginInfo(item);
            final PluginInfo distantPlugin = distant.getPluginInfo(item);

            if (localPlugin == null && distantPlugin == null) {
                button.setDisable(true);
            }
            if (localPlugin == null) {
                button.setDisable(false);
            } else if (distantPlugin == null) {
                button.setDisable(true);
            } else {
                button.setDisable(localPlugin.isOlderOrSame(distantPlugin));
            }
        }
        
    }
    
}
