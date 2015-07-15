
package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
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
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.TronconDigue;
        
import static fr.sirs.plugin.document.PropertiesFileUtilities.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    
    private static final Logger LOGGER = Logging.getLogger(DocumentsPane.class);
    
    private String rootPath = "/home/guilhem/Bureau/sym_doc_test";
    
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
                return new SimpleStringProperty(f.getName());
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
        
        updateRoot();
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
                removeClassPlace(f);
                removeDOIntegrated(f);
                removeInventoryNumber(f);

                // refresh tree
                updateRoot();
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
            if (f.isDirectory()) {
                rootPath = f.getPath();
                // refresh tree
                updateRoot();
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
            String folderName = ipane.folderNameField.getText();
            switch (ipane.locCombo.getValue()) {
                case NewFolderPane.IN_CURRENT_FOLDER: 
                    final File directory = getSelectedFile();
                    if (directory != null && directory.isDirectory()) {
                        final File newDir = new File(directory, folderName);
                        newDir.mkdir();
                        updateRoot();
                    }
                    break;
                case NewFolderPane.IN_ALL_FOLDER:break;     
                case NewFolderPane.IN_SE_FOLDER:break;
                case NewFolderPane.IN_TR_FOLDER:break;
            }
        }
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
                
        final SystemeEndiguementRepository SErepo = Injector.getBean(SystemeEndiguementRepository.class);
        final DigueRepository Drepo = Injector.getBean(DigueRepository.class);
        final TronconDigueRepository TRrepo = Injector.getBean(TronconDigueRepository.class);
        
        //on recupere tous les elements
        final List<SystemeEndiguement> sds    = SErepo.getAll();
        final Set<Digue> digues               = new HashSet<>(Drepo.getAll());
        final Set<TronconDigue> troncons      = new HashSet<>(TRrepo.getAllLight());
        final Set<Digue> diguesFound          = new HashSet<>();
        final Set<TronconDigue> tronconsFound = new HashSet<>();
        
        for (SystemeEndiguement sd : sds) {
            final File sdDir = new File(rootDirectory, sd.getLibelle());
            if (!sdDir.exists()) {
                sdDir.mkdir();
            }
            
            final List<String> digueIds = sd.getDigueIds();
            for (Digue digue : digues) {
                if (!digueIds.contains(digue.getDocumentId())) continue;
                diguesFound.add(digue);
                
                String name = digue.getLibelle();
                if (name == null) {
                    name = "null";
                }
                final File digueDir = new File(sdDir, name);
                if (!digueDir.exists()) {
                    digueDir.mkdir();
                }

                for (final TronconDigue td : troncons) {
                    if (td.getDigueId()==null || !td.getDigueId().equals(digue.getDocumentId())) continue;
                    tronconsFound.add(td);

                    final File trDir = new File(digueDir, td.getLibelle());
                    if (!trDir.exists()) {
                        trDir.mkdir();
                    }

                }
            }
        }
        
        
        //on place toute les digues et troncons non trouvé dans un group a part
        digues.removeAll(diguesFound);
        final File unclassifiedDir = new File(rootDirectory, "Non classés"); 
        if (!unclassifiedDir.exists()) {
            unclassifiedDir.mkdir();
        }
        
        for (final Digue digue : digues) {
            String name = digue.getLibelle();
            if (name == null) {
                name = "null";
            }
            final File digueDir = new File(unclassifiedDir, name);
            if (!digueDir.exists()) {
                digueDir.mkdir();
            }
            for (final TronconDigue td : troncons) {
                if (td.getDigueId()==null || !td.getDigueId().equals(digue.getDocumentId())) continue;
                tronconsFound.add(td);

                final File trDir = new File(digueDir, td.getLibelle());
                if (!trDir.exists()) {
                    trDir.mkdir();
                }
            }
        }
        
        troncons.removeAll(tronconsFound);
        for(final TronconDigue td : troncons){
            final File trDir = new File(unclassifiedDir, td.getLibelle());
            if (!trDir.exists()) {
                trDir.mkdir();
            }
        }
        
        TreeItem root = new FileTreeItem(rootDirectory);
        tree1.setRoot(root);
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
