package fr.sirs.launcher;

import com.sun.javafx.PlatformUtil;
import fr.sirs.AbstractRestartableStage;
import fr.sirs.Loader;
import fr.sirs.Plugin;
import fr.sirs.PluginInfo;
import fr.sirs.Plugins;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.binaryMD5;
import static fr.sirs.SIRS.hexaMD5;
import fr.sirs.Session;
import fr.sirs.core.CacheRules;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.authentication.AuthenticationWallet;
import fr.sirs.core.component.DatabaseRegistry;
import fr.sirs.core.component.SirsDBInfoRepository;
import fr.sirs.core.component.UtilisateurRepository;
import fr.sirs.core.model.Role;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.core.plugins.PluginLoader;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.maj.PluginInstaller;
import fr.sirs.maj.PluginList;
import fr.sirs.maj.PluginsDownloadManager;
import fr.sirs.util.FXAuthenticationWalletEditor;
import fr.sirs.util.SimpleButtonColumn;
import fr.sirs.util.property.SirsPreferences;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
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
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.apache.sis.util.logging.Logging;
import org.ektorp.DbAccessException;
import org.ektorp.ReplicationStatus;
import org.geotoolkit.gui.javafx.crs.FXCRSButton;
import org.geotoolkit.gui.javafx.util.TaskManager;
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

    private static final Logger LOGGER = Logging.getLogger("fr.sirs");

    /**
     * Si le serveur de plugins ne pointe pas sur une liste valide de
     * {@link PluginInfo} serialisés, on essaie d'atteindre le fichier suivant.
     */
    private static final String DEFAULT_PLUGIN_DESCRIPTOR = "plugins.json";

    @FXML
    private Label errorLabel;
    @FXML
    private Label uiRestartLbl;
    @FXML
    private TabPane uiTabPane;
    @FXML
    private Tab uiLocalTab;
    @FXML
    private Tab uiDistantTab;
    @FXML
    private Tab uiCreateTab;
    @FXML
    private Tab uiImportTab;
    @FXML
    private Tab uiWalletTab;

    // onglet base locales
    @FXML
    private TableView<String> uiLocalBaseTable;
    @FXML
    private Button uiConnectButton;

    // onglet base distantes
    @FXML
    private TextField uiDistantName;
    @FXML
    private TextField uiDistantUrl;
    @FXML
    private CheckBox uiDistantSync;
    @FXML
    private VBox uiSynchroRunning;

    // onglet base creation
    @FXML
    private TextField uiNewName;
    @FXML
    private FXCRSButton uiNewCRS;
    @FXML
    private ProgressBar uiProgressCreate;
    @FXML
    private Button uiCreateButton;
    @FXML
    private TextField uiCreateLogin;
    @FXML
    private PasswordField uiCreatePassword;
    @FXML
    private PasswordField uiCreateConfirmPassword;

    // onglet base importation
    @FXML private TextField uiImportName;
    @FXML private FXCRSButton uiImportCRS;
    @FXML private TextField uiImportDBData;
    @FXML private TextField uiImportDBCarto;
    @FXML private ProgressBar uiProgressImport;
    @FXML private Label uiImportInfo;
    @FXML private Button uiImportButton;
    @FXML private TextField uiImportLogin;
    @FXML private PasswordField uiImportPassword;
    @FXML private PasswordField uiImportConfirmPassword;

    // onglet mise à jour
    @FXML
    private TextField uiMajServerURL;
    @FXML
    private TableView<PluginInfo> uiInstalledPlugins;
    @FXML
    private TableView<PluginInfo> uiAvailablePlugins;

    @FXML
    private Button uiInstallPluginBtn;
    @FXML
    private Button uiDeletePluginBtn;
    @FXML
    private Button uiRestartAppBtn;

    @FXML
    private Label uiDlLbl;
    @FXML
    private ProgressBar uiProgressPlugins;

    private URL serverURL;
    private PluginList local = new PluginList();
    private PluginList distant = new PluginList();

    private final DatabaseRegistry localRegistry;

    private final DatabaseNameFormatter dbNameFormat = new DatabaseNameFormatter();

    public FXLauncherPane() throws IOException {
        SIRS.loadFXML(this);

        localRegistry = new DatabaseRegistry();

        errorLabel.setTextFill(Color.RED);
        errorLabel.visibleProperty().bind(errorLabel.textProperty().isNotEmpty());
        uiRestartLbl.visibleProperty().bind(uiRestartAppBtn.visibleProperty().and(errorLabel.visibleProperty().not()));

        uiProgressImport.visibleProperty().bindBidirectional(uiImportButton.disableProperty());
        uiProgressCreate.visibleProperty().bindBidirectional(uiCreateButton.disableProperty());
        uiSynchroRunning.visibleProperty().bind(uiDistantTab.getContent().disabledProperty());
        uiSynchroRunning.managedProperty().bind(uiSynchroRunning.visibleProperty());

        final TableColumn<String, String> column = new TableColumn<>("Base de données");
        column.setCellValueFactory((TableColumn.CellDataFeatures<String, String> param) -> new SimpleObjectProperty<>(param.getValue()));

        uiLocalBaseTable.getColumns().clear();
        uiLocalBaseTable.getColumns().add(new DeleteColumn());
        uiLocalBaseTable.getColumns().add(new CopyColumn());
        uiLocalBaseTable.getColumns().add(column);
        uiLocalBaseTable.getColumns().add(new SynchronizationColumn(localRegistry));
        uiLocalBaseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        uiLocalBaseTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        uiMajServerURL.setText(SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.UPDATE_PLUGINS_URL));
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
                uiDeletePluginBtn.setOnAction((ActionEvent event) -> {
                    restartApplicationNeeded();
                    deletePlugin(newValue);
                    if (!PluginsDownloadManager.INSTANCE.hasPendingDl()) {
                        uiDlLbl.setText("");
                    }
                    updatePluginList(null);
                });
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
                    restartApplicationNeeded();
                    uiProgressPlugins.setVisible(true);

                    try {
                        final URL bundleURL = newValue.bundleURL(serverURL);
                        final double pluginSizeMo = bundleURL.openConnection().getContentLengthLong() / 1E6;
                        final Task installTask = new TaskManager.MockTask(new Thread(() -> {
                            installPlugin(newValue);
                        }));
                        PluginsDownloadManager.INSTANCE.addToDlQueue(installTask, pluginSizeMo);

                        installTask.setOnSucceeded(e -> {
                            PluginsDownloadManager.INSTANCE.finished(installTask);
                            Platform.runLater(() -> {
                                if (!PluginsDownloadManager.INSTANCE.hasPendingDl()) {
                                    uiProgressPlugins.setVisible(false);
                                    updatePluginList(null);
                                }
                            });
                        });
                        installTask.setOnFailed(e -> {
                            PluginsDownloadManager.INSTANCE.finished(installTask);
                            Platform.runLater(() -> {
                                if (!PluginsDownloadManager.INSTANCE.hasPendingDl()) {
                                    uiProgressPlugins.setVisible(false);
                                    updatePluginList(null);
                                }
                            });
                        });
                    } catch (IOException ex) {
                        LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                        GeotkFX.newExceptionDialog("Erreur à l'installation d'un plugin", ex);
                    }
                });
            } else {
                uiInstallPluginBtn.setDisable(true);
            }
        });

        uiDlLbl.textProperty().bindBidirectional(PluginsDownloadManager.INSTANCE.txtProperty());
        uiProgressPlugins.setVisible(false);

        uiRestartAppBtn.setOnAction((ActionEvent event) -> {
            try {
                restartCore();
            } catch (URISyntaxException | IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        });

        updateLocalDbList();
        updatePluginList(null);
        uiMajServerURL.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
                if (newValue != null && !((String) newValue).isEmpty() && !newValue.equals(oldValue)) {
                    updatePluginList(null);
                }
            }
        });

        uiDistantName.textProperty().addListener(dbNameFormat);
        uiImportName.textProperty().addListener(dbNameFormat);
        uiNewName.textProperty().addListener(dbNameFormat);

        try {
            final CoordinateReferenceSystem baseCrs = CRS.decode("EPSG:2154");
            uiNewCRS.crsProperty().set(baseCrs);
            uiImportCRS.crsProperty().set(baseCrs);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        AuthenticationWallet wallet = AuthenticationWallet.getDefault();
        if (wallet == null ) {
            uiTabPane.getTabs().remove(uiWalletTab);
        } else {
            uiWalletTab.setContent(new FXAuthenticationWalletEditor(wallet));
        }
    }

    private void restartApplicationNeeded() {
        uiRestartAppBtn.setVisible(true);
        uiLocalTab.setDisable(true);
        uiDistantTab.setDisable(true);
        uiCreateTab.setDisable(true);
        uiImportTab.setDisable(true);
        uiWalletTab.setDisable(true);
    }

    /**
     * Cherche la liste des plugins installés localement, puis ceux disponibles
     * sur le serveur de mise à jour (si l'utilisateur a donné une URL valide).
     */
    private void updateLocalDbList() {
        final ObservableList<String> names = FXCollections.observableList(listLocalDatabases());
        uiLocalBaseTable.setItems(names);
        if (!names.isEmpty()) {
            uiLocalBaseTable.getSelectionModel().select(0);
        }
        uiConnectButton.setDisable(names.isEmpty());
    }

    @FXML
    void updatePluginList(ActionEvent event) {
        try {
            local = PluginInstaller.listLocalPlugins();
            uiInstalledPlugins.setItems(local.plugins);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Cannot update local plugin list !", ex);
            errorLabel.setText("Une erreur inattendue est survenue lors de la récupération des plugins installés.");
        }

        final String majURL = uiMajServerURL.getText();
        if (majURL == null || majURL.isEmpty()) {
            return;
        }
        try {
            serverURL = new URL(uiMajServerURL.getText());
        } catch (MalformedURLException e) {
            LOGGER.log(Level.FINE, "Invalid plugin server URL !", e);
            errorLabel.setText("L'URL du serveur de plugins est invalide.");
            return;
        }

        try {
            distant = PluginInstaller.listDistantPlugins(serverURL);
            distant.plugins.removeAll(local.plugins);
            uiAvailablePlugins.setItems(distant.plugins);
        } catch (Exception ex) {
            try {
                String serverStr = serverURL.toExternalForm();
                if (!serverStr.endsWith("/")) {
                    serverStr = serverStr + "/";
                }
                distant = PluginInstaller.listDistantPlugins(
                        new URL(serverStr + DEFAULT_PLUGIN_DESCRIPTOR));
                distant.plugins.removeAll(local.plugins);
                uiAvailablePlugins.setItems(distant.plugins);
            } catch (Exception e) {
                ex.addSuppressed(e);
                LOGGER.log(Level.FINE, "Cannot update distant plugin list !", ex);
                errorLabel.setText("Impossible de récupérer la liste des plugins disponibles.");
            }
        }
    }

    @FXML
    void connectLocal(ActionEvent event) {
        final String db = uiLocalBaseTable.getSelectionModel().getSelectedItem();
        final Window currentWindow = getScene().getWindow();

        if (currentWindow instanceof Stage) {
            final RestartableStage restartableStage = new RestartableStage((Stage) currentWindow);
            SIRS.setLauncher(restartableStage);
        }
        currentWindow.hide();
        runDesktop(db, localRegistry);
    }

    @FXML
    void connectDistant(ActionEvent event) {
        if (uiDistantName.getText().trim().isEmpty()) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez remplir le nom de la base de donnée.", ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
            return;
        }

        final String distantUrl = uiDistantUrl.getText();
        final Task t = new TaskManager.MockTask("", () -> {
            localRegistry.synchronizeSirsDatabases(distantUrl, uiDistantName.getText(), uiDistantSync.isSelected());
            return true;
        });

        t.runningProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> uiDistantTab.getContent().setDisable(true));
            } else {
                Platform.runLater(() -> uiDistantTab.getContent().setDisable(false));
            }
        });

        t.setOnSucceeded(evt -> Platform.runLater(() -> {
            //aller au panneau principal
            updateLocalDbList();
            uiTabPane.getSelectionModel().clearAndSelect(0);
        }));

        t.setOnCancelled(evt -> Platform.runLater(() -> {
            new Alert(AlertType.WARNING, "Synchronisation annulée").show();
        }));

        t.setOnFailed(evt -> Platform.runLater(() -> {
            final Throwable ex = t.getException();
            if (ex instanceof DbAccessException) {
                LOGGER.log(Level.WARNING, "Problème d'accès à CouchDB", ex);
                GeotkFX.newExceptionDialog("Impossible d'accéder à la base de donnée demandée. Veuillez vérifier l'URL de la base distante saisie."
                        + "Si le problème persiste, contactez votre administrateur.", ex).showAndWait();
            } else {
                LOGGER.log(Level.WARNING, "Impossible de synchroniser deux bases de données.", ex);
                GeotkFX.newExceptionDialog("Impossible de synchroniser les bases de données.", ex).show();
            }
        }));

        TaskManager.INSTANCE.submit(t);
    }

    @FXML
    void createEmpty(ActionEvent event) {
        final String dbName = cleanDbName(uiNewName.getText());
        if (dbName.isEmpty()) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez remplir le nom de la base de donnée.", ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
            return;
        }

        if (listLocalDatabases().contains(dbName)) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, "Le nom de la base de données est déjà utilisé.", ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
            return;
        }

        if ("".equals(uiCreatePassword.getText())
                || !uiCreatePassword.getText().equals(uiCreateConfirmPassword.getText())) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez entrer, puis confirmer un mot de passe administrateur.", ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
            return;
        }

        String epsg = "EPSG:2154";
        try {
            final int epsgNum = IdentifiedObjects.lookupEpsgCode(uiNewCRS.crsProperty().get(), true);
            epsg = "EPSG:" + epsgNum;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        final String epsgCode = epsg;
        uiCreateButton.setDisable(true);
        TaskManager.INSTANCE.submit("Création d'une base de données vide", () -> {
            try (ConfigurableApplicationContext applicationContext = localRegistry.connectToSirsDatabase(dbName, true, false, false)) {
                SirsDBInfoRepository sirsDBInfoRepository = applicationContext.getBean(SirsDBInfoRepository.class);
                sirsDBInfoRepository.setSRID(epsgCode);

                applicationContext.getBean(Session.class).createUser(uiCreateLogin.getText(), uiCreatePassword.getText(), Role.ADMIN);

                //aller au panneau principal
                Platform.runLater(() -> {
                    uiTabPane.getSelectionModel().clearAndSelect(0);
                    updateLocalDbList();
                });
            } catch (DbAccessException ex) {
                LOGGER.log(Level.WARNING, "Problème d'accès au CouchDB, utilisateur n'ayant pas les droits administrateur.", ex);
                GeotkFX.newExceptionDialog("L'utilisateur de la base CouchDB n'a pas les bons droits. " +
                        "Réinstaller CouchDB ou supprimer cet utilisateur \"geouser\" des administrateurs de CouchDB, " +
                        "puis relancer l'application.", ex).showAndWait();
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
        });
    }

    @FXML
    synchronized void createFromAccess(ActionEvent event) {
        final String dbName = cleanDbName(uiImportName.getText());
        if (dbName.isEmpty()) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez remplir le nom de la base de données.", ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
            return;
        }

        if (listLocalDatabases().contains(dbName)) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, "Le nom de la base de données est déjà utilisé.", ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
            return;
        }

        if ("".equals(uiImportPassword.getText())
                || !uiImportPassword.getText().equals(uiImportConfirmPassword.getText())) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez entrer, puis confirmer un mot de passe administrateur.", ButtonType.OK);
            alert.setResizable(true);
            alert.showAndWait();
            return;
        }

        String epsg = "EPSG:2154";
        try {
            final int epsgNum = IdentifiedObjects.lookupEpsgCode(uiImportCRS.crsProperty().get(), true);
            epsg = "EPSG:" + epsgNum;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        final String epsgCode = epsg;

        final File mainDbFile = new File(uiImportDBData.getText());
        final File cartoDbFile = new File(uiImportDBCarto.getText());

        if (!mainDbFile.isFile()) {
            final Alert alert = new Alert(AlertType.ERROR, "Le fichier de base de donnée suivant est illisible : " + mainDbFile.getAbsolutePath(), ButtonType.OK);
            alert.setResizable(true);
            alert.show();
        }

        if (!cartoDbFile.isFile()) {
            final Alert alert = new Alert(AlertType.ERROR, "Le fichier de base de donnée suivant est illisible : " + cartoDbFile.getAbsolutePath(), ButtonType.OK);
            alert.setResizable(true);
            alert.show();
        }

        uiImportButton.setDisable(true);
        TaskManager.INSTANCE.submit(() -> {
            CacheRules.cacheAllDocs.set(true);
            final ClassLoader scl = ClassLoader.getSystemClassLoader();
            if (scl instanceof PluginLoader) {
                try {
                    ((PluginLoader) scl).loadPlugins();
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                    GeotkFX.newExceptionDialog("Une erreur est survenue pendant le chargement de plugins.", ex).showAndWait();
                    return;
                }
            }
            try (ConfigurableApplicationContext appCtx = localRegistry.connectToSirsDatabase(dbName, true, false, false)) {
                final DbImporter importer = new DbImporter(appCtx);
                Task<Boolean> importTask = TaskManager.INSTANCE.submit(importer.importation(
                        mainDbFile, cartoDbFile, uiImportCRS.crsProperty().get(), uiImportLogin.getText(), uiImportPassword.getText()));
                uiProgressImport.progressProperty().bind(importTask.progressProperty());

                importTask.get();
                // Opérations ultérieures à l'importation à réaliser par les plugins.
                // Should initialize most of couchdb views
                for (final Plugin p : Plugins.getPlugins()) {
                    try {
                        p.afterImport();
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                    }
                }

                //aller au panneau principal
                Platform.runLater(() -> {
                    uiTabPane.getSelectionModel().clearAndSelect(0);
                    updateLocalDbList();
                });

            } catch (IOException | AccessDbImporterException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                GeotkFX.newExceptionDialog("Une erreur est survenue pendant la création de la base de données.", ex).showAndWait();
            } catch (DbAccessException ex) {
                LOGGER.log(Level.WARNING, "Problème d'accès au CouchDB, utilisateur n'ayant pas les droits administrateur.", ex);
                GeotkFX.newExceptionDialog("L'utilisateur de la base CouchDB n'a pas les bons droits. "
                        + "Réinstaller CouchDB ou supprimer cet utilisateur \"geouser\" des administrateurs de CouchDB, "
                        + "puis relancer l'application.", ex).showAndWait();
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                GeotkFX.newExceptionDialog("Une erreur est survenue pendant l'import de la base.", ex).showAndWait();
            } finally {
                CacheRules.cacheAllDocs.set(false);
                Platform.runLater(() -> uiImportButton.setDisable(false));
            }
        });
    }

    @FXML
    void chooseMainDb(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        final File prevPath = getPreviousPath();
        if (prevPath != null) {
            fileChooser.setInitialDirectory(prevPath);
        }
        final File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
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
        if (file != null) {
            setPreviousPath(file.getParentFile());
            uiImportDBCarto.setText(file.getAbsolutePath());
        }
    }

    private void installPlugin(final PluginInfo pluginInfo) {
        final String name = pluginInfo.getName();
        Optional<PluginInfo> oldPlugin = local.getPluginInfo(name).findAny();
        if (oldPlugin.isPresent()) {
            deletePlugin(oldPlugin.get());
        }

        try {
            PluginInstaller.install(serverURL, pluginInfo);
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

    private static String cleanDbName(String name) {
        return name.trim();
    }

    private static void runDesktop(final String database, final DatabaseRegistry localRegistry) {
        try {
            (SIRS.LOADER = new Loader(database, localRegistry)).start(null);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Cannot run desktop application with database : " + database, ex);
            final Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
            alert.setResizable(true);
            alert.showAndWait();
        }
    }

    private static File getPreviousPath() {
        final Preferences prefs = Preferences.userNodeForPackage(FXLauncherPane.class);
        final String str = prefs.get("path", null);
        if (str != null) {
            final File file = new File(str);
            if (file.isDirectory()) {
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
     *
     * @throws URISyntaxException Si l'application n'est pas démarrée en mode
     * natif, et que le chemin vers le JAR d'éxecution est corrompu.
     * @throws IOException Si un problème survient lors de l'accès à
     * l'éxecutable permettant de démarrer l'application.
     */
    private static void restartCore() throws URISyntaxException, IOException {
        final List<String> args = new ArrayList<>();
        final Pattern jarPattern = Pattern.compile("(?i).*\\.jar");

        /**
         * First, we check if application has been deployed through native package.
         * If it's the case, we should find ths class jar location, and retrieve
         * application executable in the parent directory. Otherwise, we'll try
         * to use the java machine from which this process has been launched, to
         * restart java process.
         */
        final Optional<Path> nativeExec;
        final URL location = FXLauncherPane.class.getProtectionDomain().getCodeSource().getLocation();
        if (location != null) {
            final Path jarPath = Paths.get(location.toURI());
            final Path appDir = jarPath.getParent().getParent();

            LOGGER.log(Level.INFO, "Application directory : {0}", appDir.toString());
            // we seek for a sirs + something executable file which is not the
            // uninstaller.
            final String execName;
            if (PlatformUtil.isWindows()) {
                execName = "sirs2.exe";
            } else {
                execName = "sirs2";
            }
            nativeExec = Files.walk(appDir, 2).filter(path -> {
                final String str = path.getFileName().toString();
                return str.equalsIgnoreCase(execName)
                        && Files.isExecutable(path)
                        && Files.isRegularFile(path); // Check needed, cause a directory can be marked executable...
            }).findFirst();
        } else {
            nativeExec = Optional.empty();
        }

        if (nativeExec.isPresent()) {
            LOGGER.log(Level.INFO, "Application executable {0}", nativeExec.get().toString());
            args.add(nativeExec.get().toString());

        } else {
            // No native executable, we try to find java executable.
            final Path javaHome = Paths.get(System.getProperty("java.home"));
            final Path javaBin = javaHome.resolve("bin").resolve("java").toAbsolutePath();

            if (Files.isExecutable(javaBin)) {
                args.add(javaBin.toAbsolutePath().toString());
            } else {
                throw new IOException("No executable file can be found to restart SIRS application.");
            }

            String command = System.getProperty("sun.java.command");
            /* If java command has not been saved (which is really unlikely to
             * happen), we must retrieve the Launcher application context (jar
             * or class).
             */
            if (command == null || command.isEmpty()) {
                // Try to find jar file from code source location
                final String applJar;
                if (location !=null) {
                    applJar = Paths.get(location.toURI()).toAbsolutePath().toString();
                } else {
                    applJar = null;
                }

                // If we cannot retrieve jar file, we just put class name as command (IDE case).
                if (applJar != null && jarPattern.matcher(applJar).matches()) {
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
            args.add("-Djava.system.class.loader=fr.sirs.core.plugins.PluginLoader");

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
        final TableColumn<PluginInfo, String> colName = new TableColumn<>("Plugin");
        colName.setCellValueFactory((TableColumn.CellDataFeatures<PluginInfo, String> param) ->
                (param.getValue().getTitle() != null) ? param.getValue().titleProperty() : param.getValue().nameProperty());
        return colName;
    }

    private static TableColumn newVersionColumn() {
        final TableColumn<PluginInfo, String> colVersion = new TableColumn<>("Version");
        colVersion.setCellValueFactory((TableColumn.CellDataFeatures<PluginInfo, String> param) -> {
            final PluginInfo info = param.getValue();
            final String version = info.getVersionMajor() + "." + info.getVersionMinor();
            return new SimpleObjectProperty<>(version);
        });
        return colVersion;
    }

    private static TableColumn newDescriptionColumn() {
        final TableColumn<PluginInfo, String> colDescription = new TableColumn<>("Description");
        colDescription.setCellValueFactory((TableColumn.CellDataFeatures<PluginInfo, String> param) -> param.getValue().descriptionProperty());
        return colDescription;
    }

    protected void deleteDatabase(final String databaseName) {
        new IdentificationStage(databaseName).showAndWait();
        updateLocalDbList();
    }

    /*
     * INNER CLASSES
     */
    /**
     * A simple column which contains a button to allow suppresssion of a local
     * database.
     */
    public class DeleteColumn extends SimpleButtonColumn<String, String> {

        public DeleteColumn() {
            super(GeotkFX.ICON_DELETE,
                    new Callback<CellDataFeatures<String, String>, ObservableValue<String>>() {
                        @Override
                        public ObservableValue<String> call(CellDataFeatures<String, String> param) {
                            return new SimpleObjectProperty<>(param.getValue());
                        }
                    },
                    new Predicate<String>() {
                        @Override
                        public boolean test(String t) {
                            return t != null && !t.isEmpty();
                        }
                    }, new Function<String, String>() {

                        public String apply(String toDelete) {
                            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la suppression ?",
                                    ButtonType.NO, ButtonType.YES);
                            alert.setResizable(true);

                            if (ButtonType.YES == alert.showAndWait().get()) {
                                try {
                                    deleteDatabase(toDelete);
                                } catch (Exception ex) {
                                    GeotkFX.newExceptionDialog("Impossible de supprimer " + toDelete, ex).show();
                                }
                            }
                            return toDelete;
                        }
                    },
                    "Supprimer la base locale."
            );
        }
    }

    /**
     * A table column offering to copy a local database to another.
     */
    private final class CopyColumn extends SimpleButtonColumn<String, String> {

        public CopyColumn() {
            super(GeotkFX.ICON_DUPLICATE,
                    (TableColumn.CellDataFeatures<String, String> param) -> new SimpleObjectProperty<>(param.getValue()),
                    (String t) -> true,
                    new Function<String, String>() {
                        public String apply(String sourceDb) {
                            final TextInputDialog nameChoice = new TextInputDialog();
                            nameChoice.getEditor().textProperty().addListener(dbNameFormat);
                            nameChoice.setHeaderText("Veuillez donner un nom pour la base de destination.");
                            Optional<String> result = nameChoice.showAndWait();

                            if (result.isPresent() && !result.get().isEmpty()) {
                                final String destDbName = result.get();

                                final Alert alert = new Alert(
                                        Alert.AlertType.WARNING,
                                        "Vous allez ajouter vos données dans une base DÉJÀ EXISTANTE. Êtes-vous sûr ?",
                                        ButtonType.NO, ButtonType.YES);

                                // TODO : Remove when jvm will be fixed to give a proper default size.
                                alert.setHeight(300);
                                alert.setWidth(400);
                                alert.setResizable(true);

                                if (!localRegistry.listSirsDatabases().contains(destDbName)
                                || ButtonType.YES.equals(alert.showAndWait().get())) {

                                    try {
                                        final ReplicationStatus copyStatus = localRegistry.copyDatabase(sourceDb, destDbName);
                                        if (!copyStatus.isOk()) {
                                            localRegistry.cancelCopy(copyStatus);
                                            final Alert alerte = new Alert(
                                                    Alert.AlertType.WARNING,
                                                    "Un problème est survenu pendant la copie. Certaines données pourraient ne pas avoir été copiées.",
                                                    ButtonType.NO, ButtonType.YES);
                                            alerte.setResizable(true);
                                            alerte.show();
                                        }
                                        updateLocalDbList();
                                    } catch (Exception ex) {
                                        LOGGER.log(Level.WARNING, "Database copy from " + sourceDb + " to " + destDbName + " failed", ex);
                                        GeotkFX.newExceptionDialog("Impossible de copier les bases de données.", ex).show();
                                    }
                                }
                            }
                            return "";
                        }
                    },
                    "Copier la base locale (pas de synchronisation)."
            );
        }
    }

    private final class IdentificationStage extends Stage {

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

        private class IdenfificationHandler implements EventHandler<ActionEvent> {

            @Override
            public void handle(ActionEvent event) {

                try (final ConfigurableApplicationContext ctx = localRegistry.connectToSirsDatabase(dbName, false, false, false)) {

                    final UtilisateurRepository utilisateurRepository = (UtilisateurRepository) ctx.getBean(Session.class).getRepositoryForClass(Utilisateur.class);

                    final List<Utilisateur> utilisateurs = utilisateurRepository.getByLogin(login.getText());
                    final String encryptedPassword = hexaMD5(password.getText());
                    boolean allowedToDropDB = false;
                    for (final Utilisateur utilisateur : utilisateurs) {
                        if (encryptedPassword.equals(utilisateur.getPassword())) {
                            allowedToDropDB = true;
                            break;
                        }
                    }

                    // Disposition transitoire destinée à permettre l'effacement des bases pour une personne dont le mot de passe a précédemment été enregistré en binaire.
                    // Seulement en cas d'échec de l'indentification en hexadécimal
                    final String binaryEncryptedPassword = binaryMD5(password.getText());
                    if (!allowedToDropDB) {
                        for (final Utilisateur utilisateur : utilisateurs) {
                            if (binaryEncryptedPassword.equals(utilisateur.getPassword())) {
                                allowedToDropDB = true;
                                break;
                            }
                        }
                    }

                    if (allowedToDropDB) {
                        localRegistry.dropDatabase(dbName);
                        hide();
                    } else {
                        final Alert alert = new Alert(Alert.AlertType.ERROR, "Échec d'identification.", ButtonType.CLOSE);
                        alert.setResizable(true);
                        alert.showAndWait();
                    }

                } catch (DbAccessException ex) {
                    LOGGER.log(Level.SEVERE, "Problème d'accès au CouchDB, utilisateur n'ayant pas les droits administrateur.", ex);
                    GeotkFX.newExceptionDialog("L'utilisateur de la base CouchDB n'a pas les bons droits. " +
                            "Réinstaller CouchDB ou supprimer cet utilisateur \"geouser\" des administrateurs de CouchDB, " +
                            "puis relancer l'application.", ex).showAndWait();
                } catch (Exception ex) {
                    throw new SirsCoreRuntimeException(ex);
                }
            }
        }
    }

    /**
     * Rewrite text to replace or remove all unsupported characters in database name.
     * Multiple steps :
     * - Replace all letters with accent by their equivalent without accent
     * - Ensure database name starts with a letter
     * - Replace all spacing characters with an underscore
     * - Remove all other non-standard characters
     * - convert to lower case.
     *
     * See {@link https://wiki.apache.org/couchdb/HTTP_database_API#Naming_and_Addressing}
     */
    private static class DatabaseNameFormatter implements ChangeListener<String> {
        @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null) return;
                final String nfdText = Normalizer.normalize(newValue, Normalizer.Form.NFD);
                ((WritableValue) observable).setValue(nfdText
                        .replaceFirst("^[^A-Za-z]+", "")
                        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                        .replaceAll("\\s+", "_")
                        .replaceAll("[^\\w\\d_\\-\\$+\\(\\)]", "")
                        .toLowerCase()
                );
            }
    }

    private final class RestartableStage extends AbstractRestartableStage {
        public RestartableStage(Stage stage) {
            super(stage);
        }

        @Override
        public void restart() {
            try {
                restartCore();
            } catch (URISyntaxException | IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }
}
