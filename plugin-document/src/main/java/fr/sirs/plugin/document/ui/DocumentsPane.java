
package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.SirsDBInfo;
import fr.sirs.plugin.document.DocumentManagementTheme;
import fr.sirs.plugin.document.FileTreeItem;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.util.FileUtilities;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.component.SirsDBInfoRepository;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.TronconDigue;
        
import static fr.sirs.plugin.document.PropertiesFileUtilities.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javafx.scene.control.Alert;

/**
 *
 * @author guilhem
 */
public class DocumentsPane extends GridPane {
    
    @FXML
    private Button importDocButton;

    @FXML
    private Button deleteDocButton;

    @FXML
    private Button setFolderButton;

    @FXML
    private TreeTableView<File> tree1;

    @FXML
    private Button addDocButton;

    @FXML
    private Button addFolderButton;

    @FXML
    private Button listButton;
    
    
    private static final Image ADDF_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/add_folder.png"));
    private static final Image ADDD_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/add_doc.png"));
    private static final Image IMP_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/import.png"));
    private static final Image DEL_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/remove.png"));
    private static final Image SET_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/set.png"));
    private static final Image LIST_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/list.png"));
    
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
    
    public static final String UNCLASSIFIED = "Non classés";
    public static final String SAVE_FOLDER  = "Sauvegarde";
    public static final String DOCUMENT_FOLDER = "Dossier d'ouvrage";
    public static final String ROOT_FOLDER = "symadrem.root.foler";
    
    private static final Logger LOGGER = Logging.getLogger(DocumentsPane.class);
    
    private String rootPath;
    
    public DocumentsPane() {
        SIRS.loadFXML(this, DocumentsPane.class);
        Injector.injectDependencies(this);
        
        addFolderButton.setGraphic(new ImageView(ADDF_BUTTON_IMAGE));
        importDocButton.setGraphic(new ImageView(IMP_BUTTON_IMAGE));
        deleteDocButton.setGraphic(new ImageView(DEL_BUTTON_IMAGE));
        setFolderButton.setGraphic(new ImageView(SET_BUTTON_IMAGE));
        addDocButton.setGraphic(new ImageView(ADDD_BUTTON_IMAGE));
        listButton.setGraphic(new ImageView(LIST_BUTTON_IMAGE));
        
        // Name column
        tree1.getColumns().get(0).setEditable(false);
        tree1.getColumns().get(0).setCellValueFactory(new Callback() {
            @Override
            public ObservableValue call(Object param) {
                final File f = (File) ((CellDataFeatures)param).getValue().getValue();
                final String name;
                if (getIsModelFolder(f)) {
                    name = getLibelle(f);
                } else {
                    name = f.getName();
                }
                return new SimpleStringProperty(name);
            }
        });
        
        // Date column
        tree1.getColumns().get(1).setEditable(false);
        tree1.getColumns().get(1).setCellValueFactory(new Callback() {
            @Override
            public ObservableValue call(Object param) {
                final File f = (File) ((CellDataFeatures)param).getValue().getValue();
                synchronized (DATE_FORMATTER) {
                    return new SimpleStringProperty(DATE_FORMATTER.format(new Date(f.lastModified())));
                }
            }
        });
        
        // Size column
        tree1.getColumns().get(2).setEditable(false);
        tree1.getColumns().get(2).setCellValueFactory(new Callback() {
            @Override
            public ObservableValue call(Object param) {
                final File f = (File) ((CellDataFeatures)param).getValue().getValue();
                return new SimpleStringProperty(getStringSizeFile(f));
            }
        });
        
        // Inventory number column
        tree1.getColumns().get(3).setEditable(false);
        tree1.getColumns().get(3).setCellValueFactory(new Callback() {
            @Override
            public ObservableValue call(Object param) {
                final File f = (File) ((CellDataFeatures)param).getValue().getValue();
                return new SimpleStringProperty(getInventoryNumber(f));
            }
        });
        
        // class place column
        tree1.getColumns().get(4).setEditable(false);
        tree1.getColumns().get(4).setCellValueFactory(new Callback() {
            @Override
            public ObservableValue call(Object param) {
                final File f = (File) ((CellDataFeatures)param).getValue().getValue();
                return new SimpleStringProperty(getClassPlace(f));
            }
        });
        
        // do integrated column
        tree1.getColumns().get(5).setCellValueFactory(new Callback() {
            @Override
            public ObservableValue call(Object param) {
                final File f = (File) ((CellDataFeatures)param).getValue().getValue();
                return new SimpleObjectProperty(f);
            }
        });
        
        
        tree1.getColumns().get(5).setCellFactory(new Callback() {
            @Override
            public TreeTableCell call(Object param) {
                return new DOIntegatedCell();
            }
        });
        tree1.setShowRoot(false);
        
        final Preferences prefs = Preferences.userRoot().node(getClass().getName());
        rootPath = prefs.get(ROOT_FOLDER, null);
        
        if (rootPath != null && verifyDatabaseVersion(new File(rootPath))) {
            updateRoot();
            setDatabaseIdentifier(new File(rootPath), getDatabaseIdentifier());
           
        } else {
            importDocButton.disableProperty().set(true);
            deleteDocButton.disableProperty().set(true);
            addDocButton.disableProperty().set(true);
            addFolderButton.disableProperty().set(true);
            listButton .disableProperty().set(true);
        }
    }
    
    @FXML
    public void showImportDialog(ActionEvent event) throws IOException {
        final Dialog dialog    = new Dialog();
        final DialogPane pane  = new DialogPane();
        final ImportPane ipane = new ImportPane();
        pane.setContent(ipane);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Import de document");

        final Optional opt = dialog.showAndWait();
        if(opt.isPresent() && ButtonType.OK.equals(opt.get())){
            File f = new File(ipane.fileField.getText());
            final File directory = getSelectedFile();
            if (directory != null && directory.isDirectory()) {
                final File newFile = new File(directory, f.getName());
                FileUtilities.copy(f, newFile);
                setInventoryNumber(newFile, ipane.inventoryNumField.getText());
                setClassPlace(newFile, ipane.classPlaceField.getText());
                
                // refresh tree
                updateRoot();
            }
        }
    }
    
    @FXML
    public void showRemoveDialog(ActionEvent event) {
        final Dialog dialog    = new Dialog();
        final DialogPane pane  = new DialogPane();
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Detruire document");
        dialog.setContentText("Detruire le fichier/dossier dans le système de fichier?");

        final Optional opt = dialog.showAndWait();
        if(opt.isPresent() && ButtonType.OK.equals(opt.get())){
            final File f = getSelectedFile();
            if (f != null) {
                if (f.isDirectory()) {
                    FileUtilities.deleteDirectory(f);
                } else {
                    f.delete();
                }
                removeProperties(f);

                // refresh tree
                updateRoot();
            } else {
                showErrorDialog("Vous devez selectionner un dossier.");
            }
        }
    }
    
    @FXML
    public void setMainFolder(ActionEvent event) {
        final Dialog dialog    = new Dialog();
        final DialogPane pane  = new DialogPane();
        final MainFolderPane ipane = new MainFolderPane();
        pane.setContent(ipane);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Emplacement du dossier");

        final Optional opt = dialog.showAndWait();
        if(opt.isPresent() && ButtonType.OK.equals(opt.get())){
            File f = new File(ipane.rootFolderField.getText());
            if (f.isDirectory() && verifyDatabaseVersion(f)) {
                rootPath = f.getPath();
                
                final Preferences prefs = Preferences.userRoot().node(getClass().getName());
                prefs.put(ROOT_FOLDER, rootPath);
                importDocButton.disableProperty().set(false);
                deleteDocButton.disableProperty().set(false);
                addDocButton.disableProperty().set(false);
                addFolderButton.disableProperty().set(false);
                listButton .disableProperty().set(false);
                // refresh tree
                updateRoot();
                setDatabaseIdentifier(new File(rootPath), getDatabaseIdentifier());
            }
        }
    }
    
    @FXML
    public void showAddFolderDialog(ActionEvent event) {
        final Dialog dialog    = new Dialog();
        final DialogPane pane  = new DialogPane();
        final NewFolderPane ipane = new NewFolderPane();
        pane.setContent(ipane);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Création de dossier");

        final Optional opt = dialog.showAndWait();
        if(opt.isPresent() && ButtonType.OK.equals(opt.get())){
            String folderName  = ipane.folderNameField.getText();
            final File rootDir = new File(rootPath);
            switch (ipane.locCombo.getValue()) {
                case NewFolderPane.IN_CURRENT_FOLDER: 
                    addToSelectedFolder(folderName);
                    break;
                case NewFolderPane.IN_ALL_FOLDER:
                    addToAllFolder(rootDir, folderName);
                    updateRoot();
                    break;     
                case NewFolderPane.IN_SE_FOLDER:
                    addToSeFolder(rootDir, folderName);
                    updateRoot();
                    break;
                case NewFolderPane.IN_TR_FOLDER:
                    addToTrFolder(rootDir, folderName);
                    updateRoot();
                    break;
            }
        }
    }
    
    private boolean verifyDatabaseVersion(final File rootDirectory) {
        final String key         = getDatabaseIdentifier();
        final String existingKey = getExistingDatabaseIdentifier(rootDirectory);
        if (existingKey == null) {
            return true;
        } else if (!existingKey.equals(key)) {
            return showBadVersionDialog(existingKey, key);
        }
        return true;
    }
    
    private void showErrorDialog(final String errorMsg) {
        final Dialog dialog    = new Alert(Alert.AlertType.ERROR);
        final DialogPane pane  = new DialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Erreur");
        dialog.setContentText(errorMsg);
        dialog.showAndWait();
    }
    
    private boolean showBadVersionDialog(final String existingKey, final String dbKey) {
        final Dialog dialog    = new Alert(Alert.AlertType.ERROR);
        final DialogPane pane  = new DialogPane();
        final DatabaseVersionPane ipane = new DatabaseVersionPane(existingKey, dbKey);
        pane.setContent(ipane);
        pane.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Version de la base differente");
        dialog.setContentText("Le système de fichier que vous tenter d'ouvrir correspond a une autre base de données.\n Voulez vous l'ouvrir quand même?");
        final Optional opt = dialog.showAndWait();
        return opt.isPresent() && ButtonType.YES.equals(opt.get());
    }
    
    private File getSelectedFile() {
        TreeItem<File> item = tree1.getSelectionModel().getSelectedItem();
        if (item != null) {
            return item.getValue();
        }
        return null;
    }
    
    private void updateRoot() {
        final File rootDirectory = new File(rootPath);
        
        //objet detruit
        final File saveDir = new File(rootDirectory, SAVE_FOLDER);
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }
        
        final File unclassifiedDir = getOrCreateUnclassif(rootDirectory);
                
        final SystemeEndiguementRepository SErepo = Injector.getBean(SystemeEndiguementRepository.class);
        final DigueRepository Drepo = Injector.getBean(DigueRepository.class);
        final TronconDigueRepository TRrepo = Injector.getBean(TronconDigueRepository.class);
        
        //on recupere tous les elements
        final List<SystemeEndiguement> ses    = SErepo.getAll();
        final Set<Digue> digues               = new HashSet<>(Drepo.getAll());
        final Set<TronconDigue> troncons      = new HashSet<>(TRrepo.getAllLight());
        final Set<Digue> diguesFound          = new HashSet<>();
        final Set<TronconDigue> tronconsFound = new HashSet<>();
        final Set<File> seFiles               = listSE(rootDirectory);
        
        
        for (SystemeEndiguement se : ses) {
            final File seDir = getOrCreateSE(rootDirectory, se);
            seFiles.remove(seDir);
            
            final Set<File> digueFiles = listDigue(seDir);
            
            final List<String> digueIds = se.getDigueIds();
            for (Digue digue : digues) {
                if (!digueIds.contains(digue.getDocumentId())) continue;
                diguesFound.add(digue);
                
                final File digueDir = getOrCreateDG(seDir, digue);
                digueFiles.remove(digueDir);
                
                final Set<File> trFiles = listTroncon(digueDir);

                for (final TronconDigue td : troncons) {
                    if (td.getDigueId()==null || !td.getDigueId().equals(digue.getDocumentId())) continue;
                    tronconsFound.add(td);

                    final File trDir = getOrCreateTR(digueDir, td);
                    trFiles.remove(trDir);
                }
                
                // on place les tronçon disparus dans backup
                backupDirectories(saveDir, trFiles);
            }
            
            // on place les digues disparues dans backup
            backupDirectories(saveDir, digueFiles);
        }
        
        // on place les systèmes d'endiguement disparus dans backup
        backupDirectories(saveDir, seFiles);
        
        
        
        //on place toute les digues et troncons non trouvé dans un group a part
        digues.removeAll(diguesFound);
        
        
        /*
         * digue / tronçon non classé
         */      
        final Set<File> digueFiles = listDigue(unclassifiedDir);
        
        for (final Digue digue : digues) {
            final File digueDir = getOrCreateDG(unclassifiedDir, digue);
            digueFiles.remove(digueDir);
            
            final Set<File> trFiles = listTroncon(digueDir);
            
            for (final TronconDigue td : troncons) {
                if (td.getDigueId()==null || !td.getDigueId().equals(digue.getDocumentId())) continue;
                tronconsFound.add(td);

                final File trDir = getOrCreateTR(digueDir, td);
                trFiles.remove(trDir);
            }
            
            // on place les tronçon disparus dans backup
            backupDirectories(saveDir, trFiles);
        }
        
        // on place les digues disparues dans backup
        backupDirectories(saveDir, digueFiles);
        
        
        troncons.removeAll(tronconsFound);
        
        final Set<File> trFiles = listTroncon(unclassifiedDir, false);
        
        for(final TronconDigue td : troncons){
            final File trDir = getOrCreateTR(unclassifiedDir, td);
            trFiles.remove(trDir);
        }
        
        // on place les tronçon disparus dans backup
        backupDirectories(saveDir, trFiles);
        
        
        TreeItem root = new FileTreeItem(rootDirectory);
        tree1.setRoot(root);
    }
    
    private void addToSelectedFolder(final String folderName) {
        File directory = getSelectedFile();
        if (directory != null && directory.isDirectory()) {
            if (getIsModelFolder(directory)) {
                directory = new File(directory, DOCUMENT_FOLDER);
                if (!directory.exists()) {
                    directory.mkdir();
                }
            }
            final File newDir = new File(directory, folderName);
            newDir.mkdir();
            updateRoot();
        } else {
            showErrorDialog("Vous devez selectionner un dossier.");
        }
    }
    
    private void addToAllFolder(final File rootDir, final String folderName) {
        for (File f : rootDir.listFiles()) {
            if (f.isDirectory()) {
                if (f.getName().equals(DOCUMENT_FOLDER)) {
                    final File newDir = new File(f, folderName);
                    if (!newDir.exists()) {
                        newDir.mkdir();
                    }
                } else {
                    addToAllFolder(f, folderName);
                }
            }
        }
    }
    
    private void addToTrFolder(final File rootDir, final String folderName) {
        for (File f : rootDir.listFiles()) {
            if (f.isDirectory()) {
                if (getIsTr(f)) {
                    final File docDir = new File(f, DOCUMENT_FOLDER);
                    if (!docDir.exists()) {
                        docDir.mkdir();
                    }
                    final File newDir = new File(docDir, folderName);
                    if (!newDir.exists()) {
                        newDir.mkdir();
                    }
                } else {
                    addToTrFolder(f, folderName);
                }
            }
        }
    }
    
    private void addToSeFolder(final File rootDir, final String folderName) {
        for (File f : rootDir.listFiles()) {
            if (f.isDirectory()) {
                if (getIsSe(f)) {
                    final File docDir = new File(f, DOCUMENT_FOLDER);
                    if (!docDir.exists()) {
                        docDir.mkdir();
                    }
                    final File newDir = new File(docDir, folderName);
                    if (!newDir.exists()) {
                        newDir.mkdir();
                    }
                } else {
                    addToSeFolder(f, folderName);
                }
            }
        }
    }
    
    private String getDatabaseIdentifier() {
        final SirsDBInfoRepository DBrepo = Injector.getBean(SirsDBInfoRepository.class);
        final Optional<SirsDBInfo> info = DBrepo.get();
        if (info.isPresent()) {
            final SirsDBInfo dbInfo = info.get();
            return dbInfo.getUuid() + "|" + dbInfo.getEpsgCode() + "|" + dbInfo.getVersion()  + "|" + dbInfo.getRemoteDatabase();
        }
        return null;
    }
    
    private static class DOIntegatedCell extends TreeTableCell {

        private CheckBox box = new CheckBox();

        public DOIntegatedCell() {
            setGraphic(box);
            box.disableProperty().bind(editingProperty());
            box.selectedProperty().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    File f = (File) getItem();
                    if (f != null) {
                        setDOIntegrated(f, newValue);
                    }
                }
            });
        }
        
        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            File f = (File) item;
            if (f == null || f.isDirectory()) {
                box.setVisible(false);
            } else {
                box.setVisible(true);
                box.setSelected(getDOIntegrated(f));
            }
        }
    }
}
