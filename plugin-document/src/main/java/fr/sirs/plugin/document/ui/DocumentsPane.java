
package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
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
import fr.sirs.core.component.RapportModeleDocumentRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.RapportModeleDocument;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.plugin.document.DynamicDocumentTheme;
import fr.sirs.plugin.document.ODTUtils;
        
import static fr.sirs.plugin.document.PropertiesFileUtilities.*;
import java.awt.Desktop;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    
    @FXML
    private Button hideShowButton;

    @FXML
    private Button hideFileButton;
    
    protected static final String BUTTON_STYLE = "buttonbar-button";
    
    private static final Image ADDF_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/add_folder.png"));
    private static final Image ADDD_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/add_doc.png"));
    private static final Image IMP_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/import.png"));
    private static final Image DEL_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/remove.png"));
    private static final Image SET_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/set.png"));
    private static final Image LIST_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/list.png"));
    private static final Image PUB_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/publish.png"));
    private static final Image OP_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/publish.png"));
    private static final Image HIDE_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/set.png"));
    private static final Image HISH_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/set.png"));
    
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
    
    public static final String UNCLASSIFIED     = "Non classés";
    public static final String SAVE_FOLDER      = "Sauvegarde";
    public static final String DOCUMENT_FOLDER  = "Dossier d'ouvrage";
    public static final String ROOT_FOLDER      = "symadrem.root.foler";
    
    // SIRS hidden file properties
    public static final String INVENTORY_NUMBER = "inventory_number";
    public static final String CLASS_PLACE      = "class_place";
    public static final String DO_INTEGRATED    = "do_integrated";
    public static final String LIBELLE          = "libelle";
    public static final String DYNAMIC          = "dynamic";
    public static final String MODELE           = "modele";
    public static final String HIDDEN           = "hidden";
    
    public static final String SE = "se";
    public static final String TR = "tr";
    public static final String DG = "dg";
    
    
    private static final Logger LOGGER = Logging.getLogger(DocumentsPane.class);
    
    private final FileTreeItem root;
    
    private final DynamicDocumentTheme dynDcTheme;
    
    public DocumentsPane(final FileTreeItem root, final DynamicDocumentTheme dynDcTheme) {
        SIRS.loadFXML(this, DocumentsPane.class);
        Injector.injectDependencies(this);
        this.root = root;
        this.dynDcTheme = dynDcTheme;
        
        getStylesheets().add(SIRS.CSS_PATH);
        
        addFolderButton.setGraphic(new ImageView(ADDF_BUTTON_IMAGE));
        importDocButton.setGraphic(new ImageView(IMP_BUTTON_IMAGE));
        deleteDocButton.setGraphic(new ImageView(DEL_BUTTON_IMAGE));
        setFolderButton.setGraphic(new ImageView(SET_BUTTON_IMAGE));
        addDocButton.setGraphic(new ImageView(ADDD_BUTTON_IMAGE));
        listButton.setGraphic(new ImageView(LIST_BUTTON_IMAGE));
        hideShowButton.setGraphic(new ImageView(HISH_BUTTON_IMAGE));
        hideFileButton.setGraphic(new ImageView(HIDE_BUTTON_IMAGE));
        
        addFolderButton.setTooltip(new Tooltip("Ajouter un dossier"));
        importDocButton.setTooltip(new Tooltip("Importer un fichier"));
        deleteDocButton.setTooltip(new Tooltip("Supprimer un fichier"));
        setFolderButton.setTooltip(new Tooltip("Configurer le dossier racine"));
        addDocButton.setTooltip(new Tooltip("Ajouter un dossier dynamique"));
        listButton.setTooltip(new Tooltip("Exporter le sommaire"));
        hideShowButton.setTooltip(new Tooltip("Cacher/Afficher les fichiers cachés"));
        hideFileButton.setTooltip(new Tooltip("Cacher/Afficher le fichier selectionné"));
        
        addFolderButton.getStyleClass().add(BUTTON_STYLE);
        importDocButton.getStyleClass().add(BUTTON_STYLE);
        deleteDocButton.getStyleClass().add(BUTTON_STYLE);
        setFolderButton.getStyleClass().add(BUTTON_STYLE);
        addDocButton.getStyleClass().add(BUTTON_STYLE);
        listButton.getStyleClass().add(BUTTON_STYLE);
        hideShowButton.getStyleClass().add(BUTTON_STYLE);
        hideFileButton.getStyleClass().add(BUTTON_STYLE);
        
        // Name column
        tree1.getColumns().get(0).setEditable(false);
        tree1.getColumns().get(0).setCellValueFactory(new Callback() {
            @Override
            public ObservableValue call(Object param) {
                final File f = (File) ((CellDataFeatures)param).getValue().getValue();
                return new SimpleObjectProperty(f);
            }
        });
        tree1.getColumns().get(0).setCellFactory(new Callback() {
            @Override
            public TreeTableCell call(Object param) {
                return new FileNameCell();
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
                final FileTreeItem f = (FileTreeItem) ((CellDataFeatures)param).getValue();
                return new SimpleStringProperty(f.getSize());
            }
        });
        
        // Inventory number column
        tree1.getColumns().get(3).setCellValueFactory(new Callback() {
            @Override
            public ObservableValue call(Object param) {
                final File f = (File) ((CellDataFeatures)param).getValue().getValue();
                return new SimpleObjectProperty(f);
            }
        });
        tree1.getColumns().get(3).setCellFactory(new Callback() {
            @Override
            public TreeTableCell call(Object param) {
                return new PropertyCell(INVENTORY_NUMBER);
            }
        });
        
        // class place column
        tree1.getColumns().get(4).setCellValueFactory(new Callback() {
            @Override
            public ObservableValue call(Object param) {
                final File f = (File) ((CellDataFeatures)param).getValue().getValue();
                return new SimpleObjectProperty(f);
            }
        });
        tree1.getColumns().get(4).setCellFactory(new Callback() {
            @Override
            public TreeTableCell call(Object param) {
                return new PropertyCell(CLASS_PLACE);
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
        
        // publish column
        tree1.getColumns().get(6).setCellValueFactory(new Callback() {
            @Override
            public ObservableValue call(Object param) {
                final FileTreeItem f = (FileTreeItem) ((CellDataFeatures)param).getValue();
                return new SimpleObjectProperty(f);
            }
        });
        tree1.getColumns().get(6).setCellFactory(new Callback() {
            @Override
            public TreeTableCell call(Object param) {
                return new PublicationCell(root);
            }
        });
        
        // open column
        tree1.getColumns().get(7).setCellValueFactory(new Callback() {
            @Override
            public ObservableValue call(Object param) {
                final FileTreeItem f = (FileTreeItem) ((CellDataFeatures)param).getValue();
                return new SimpleObjectProperty(f);
            }
        });
        tree1.getColumns().get(7).setCellFactory(new Callback() {
            @Override
            public TreeTableCell call(Object param) {
                return new OpenCell();
            }
        });
        
        
        tree1.setShowRoot(false);
        tree1.setRoot(root);
        
        final Preferences prefs = Preferences.userRoot().node("DocumentPlugin");
        final String rootPath   = prefs.get(ROOT_FOLDER, null);
        
        if (rootPath != null && verifyDatabaseVersion(new File(rootPath))) {
            final File rootDirectory = new File(rootPath);
            root.setValue(rootDirectory);
            updateDatabaseIdentifier(rootDirectory);
           
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
                setProperty(newFile, INVENTORY_NUMBER, ipane.inventoryNumField.getText());
                setProperty(newFile, CLASS_PLACE,      ipane.classPlaceField.getText());
                
                // refresh tree
                update();
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
                update();
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
        dialog.setTitle("Emplacement du dossier racine");

        final Optional opt = dialog.showAndWait();
        if(opt.isPresent() && ButtonType.OK.equals(opt.get())){
            File f = new File(ipane.rootFolderField.getText());
            if (f.isDirectory() && verifyDatabaseVersion(f)) {
                String rootPath = f.getPath();
                
                final Preferences prefs = Preferences.userRoot().node("DocumentPlugin");
                prefs.put(ROOT_FOLDER, rootPath);
                importDocButton.disableProperty().set(false);
                deleteDocButton.disableProperty().set(false);
                addDocButton.disableProperty().set(false);
                addFolderButton.disableProperty().set(false);
                listButton .disableProperty().set(false);
                // refresh tree
                final File rootDirectory = new File(rootPath);
                updateFileSystem(rootDirectory);
                root.setValue(rootDirectory);
                root.update(root.rootShowHiddenFile);
                updateDatabaseIdentifier(rootDirectory);
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
            final File rootDir = root.getValue();
            switch (ipane.locCombo.getValue()) {
                case NewFolderPane.IN_CURRENT_FOLDER: 
                    addToSelectedFolder(folderName);
                    break;
                case NewFolderPane.IN_ALL_FOLDER:
                    addToAllFolder(rootDir, folderName);
                    update();
                    break;     
                case NewFolderPane.IN_SE_FOLDER:
                    addToModelFolder(rootDir, folderName, SE);
                    update();
                    break;
                case NewFolderPane.IN_DG_FOLDER:
                    addToModelFolder(rootDir, folderName, DG);
                    update();
                    break;
                case NewFolderPane.IN_TR_FOLDER:
                    addToModelFolder(rootDir, folderName, TR);
                    update();
                    break;
            }
        }
    }
    
    @FXML
    public void exportOdtSummary(ActionEvent event) {
        final Dialog dialog    = new Dialog();
        final DialogPane pane  = new DialogPane();
        final SaveSummaryPane ipane = new SaveSummaryPane();
        pane.setContent(ipane);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Exporter le sommaire");

        final Optional opt = dialog.showAndWait();
        if(opt.isPresent() && ButtonType.OK.equals(opt.get())){
            File f = new File(ipane.newFileFIeld.getText());
            try {
                ODTUtils.writeSummary(root, f);
            } catch (Exception ex) {
                showErrorDialog(ex.getMessage());
            }
        }
    }
    
    @FXML
    public void openDynamicDocTab(ActionEvent event) {
        Session session = Injector.getSession();
        final Tab result = session.getOrCreateThemeTab(dynDcTheme);
        session.getFrame().addTab(result);
    }
    
    @FXML
    public void hideFiles(ActionEvent event) {
        File f = getSelectedFile();
        boolean previousState = getBooleanProperty(f, HIDDEN);
        setBooleanProperty(f, HIDDEN, !previousState);
        update();
    }

    @FXML
    public void hideShowFiles(ActionEvent event) {
        root.rootShowHiddenFile = !root.rootShowHiddenFile;
        update();
    }
    
    private File getSelectedFile() {
        TreeItem<File> item = tree1.getSelectionModel().getSelectedItem();
        if (item != null) {
            return item.getValue();
        }
        return null;
    }
    
    private void update() {
        root.update(root.rootShowHiddenFile);
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
            update();
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
    
    private void addToModelFolder(final File rootDir, final String folderName, final String model) {
        for (File f : rootDir.listFiles()) {
            if (f.isDirectory()) {
                if (getIsModelFolder(f, model)) {
                    final File docDir = new File(f, DOCUMENT_FOLDER);
                    if (!docDir.exists()) {
                        docDir.mkdir();
                    }
                    final File newDir = new File(docDir, folderName);
                    if (!newDir.exists()) {
                        newDir.mkdir();
                    }
                } else {
                    addToModelFolder(f, folderName, model);
                }
            }
        }
    }
    
    private static class DOIntegatedCell extends TreeTableCell {

        private final CheckBox box = new CheckBox();

        public DOIntegatedCell() {
            setGraphic(box);
            box.disableProperty().bind(editingProperty());
            box.selectedProperty().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    File f = (File) getItem();
                    if (f != null) {
                        setBooleanProperty(f, DO_INTEGRATED, newValue);
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
                box.setSelected(getBooleanProperty(f, DO_INTEGRATED));
            }
        }
    }
    
    private static class PropertyCell extends TreeTableCell {

        private TextField text = new TextField();

        private final String property;
        
        public PropertyCell(final String property) {
            this.property = property;
            setGraphic(text);
            text.disableProperty().bind(editingProperty());
            text.textProperty().addListener(new ChangeListener<String>() {

                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    File f = (File) getItem();
                    if (f != null) {
                        setProperty(f, property, newValue);
                    }
                }
            });
        }
        
        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            File f = (File) item;
            if (f == null || f.isDirectory()) {
                text.setVisible(false);
            } else {
                text.setVisible(true);
                text.setText(getProperty(f, property));
            }
        }
    }
    
    private static class PublicationCell extends TreeTableCell {

        private final Button button = new Button();

        private final FileTreeItem root;
        
        public PublicationCell(final FileTreeItem root) {
            setGraphic(button);
            this.root = root;
            button.setGraphic(new ImageView(PUB_BUTTON_IMAGE));
            button.getStyleClass().add(BUTTON_STYLE);
            button.disableProperty().bind(editingProperty());
            button.setOnAction(this::handle);
            
        }
        
        public void handle(ActionEvent event) {
            final FileTreeItem item = (FileTreeItem) getItem();
            if (getBooleanProperty(item.getValue(), DYNAMIC)) {
                regenerateDynamicDocument(item.getValue());
            } else if (item.getValue().getName().equals(DOCUMENT_FOLDER)) {
                printSynthesisDoc(item);
            }
        }
        
        private void printSynthesisDoc(final FileTreeItem item) {
            final Dialog dialog    = new Dialog();
            final DialogPane pane  = new DialogPane();
            final SaveSummaryPane ipane = new SaveSummaryPane();
            pane.setContent(ipane);
            pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
            dialog.setDialogPane(pane);
            dialog.setResizable(true);
            dialog.setTitle("Exporter le dossier de synthèse");

            final Optional opt = dialog.showAndWait();
            if(opt.isPresent() && ButtonType.OK.equals(opt.get())){
                File f = new File(ipane.newFileFIeld.getText());
                try {
                    final Stage stage         = new Stage();
                    final DialogPane stagePane      = new DialogPane();
                    final GenerationPane gpane = new GenerationPane();
                    gpane.uiGenerateFinish.setOnAction((ActionEvent event1) -> {stage.hide();});
                    stagePane.setContent(gpane);
                    stage.setScene(new Scene(stagePane));
                    stage.setResizable(true);
                    stage.setTitle("Generation du document de synthèse");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    
                    new Thread() {
                       @Override
                       public void run() {
                           gpane.writeDoSynth(item, f);
                       }
                    }.start();

                    stage.show();
                } catch (Exception ex) {
                    showErrorDialog(ex.getMessage());
                }
            }
        }
        
        private void regenerateDynamicDocument(final File item) {
            final RapportModeleDocumentRepository rmdr = Injector.getBean(RapportModeleDocumentRepository.class);
            String modelId = getProperty(item, MODELE);
            if (modelId != null && !modelId.isEmpty()) {
                final RapportModeleDocument modele = rmdr.get(modelId);
                if (modele != null) {
                    
                    final Stage dialog         = new Stage();
                    final DialogPane pane      = new DialogPane();
                    final GenerationPane ipane = new GenerationPane();
                    ipane.uiGenerateFinish.setOnAction((ActionEvent event1) -> {dialog.hide();});
                    pane.setContent(ipane);
                    dialog.setScene(new Scene(pane));
                    dialog.setResizable(true);
                    dialog.setTitle("Mise à jour du document");
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    
                    final Collection<TronconDigue> troncons = getTronconList();
                    new Thread() {
                       @Override
                       public void run() {
                           ipane.reGenerateDoc(modele, troncons, item, root, root.rootShowHiddenFile);
                       }
                    }.start();

                    dialog.show();
                }
            } else {
                showErrorDialog("Impossible de resoudre l'identifiant du modèle pour le fichier: " + item.getName());
            }
        }
        
        private Collection<TronconDigue> getTronconList() {
            final FileTreeItem item = (FileTreeItem) getItem();
            final File modelFolder  = getModelFolder(item.getValue());
            Collection<TronconDigue> elements;
            if (getIsModelFolder(modelFolder, SE)) {
                final SystemeEndiguementRepository sdRepo = (SystemeEndiguementRepository) Injector.getSession().getRepositoryForClass(SystemeEndiguement.class);
                final SystemeEndiguement sd                = sdRepo.get(modelFolder.getName());
                final DigueRepository digueRepo          = (DigueRepository) Injector.getSession().getRepositoryForClass(Digue.class);
                final TronconDigueRepository tronconRepo = (TronconDigueRepository) Injector.getSession().getRepositoryForClass(TronconDigue.class);
                final Set<TronconDigue> troncons         = new HashSet<>();
                final List<Digue> digues                 = digueRepo.getBySystemeEndiguement(sd);
                for(Digue digue : digues){
                    troncons.addAll(tronconRepo.getByDigue(digue));
                }
                return troncons;
            } else if (getIsModelFolder(modelFolder, TR)) {
                final TronconDigueRepository tronconRepo = (TronconDigueRepository) Injector.getSession().getRepositoryForClass(TronconDigue.class);
                return Collections.singleton(tronconRepo.get(modelFolder.getName()));
            } else {
                elements = new ArrayList<>();
            }
            return elements;
        }

        private File getModelFolder(File f) {
            if (getIsModelFolder(f)) {
                return f;
            } else if (!f.getParentFile().equals(root.getValue())) {
                return getModelFolder(f.getParentFile());
            }
            return null;
        }
        
        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            final FileTreeItem ft = (FileTreeItem) item;
            if (ft != null) {
                final File f          = ft.getValue();
                if (f != null && (getBooleanProperty(f, DYNAMIC) || f.getName().equals(DOCUMENT_FOLDER))) {
                    if (getBooleanProperty(f, DYNAMIC)) {
                        button.setTooltip(new Tooltip("Mettre a jour le fichier dynamique"));
                    } else if (f.getName().equals(DOCUMENT_FOLDER)) {
                        button.setTooltip(new Tooltip("Exporter le dossier de synthèse"));
                    }
                    button.setVisible(true);
                } else {
                    button.setVisible(false);
                }
            } else {
                button.setVisible(false);
            }
        }
    }
    
    private static class OpenCell extends TreeTableCell {

        private final Button button = new Button();

        
        public OpenCell() {
            setGraphic(button);
            button.setGraphic(new ImageView(OP_BUTTON_IMAGE));
            button.getStyleClass().add(BUTTON_STYLE);
            button.disableProperty().bind(editingProperty());
            button.setOnAction(this::handle);
            
        }
        
        public void handle(ActionEvent event) {
            final FileTreeItem item = (FileTreeItem) getItem();
            if (item != null && item.getValue() != null) {
                File file = item.getValue();

                //first check if Desktop is supported by Platform or not
                if(!Desktop.isDesktopSupported()){
                    showErrorDialog("Impossible d'ouvrir des fichiers sur cette application.");
                    return;
                }

                 new Thread(new Runnable() {  
                        @Override  
                        public void run() {  
                            try {
                                Desktop.getDesktop().open(file);
                            } catch (IOException ex) {
                               LOGGER.log(Level.WARNING, null, ex);
                                showErrorDialog(ex.getMessage());
                            }
                        }
                 }).start();

            }
        }
        
        
        
        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            final FileTreeItem ft = (FileTreeItem) item;
            if (ft != null) {
                final File f          = ft.getValue();
                if (f != null && !f.isDirectory()) {
                    button.setTooltip(new Tooltip("Ouvrir le fichier"));
                    button.setVisible(true);
                } else {
                    button.setVisible(false);
                }
            } else {
                button.setVisible(false);
            }
        }
    }
    
    private static class FileNameCell extends TreeTableCell {

        private final Label label = new Label();

        public FileNameCell() {
            setGraphic(label);
        }
        
        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            File f = (File) item;
            if (f != null) {
                final String name;
                if (getIsModelFolder(f)) {
                    name = getProperty(f, LIBELLE);
                } else {
                    name = f.getName();
                }
                label.setText(name);
                label.setOpacity(1.0);
                if (getBooleanProperty(f, HIDDEN)) {
                    label.setOpacity(0.5);
                }
            }
        }
    }
}
