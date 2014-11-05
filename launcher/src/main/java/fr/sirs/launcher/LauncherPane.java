
package fr.sirs.launcher;

import com.healthmarketscience.jackcess.DatabaseBuilder;

import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.core.CouchDBInit;
import fr.sirs.core.DatabaseRegistry;
import static fr.sirs.core.CouchDBInit.DB_CONNECTOR;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import org.apache.sis.util.logging.Logging;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.geotoolkit.internal.io.IOUtilities;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class LauncherPane extends BorderPane {

    private static final Logger LOGGER = Logging.getLogger(LauncherPane.class);
    private static final String URL_LOCAL = "http://geouser:geopw@localhost:5984";
    
    @FXML
    private TextField uiDistantName;
    @FXML
    private TextField uiNewName;
    @FXML
    private TextField uiImportDBData;
    @FXML
    private TextField uiImportDBCarto;
    @FXML
    private TableView<String> uiLocalBaseTable;
    @FXML
    private TextField uiDistantUrl;
    @FXML
    private CheckBox uiDistantSync;
    @FXML
    private TextField uiImportName;           
    @FXML
    private Button uiCreateButton;     
    @FXML
    private Button uiImportButton;
    @FXML
    private Button uiConnectButton;
    @FXML
    private ProgressBar uiProgressCreate;    
    @FXML
    private ProgressBar uiProgressImport;
    @FXML
    private TabPane uiTabPane;
    @FXML
    private Tab uiLocalDbTab;
    
    public LauncherPane() {
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
        
        updateLocalDbList();
        
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
    void connectLocal(ActionEvent event) {
        final String db = uiLocalBaseTable.getSelectionModel().getSelectedItem();
        runDesktop(URL_LOCAL, db);
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
                            URL_LOCAL, uiImportName.getText().trim(), "classpath:/fr/sirs/spring/couchdb-context.xml");
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

    }
        
    private static void runDesktop(final String serverUrl, final String database){
        new Thread(){
            @Override
            public void run() {
                final File folder = new File(".");
                final File[] sub = folder.listFiles();
                File desktopFile = null;
                String name = "";
                for(File f : sub){
                    if(f.getName().toLowerCase().startsWith("desktop")){
                        desktopFile = f;
                        name = f.getName();
                    }
                }

                try {
                    final Properties prop = new Properties();
                    prop.setProperty("url", serverUrl);
                    prop.setProperty("database", database);
                    prop.store(new FileOutputStream(new File("params.run")), "");
                    Desktop.getDesktop().open(desktopFile);
                    System.exit(0);
                    
                    //new Alert(Alert.AlertType.ERROR, "java -jar "+name+" \""+serverUrl+"\" \""+database+"\"").showAndWait();
                    //Runtime.getRuntime().exec("java -jar "+desktopFile.getAbsolutePath()+" "+serverUrl+" "+database);
                    //Runtime.getRuntime().exec(new String[]{"java","-jar",name,serverUrl,database});
                    //ProcessBuilder pb = new ProcessBuilder("java","-jar",desktopFile.getAbsolutePath(), serverUrl, database);
                    //pb.start();
                    
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
                    ex.printStackTrace();
                }
            }
            
        }.start();
        
    }
    
    private static File getPreviousPath() {
        final Preferences prefs = Preferences.userNodeForPackage(LauncherPane.class);
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
        final Preferences prefs = Preferences.userNodeForPackage(LauncherPane.class);
        prefs.put("path", path.getAbsolutePath());
    }
 
    private static List<String> listLocalDatabase(){
        List<String> dbs = new ArrayList<>();
        
        try {
            dbs = DatabaseRegistry.listSirsDatabase(new URL(URL_LOCAL));
        } catch (MalformedURLException ex) {
            Logger.getLogger(LauncherPane.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return dbs;
    }
    
}
