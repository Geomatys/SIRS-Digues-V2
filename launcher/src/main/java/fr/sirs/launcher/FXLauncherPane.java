
package fr.sirs.launcher;

import com.healthmarketscience.jackcess.DatabaseBuilder;

import fr.sirs.Loader;
import fr.sirs.Plugins;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.core.component.DatabaseRegistry;
import fr.sirs.core.component.SirsDBInfoRepository;
import fr.sirs.PluginInfo;
import fr.sirs.core.model.Role;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.LOGIN_DEFAULT_GUEST;
import static fr.sirs.SIRS.PASSWORD_ENCRYPT_ALGO;
import fr.sirs.Session;
import fr.sirs.core.SirsCore;
import fr.sirs.core.SirsCoreRuntimeExecption;
import fr.sirs.core.component.UtilisateurRepository;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.maj.PluginInstaller;
import fr.sirs.maj.PluginList;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

import org.apache.sis.util.logging.Logging;
import org.ektorp.CouchDbConnector;
import org.geotoolkit.gui.javafx.crs.FXCRSButton;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.context.ConfigurableApplicationContext;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 * @author Samuel Andres (Geomatys)
 */
public class FXLauncherPane extends BorderPane {

    private static final Logger LOGGER = Logging.getLogger(FXLauncherPane.class);
    
    /**
     * Si le serveur de plugins ne pointe pas sur une liste valide de {@link PluginInfo}
     * serialisés, on essaie d'atteindre le fichier suivant.
     */
    private static final String DEFAULT_PLUGIN_DESCRIPTOR = "plugins.json";
    
    @FXML private Label errorLabel;
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
    @FXML private FXCRSButton uiNewCRS;
    @FXML private ProgressBar uiProgressCreate;    
    @FXML private Button uiCreateButton;
    @FXML private TextField uiCreateLogin;
    @FXML private PasswordField uiCreatePassword;
    @FXML private PasswordField uiCreateConfirmPassword;
    
    // onglet base importation
    @FXML private TextField uiImportName;
    @FXML private FXCRSButton uiImportCRS;
    @FXML private TextField uiImportDBData;
    @FXML private TextField uiImportDBCarto;
    @FXML private ProgressBar uiProgressImport;
    @FXML private Button uiImportButton;
    @FXML private TextField uiImportLogin;
    @FXML private PasswordField uiImportPassword;
    @FXML private PasswordField uiImportConfirmPassword;
    
    // onglet mise à jour
    @FXML private TextField uiMajServerURL;
    @FXML private TableView<PluginInfo> uiInstalledPlugins;
    @FXML private TableView<PluginInfo> uiAvailablePlugins;
    
    @FXML private Button uiInstallPluginBtn;
    @FXML private Button uiDeletePluginBtn;
    
    @FXML private ProgressBar uiProgressPlugins;
    
    private URL serverURL;
    private PluginList local = new PluginList();
    private PluginList distant = new PluginList();
    
    private final DatabaseRegistry localRegistry;
        
    public FXLauncherPane() throws IOException {
        SIRS.loadFXML(this);
        
        localRegistry = new DatabaseRegistry();
        
        errorLabel.setTextFill(Color.RED);
        uiProgressImport.visibleProperty().bindBidirectional(uiImportButton.disableProperty());
        uiProgressCreate.visibleProperty().bindBidirectional(uiCreateButton.disableProperty());
        
        final TableColumn<String,String> column = new TableColumn<>("Base de données");
        column.setCellValueFactory((TableColumn.CellDataFeatures<String, String> param) -> new SimpleObjectProperty<>(param.getValue()));
        
        uiLocalBaseTable.getColumns().clear();
        uiLocalBaseTable.getColumns().add(new DeleteColumn());
        uiLocalBaseTable.getColumns().add(column);
        uiLocalBaseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        uiLocalBaseTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        uiInstallPluginBtn.setDisable(true);
        uiDeletePluginBtn.setDisable(true);
        
        uiInstalledPlugins.getColumns().add(newNameColumn());
        uiInstalledPlugins.getColumns().add(newVersionColumn());
        uiInstalledPlugins.getColumns().add(newDescriptionColumn());
        
        uiAvailablePlugins.getColumns().add(newNameColumn());
        uiAvailablePlugins.getColumns().add(newVersionColumn());
        uiAvailablePlugins.getColumns().add(newDescriptionColumn());
        
        uiInstalledPlugins.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends PluginInfo> observable, PluginInfo oldValue, final PluginInfo newValue) -> {          
            if (newValue != null) {
                uiDeletePluginBtn.setDisable(false);
                uiDeletePluginBtn.setOnAction((ActionEvent event) -> {deletePlugin(newValue); updatePluginList(null);});
            } else {
                uiDeletePluginBtn.setDisable(true);
            }
        });        
                
        uiAvailablePlugins.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends PluginInfo> observable, PluginInfo oldValue, final PluginInfo newValue) -> {          
            if (newValue != null) {
                if (local.getPluginInfo(newValue.getName()).findAny().isPresent()) {
                    uiInstallPluginBtn.setText("Mettre à jour");
                } else {
                    uiInstallPluginBtn.setText("Installer");
                }
                uiInstallPluginBtn.setDisable(false);
                uiInstallPluginBtn.setOnAction((ActionEvent event) -> {
                    installPlugin(newValue);
                    updatePluginList(null);
                });
            } else {
                uiInstallPluginBtn.setDisable(true);
            }
        });
        
        uiProgressPlugins.visibleProperty().bind(
                uiInstallPluginBtn.armedProperty().or(uiDeletePluginBtn.armedProperty()));
        
        updateLocalDbList();
        updatePluginList(null);
        uiMajServerURL.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
                if (newValue != null && !((String)newValue).isEmpty() && !newValue.equals(oldValue)) updatePluginList(null);
            }
        });
        
        final ChangeListener<Object> accentReplacer = new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
            final String nfdText = Normalizer.normalize((String)newValue, Normalizer.Form.NFD);
            ((WritableValue)observable).setValue(nfdText.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replaceAll("\\s+", "_"));
            }
        }; 
        
        uiDistantName.textProperty().addListener(accentReplacer);
        uiImportName.textProperty().addListener(accentReplacer);
        uiNewName.textProperty().addListener(accentReplacer);
        
        try{
            final CoordinateReferenceSystem baseCrs = CRS.decode("EPSG:2154");
            uiNewCRS.crsProperty().set(baseCrs);
            uiImportCRS.crsProperty().set(baseCrs);
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        
    }
    
    /** 
     * Cherche la liste des plugins installés localement, puis ceux disponibles 
     * sur le serveur de mise à jour (si l'utilisateur a donné une URL valide).
     */
    private void updateLocalDbList(){
        final ObservableList<String> names = FXCollections.observableList(listLocalDatabases());
        uiLocalBaseTable.setItems(names);
        if(!names.isEmpty()){
            uiLocalBaseTable.getSelectionModel().select(0);
        }
        uiConnectButton.setDisable(names.isEmpty());
    }
    
    @FXML
    void updatePluginList(ActionEvent event) {
        try {
            local = PluginInstaller.listLocalPlugins();
            uiInstalledPlugins.setItems(local.plugins);
        } catch(Exception ex) {
            LOGGER.log(Level.WARNING, "Cannot update local plugin list !", ex);
            errorLabel.setText("Une erreur inattendue est survenue lors de la récupération des plugins installés.");
        }
               
        final String majURL = uiMajServerURL.getText();
        if (majURL == null || majURL.isEmpty()) return;
        try {
            serverURL = new URL(uiMajServerURL.getText());
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "Invalid plugin server URL !", e);
            errorLabel.setText("L'URL du serveur de plugins est invalide.");
            return;
        }
        
        try {
            distant = PluginInstaller.listDistantPlugins(serverURL);
            uiAvailablePlugins.setItems(distant.plugins);
        } catch(Exception ex) {
            try {
                String serverStr = serverURL.toExternalForm();
                if (!serverStr.endsWith("/")) {
                    serverStr = serverStr + "/";
                }
                distant = PluginInstaller.listDistantPlugins(
                        new URL(serverStr+DEFAULT_PLUGIN_DESCRIPTOR));
                distant.plugins.removeAll(local.plugins);
                uiAvailablePlugins.setItems(distant.plugins);
            } catch (Exception e) {
                ex.addSuppressed(e);
                LOGGER.log(Level.WARNING, "Cannot update distant plugin list !", ex);
                errorLabel.setText("Impossible de récupérer la liste des plugins disponibles.");
            }
        }
    }
        
    
    @FXML
    void connectLocal(ActionEvent event) {
        final String db = uiLocalBaseTable.getSelectionModel().getSelectedItem();
        final Window currentWindow = getScene().getWindow();
        
        if (currentWindow instanceof Stage) {
            SIRS.setLauncher((Stage) currentWindow);
        }
        currentWindow.hide();
        runDesktop(db);
    }

    @FXML
    void connectDistant(ActionEvent event) {
        if(uiDistantName.getText().trim().isEmpty()){
            new Alert(Alert.AlertType.ERROR,"Veuillez remplir le nom de la base de donnée.",ButtonType.OK).showAndWait();
            return;
        }
        
        final String distantUrl = uiDistantUrl.getText();
        localRegistry.synchronizeDatabases(distantUrl, uiDistantName.getText(), uiDistantSync.isSelected());
        
        //aller au panneau principale
        Platform.runLater(() -> {
            uiTabPane.getSelectionModel().clearAndSelect(0);
            updateLocalDbList();
        });
    }

    @FXML
    void createEmpty(ActionEvent event) {
        final String dbName = cleanDbName(uiNewName.getText());
        if(dbName.isEmpty()){
            new Alert(Alert.AlertType.ERROR,"Veuillez remplir le nom de la base de donnée.",ButtonType.OK).showAndWait();
            return;
        }
        
        if(listLocalDatabases().contains(dbName)){
            new Alert(Alert.AlertType.ERROR,"Le nom de la base de données est déjà utilisé.",ButtonType.OK).showAndWait();
            return;
        }
        
        if("".equals(uiCreatePassword.getText())
                || !uiCreatePassword.getText().equals(uiCreateConfirmPassword.getText())){
            new Alert(Alert.AlertType.ERROR,"Veuillez entrer, puis confirmer un mot de passe administrateur.",ButtonType.OK).showAndWait();
            return;
        }
        
        String epsg = "EPSG:2154";
        try{
            final int epsgNum = IdentifiedObjects.lookupEpsgCode(uiNewCRS.crsProperty().get(),true);
            epsg = "EPSG:"+epsgNum;
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        
        final String epsgCode = epsg;        
        uiCreateButton.setDisable(true);
        new Thread(() -> {
            try (ConfigurableApplicationContext applicationContext = localRegistry.connectToSirsDatabase(dbName, true, false, false)) {
                SirsDBInfoRepository sirsDBInfoRepository = applicationContext.getBean(SirsDBInfoRepository.class);
                sirsDBInfoRepository.create(epsgCode);
                final UtilisateurRepository utilisateurRepository = applicationContext.getBean(UtilisateurRepository.class);
//                final Utilisateur administrateur = utilisateurRepository.create();
//                administrateur.setValid(true);
//                administrateur.setLogin(uiCreateLogin.getText());
//                MessageDigest messageDigest = null;
//                try {
//                    messageDigest = MessageDigest.getInstance("MD5");
//                } catch (NoSuchAlgorithmException ex) {
//                    Logger.getLogger(FXLauncherPane.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                administrateur.setPassword(new String(messageDigest.digest(uiCreatePassword.getText().getBytes())));
//                administrateur.setRole(Role.ADMIN);
//                utilisateurRepository.add(administrateur);
                
                createDefaultUsers(utilisateurRepository, uiCreateLogin.getText(), uiCreatePassword.getText());
                
                
                //aller au panneau principal
                Platform.runLater(() -> {
                    uiTabPane.getSelectionModel().clearAndSelect(0);
                    updateLocalDbList();
                });
                
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                Platform.runLater(() -> {
                    GeotkFX.newExceptionDialog("La création de la base a échoué.", ex).showAndWait();
                });
            } finally {
                Platform.runLater(() -> {
                    uiCreateButton.setDisable(false);
                });
            }
        }).start();
    }
    
    
    private void createDefaultUsers(final UtilisateurRepository utilisateurRepository, final String adminLogin, final String adminPassword){
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(PASSWORD_ENCRYPT_ALGO);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(FXLauncherPane.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        final Utilisateur administrateur = ElementCreator.createAnonymValidElement(Utilisateur.class);
        administrateur.setLogin(adminLogin);
        administrateur.setPassword(new String(messageDigest.digest(adminPassword.getBytes())));
        administrateur.setRole(Role.ADMIN);
        utilisateurRepository.add(administrateur);
        
        
        final Utilisateur defaultGuest = ElementCreator.createAnonymValidElement(Utilisateur.class);
        defaultGuest.setLogin(LOGIN_DEFAULT_GUEST);
        defaultGuest.setPassword(new String(messageDigest.digest("".getBytes())));
        defaultGuest.setRole(Role.GUEST);
        utilisateurRepository.add(defaultGuest);
    }

    @FXML
    synchronized void createFromAccess(ActionEvent event) {
        final String dbName = cleanDbName(uiImportName.getText());
        if(dbName.isEmpty()){
            new Alert(Alert.AlertType.ERROR,"Veuillez remplir le nom de la base de donnée.",ButtonType.OK).showAndWait();
            return;
        }
        
        if(listLocalDatabases().contains(dbName)){
            new Alert(Alert.AlertType.ERROR,"Le nom de la base de données est déjà utilisé.",ButtonType.OK).showAndWait();
            return;
        }
        
        if("".equals(uiImportPassword.getText())
                || !uiImportPassword.getText().equals(uiImportConfirmPassword.getText())){
            new Alert(Alert.AlertType.ERROR,"Veuillez entrer, puis confirmer un mot de passe administrateur.",ButtonType.OK).showAndWait();
            return;
        }
        
        String epsg = "EPSG:2154";
        try{
            final int epsgNum = IdentifiedObjects.lookupEpsgCode(uiImportCRS.crsProperty().get(),true);
            epsg = "EPSG:"+epsgNum;
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        final String epsgCode = epsg;

        uiImportButton.setDisable(true);
        new Thread(() -> {
            try (ConfigurableApplicationContext appCtx = localRegistry.connectToSirsDatabase(dbName, true, false, false)) {
                final File mainDbFile = new File(uiImportDBData.getText());
                final File cartoDbFile = new File(uiImportDBCarto.getText());
                final CouchDbConnector couchDbConnector = appCtx.getBean(CouchDbConnector.class);
                DbImporter importer = new DbImporter(couchDbConnector);
                importer.setDatabase(DatabaseBuilder.open(mainDbFile),
                        DatabaseBuilder.open(cartoDbFile), uiImportCRS.crsProperty().get());
                importer.cleanDb();
                importer.importation();
                SirsDBInfoRepository sirsDBInfoRepository = appCtx.getBean(SirsDBInfoRepository.class);
                sirsDBInfoRepository.create(epsgCode);
                final UtilisateurRepository utilisateurRepository = appCtx.getBean(UtilisateurRepository.class);
//                final Utilisateur administrateur = utilisateurRepository.create();
//                administrateur.setValid(true);
//                administrateur.setLogin(uiImportLogin.getText());
//                MessageDigest messageDigest = null;
//                try {
//                    messageDigest = MessageDigest.getInstance("MD5");
//                } catch (NoSuchAlgorithmException ex) {
//                    Logger.getLogger(FXLauncherPane.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                administrateur.setPassword(new String(messageDigest.digest(uiImportPassword.getText().getBytes())));
//                administrateur.setRole(Role.ADMIN);
//                utilisateurRepository.add(administrateur);
                
                createDefaultUsers(utilisateurRepository, uiImportLogin.getText(), uiImportPassword.getText());

                //aller au panneau principale
                Platform.runLater(() -> {
                    uiTabPane.getSelectionModel().clearAndSelect(0);
                    updateLocalDbList();
                });

            } catch (IOException | AccessDbImporterException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                GeotkFX.newExceptionDialog("Une erreur est survenue pendant la création de la base de données.", ex).showAndWait();
            } finally {
                Platform.runLater(() -> {
                    uiImportButton.setDisable(false);
                });
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
        
    private void installPlugin(final PluginInfo input) {
        final String name = input.getName();
        Optional<PluginInfo> oldPlugin = local.getPluginInfo(name).findAny();
        if (oldPlugin.isPresent()) {
            deletePlugin(oldPlugin.get());
        }

        try {
            PluginInstaller.install(serverURL, input);
            //updatePluginList(null);
        } catch (IOException ex) {
            errorLabel.setText("Une erreur inattendue est survenue pendant l'installation du plugin " + name);
            LOGGER.log(Level.SEVERE, "Plugin " + name + " cannot be installed !", ex);
        }
    }
    
    private void deletePlugin(final PluginInfo input) {
        try {
            PluginInstaller.uninstall(input);
        } catch (Exception e) {
            errorLabel.setText("Le plugin " + input.getName() + "ne peut être désinstallé : Erreur inattendue.");
            LOGGER.log(Level.SEVERE, "Following plugin cannot be removed : " + input.getName(), e);
        }
    }
    
    
    private static String cleanDbName(String name){
        return name.trim();
    }
    
    private static void runDesktop(final String database){
        try {
            Plugins.loadPlugins();
            (SIRS.LOADER = new Loader(database)).start(null);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Cannot run desktop application with database : " + database, ex);
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
 
    private List<String> listLocalDatabases() {
        try {
            return localRegistry.listSirsDatabases();
        } catch (Exception e) {
            GeotkFX.newExceptionDialog("Impossible de lister les bases locales.", e);
            return Collections.EMPTY_LIST;
        }
    }
    
    /**
     * Permet de redémarrer l'application en cours d'éxecution.
     * @throws URISyntaxException Si l'application n'est pas démarrée en mode
     * natif, et que le chemin vers le JAR d'éxecution est corrompu.
     * @throws IOException Si un problème survient lors de l'accès à l'éxecutable
     * permettant de démarrer l'application.
     */
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
            LOGGER.log(Level.INFO, "Application directory : {0}", appDir.toString());
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
            LOGGER.log(Level.INFO, "Application executable {0}", appExec.toString());
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
    
    private static TableColumn newNameColumn() {
        final TableColumn<PluginInfo,String> colName = new TableColumn<>("Plugin");
        colName.setCellValueFactory((TableColumn.CellDataFeatures<PluginInfo, String> param) -> param.getValue().nameProperty());
        return colName;
    }
    
        
    private static TableColumn newVersionColumn() { 
        final TableColumn<PluginInfo,String> colVersion = new TableColumn<>("Version");
        colVersion.setCellValueFactory((TableColumn.CellDataFeatures<PluginInfo, String> param) -> {
            final PluginInfo info = param.getValue();
            final String version = info.getVersionMajor()+"."+info.getVersionMinor();
            return new SimpleObjectProperty<>(version);
        });
        return colVersion;
    }
    
        
    private static TableColumn newDescriptionColumn() {
        final TableColumn<PluginInfo,String> colDescription = new TableColumn<>("Description");
        colDescription.setCellValueFactory((TableColumn.CellDataFeatures<PluginInfo, String> param) -> param.getValue().descriptionProperty());
        return colDescription;
    }        
    
    
    public class DeleteColumn extends TableColumn<String,String>{

        public DeleteColumn() {
            super();            
            setSortable(false);
            setResizable(false);
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_DELETE));
            
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<String, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<String, String> param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });
            setCellFactory((TableColumn<String, String> param) -> new ButtonTableCell<>(
                    false,new ImageView(GeotkFX.ICON_DELETE), (String t) -> true, new Function<String, String>() {
                @Override
                public String apply(String t) {
                    final ButtonType res = new Alert(Alert.AlertType.CONFIRMATION,"Confirmer la suppression ?", 
                            ButtonType.NO, ButtonType.YES).showAndWait().get();
                    if(ButtonType.YES == res){
                        try {
                            deleteDatabase(t);
                        } catch (Exception ex) {
                            GeotkFX.newExceptionDialog("Impossible de supprimer "+t, ex).show();
                        }
                    }
                    return null;
                }
            }));
        }
    }
    
    protected void deleteDatabase(final String databaseName) {        
        new IdentificationStage(databaseName).showAndWait();
        updateLocalDbList();
    }
    
    private final class IdentificationStage extends Stage{
        
        private final String dbName;
        private final Button cancel;
        private final Button ok;
        private final TextField login;
        private final PasswordField password;
        private final Label label;
        
        private IdentificationStage(final String databaseName) {
            super();
            setTitle("Identification");
            this.dbName = databaseName;
            label = new Label("Veuillez vous identifier comme utilisateur de la base");
            cancel = new Button("Annuler");
            ok = new Button("Effacer la base");
            login = new TextField();
            password = new PasswordField();
            
            cancel.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    hide();
                }
            });
            
            ok.setOnAction(new IdenfificationHandler());
            password.onActionProperty().bind(ok.onActionProperty());

            final GridPane gridpane = new GridPane();
            gridpane.add(label, 0, 0, 2, 1);
            gridpane.add(new Label("Login : "), 0, 1);
            gridpane.add(login, 1, 1);
            gridpane.add(new Label("Mot de passe : "), 0, 2);
            gridpane.add(password, 1, 2);
            gridpane.add(cancel, 0, 3);
            gridpane.add(ok, 1, 3);
            gridpane.setHgap(5);
            gridpane.setVgap(5);
            gridpane.setPadding(new Insets(10));
            
            final Scene scene = new Scene(gridpane);
            setScene(scene);
            
        }
        
        private class IdenfificationHandler implements EventHandler<ActionEvent>{
            
            @Override
            public void handle(ActionEvent event) {

                try (final ConfigurableApplicationContext ctx = localRegistry.connectToSirsDatabase(dbName, false, false, false)) {

                    final UtilisateurRepository utilisateurRepository = ctx.getBean(Session.class).getUtilisateurRepository();

                    MessageDigest messageDigest = null;
                    messageDigest = MessageDigest.getInstance("MD5");

                    final List<Utilisateur> utilisateurs = utilisateurRepository.getByLogin(login.getText());
                    final String encryptedPassword = new String(messageDigest.digest(password.getText().getBytes()));
                    boolean allowedToDropDB = false;
                    for (final Utilisateur utilisateur : utilisateurs) {
                        if (encryptedPassword.equals(utilisateur.getPassword())) {
                            allowedToDropDB = true;
                            break;
                        }
                    }

                    if (allowedToDropDB) {
                        localRegistry.dropDatabase(dbName);
                        hide();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Échec d'identification.", ButtonType.CLOSE).showAndWait();
                    }

                } catch (Exception ex) {
                   throw new SirsCoreRuntimeExecption(ex);
                }
            }
        }
    }
}
