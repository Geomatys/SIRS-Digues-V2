
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

import static fr.sirs.plugin.document.PropertiesFileUtilities.*;

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
            final File directory = tree1.getSelectionModel().getSelectedItem().getValue();
            if (directory.isDirectory()) {
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
            final File f = tree1.getSelectionModel().getSelectedItem().getValue();
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
    
    private void updateRoot() {
        TreeItem root = new FileTreeItem(new File(rootPath));
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
