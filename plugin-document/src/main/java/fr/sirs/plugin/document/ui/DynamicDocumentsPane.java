package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.component.RapportModeleDocumentRepository;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RapportModeleDocument;
import fr.sirs.core.model.RapportSectionDocument;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.plugin.document.FileTreeItem;
import static fr.sirs.plugin.document.PropertiesFileUtilities.*;
import static fr.sirs.plugin.document.ui.DocumentsPane.ROOT_FOLDER;
import fr.sirs.util.SirsStringConverter;
import java.io.File;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.sis.util.logging.Logging;

/**
 * Panneau de gestion de création de documents dynamiques.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class DynamicDocumentsPane extends BorderPane implements Initializable {
    
    @FXML private CheckBox uiSelectAllTronconBox;
    
    @FXML private CheckBox uiOnlySEBox;
    
    @FXML private ComboBox<Preview> uiSECombo;

    @FXML private ListView<TronconDigue> uiTronconsList;

    @FXML private ListView<RapportModeleDocument> uiModelsList;

    @FXML private VBox uiRightVBox;

    @FXML private VBox uiParagraphesVbox;

    @FXML private Button uiAddParagrapheBtn;

    @FXML private TextField uiModelNameTxtField;
    
    @FXML private Label uiTronconLabel;
    
    @FXML private Button uiGenerateBtn;
    
    @FXML private TextField uiDocumentNameField;

    private static final Logger LOGGER = Logging.getLogger(DocumentsPane.class);
    
    private final FileTreeItem root;
    
    public DynamicDocumentsPane(final FileTreeItem root) {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
        this.root = root;
    }

    /**
     * Initialise les différents panneaux de la page.
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final Previews previewRepository = Injector.getSession().getPreviews();

        // Gestion de la liste de système d'endiguements et de tronçons associés
        uiSECombo.setEditable(false);
        uiSECombo.valueProperty().addListener(this::systemeEndiguementChange);
        uiTronconsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        final SirsStringConverter converter = new SirsStringConverter();
        uiTronconsList.setCellFactory(new Callback<ListView<TronconDigue>, ListCell<TronconDigue>>() {
            @Override
            public ListCell<TronconDigue> call(ListView<TronconDigue> param) {
                return new ListCell<TronconDigue>() {
                    @Override
                    protected void updateItem(TronconDigue item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(converter.toString(item));
                    }
                };
            }
        });

        uiSECombo.setConverter(new SirsStringConverter());
        uiSECombo.setItems(FXCollections.observableArrayList(
                previewRepository.getByClass(SystemeEndiguement.class)));
        if(uiSECombo.getItems()!=null){
            uiSECombo.getSelectionModel().select(0);
        }

        final RapportModeleDocumentRepository rmdr = Injector.getBean(RapportModeleDocumentRepository.class);
        uiModelsList.setCellFactory(new Callback<ListView<RapportModeleDocument>, ListCell<RapportModeleDocument>>() {
            @Override
            public ListCell<RapportModeleDocument> call(ListView<RapportModeleDocument> param) {
                return new ListCell<RapportModeleDocument>() {
                    @Override
                    protected void updateItem(RapportModeleDocument item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item != null) {
                            setText(item.getLibelle());
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });

        uiModelsList.setItems(FXCollections.observableArrayList(rmdr.getAll()));

        // Gestion de l'affichage de la partie de droite.
        uiModelsList.getSelectionModel().selectedItemProperty().addListener((observable, oldModel, newModel) -> {
            if (oldModel != null) {
                uiModelNameTxtField.textProperty().unbindBidirectional(oldModel.libelleProperty());
            }

            if (newModel != null) {
                if (uiParagraphesVbox.getChildren().size() > 1) {
                    uiParagraphesVbox.getChildren().remove(1, uiParagraphesVbox.getChildren().size());
                }

                uiModelNameTxtField.textProperty().bindBidirectional(newModel.libelleProperty());

                uiAddParagrapheBtn.setOnAction(event -> addParagraphePane(newModel));
                final List<RapportSectionDocument> sections = newModel.getSections();
                for (int i=0, length=sections.size(); i<length; i++) {
                    final RapportSectionDocument section = sections.get(i);
                    uiParagraphesVbox.getChildren().add(new ModelParagraphePane(uiParagraphesVbox, newModel, section, i+1));
                }
            }
        });

        uiRightVBox.visibleProperty().bind(uiModelsList.getSelectionModel().selectedItemProperty().isNotNull());
        
        uiOnlySEBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            uiTronconLabel.setVisible(!newValue);
            uiSelectAllTronconBox.setVisible(!newValue);
            uiTronconsList.setVisible(!newValue);
        });
        
        uiSelectAllTronconBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                uiTronconsList.getSelectionModel().selectRange(0, uiTronconsList.getItems().size());
            } else {
                uiTronconsList.getSelectionModel().clearSelection();
            }
        });
    }

    @FXML
    private void addModel() {
        final RapportModeleDocumentRepository rmdr = Injector.getBean(RapportModeleDocumentRepository.class);
        final RapportModeleDocument model = rmdr.create();
        model.setLibelle("Nouveau modèle");
        rmdr.add(model);
        uiModelsList.getItems().add(model);
    }

    @FXML
    private void deleteModel() {
        final RapportModeleDocument model = uiModelsList.getSelectionModel().getSelectedItem();
        uiModelsList.getItems().remove(model);
        Injector.getBean(RapportModeleDocumentRepository.class).remove(model);
    }

    @FXML
    private void saveModel() {
        final RapportModeleDocument model = uiModelsList.getSelectionModel().getSelectedItem();
        Injector.getBean(RapportModeleDocumentRepository.class).update(model);
        final ObservableList<RapportModeleDocument> oldModels = uiModelsList.getItems();
        uiModelsList.setItems(null);
        uiModelsList.setItems(oldModels);
        uiModelsList.getSelectionModel().select(model);
    }
    
    @FXML
    private void generateDocument(ActionEvent event) {
        String tmp = uiDocumentNameField.getText();
        if (tmp.isEmpty()) {
            showErrorDialog("Vous devez remplir le nom du fichier");
            return;
        }
        
        final String docName;
        if (!tmp.endsWith(".odt")) {
            docName = tmp + ".odt";
        } else {
            docName= tmp;
        }
        final Preferences prefs = Preferences.userRoot().node("DocumentPlugin");
        String rootPath = prefs.get(ROOT_FOLDER, null);
        
        if (rootPath == null || rootPath.isEmpty()) {
            rootPath = setMainFolder();
        }
        
        final File rootDir = new File (rootPath);
        root.setValue(new File (rootPath));
        
        final RapportModeleDocument modele = uiModelsList.getSelectionModel().getSelectedItem();
        if (modele == null) {
            showErrorDialog("Vous devez selectionner un modéle.");
            return;
        }
        
        final Stage dialog         = new Stage();
        final DialogPane pane      = new DialogPane();
        final GenerationPane ipane = new GenerationPane();
        ipane.uiGenerateFinish.setOnAction((ActionEvent event1) -> {dialog.hide();});
        pane.setContent(ipane);
        dialog.setScene(new Scene(pane));
        dialog.setResizable(true);
        dialog.setTitle("Generation des document");
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        final Collection<TronconDigue> troncons = getTronconList();
        final File seDir                        = getOrCreateSE(rootDir, getSelectedSE());

        new Thread() {
           @Override
           public void run() {
               ipane.generateDoc(docName, uiOnlySEBox.isSelected(), modele, troncons, seDir, root); 
           }
        }.start();
            
        dialog.show();
    }
        

    
    public String setMainFolder() {
        final Dialog dialog    = new Dialog();
        final DialogPane pane  = new DialogPane();
        final MainFolderPane ipane = new MainFolderPane();
        pane.setContent(ipane);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Emplacement du dossier racine");

        String rootPath = null;
        final Optional opt = dialog.showAndWait();
        if(opt.isPresent() && ButtonType.OK.equals(opt.get())){
            File f = new File(ipane.rootFolderField.getText());
            if (f.isDirectory() && verifyDatabaseVersion(f)) {
                rootPath = f.getPath();
                
                final Preferences prefs = Preferences.userRoot().node("DocumentPlugin");
                prefs.put(ROOT_FOLDER, rootPath);
                updateDatabaseIdentifier(new File(rootPath));
            }
        }
        return rootPath;
    }

    /**
     * Rafraîchit la liste des tronçons associés au système d'endiguement choisi.
     *
     * @param observable système d'endiguement
     * @param oldValue ancien système
     * @param newValue nouveau système
     */
    private void systemeEndiguementChange(ObservableValue<? extends Preview> observable,
                                          Preview oldValue, Preview newValue) {
        if(newValue==null){
            uiTronconsList.setItems(FXCollections.emptyObservableList());
        }else{
            final SystemeEndiguementRepository sdRepo = (SystemeEndiguementRepository) Injector.getSession().getRepositoryForClass(SystemeEndiguement.class);
            final DigueRepository digueRepo = (DigueRepository) Injector.getSession().getRepositoryForClass(Digue.class);
            final TronconDigueRepository tronconRepo = (TronconDigueRepository) Injector.getSession().getRepositoryForClass(TronconDigue.class);
            final SystemeEndiguement sd = sdRepo.get(newValue.getElementId());
            final Set<TronconDigue> troncons = new HashSet<>();
            final List<Digue> digues = digueRepo.getBySystemeEndiguement(sd);
            for(Digue digue : digues){
                troncons.addAll(tronconRepo.getByDigue(digue));
            }
            uiTronconsList.setItems(FXCollections.observableArrayList(troncons));
        }
    }

    /**
     * Ajoute un paragraphe au modèle de document.
     */
    private void addParagraphePane(final RapportModeleDocument model) {
        final RapportSectionDocument newSection =
                Injector.getSession().getElementCreator().createElement(RapportSectionDocument.class);
        model.getSections().add(newSection);
        uiParagraphesVbox.getChildren().add(new ModelParagraphePane(uiParagraphesVbox, model, newSection, model.getSections().size()));
    }
    
    private SystemeEndiguement getSelectedSE() {
        final Preview newValue = uiSECombo.getSelectionModel().getSelectedItem();
        final SystemeEndiguementRepository sdRepo = (SystemeEndiguementRepository) Injector.getSession().getRepositoryForClass(SystemeEndiguement.class);
        return sdRepo.get(newValue.getElementId());
    }
    
    private Collection<TronconDigue> getTronconList() {
        if (uiOnlySEBox.isSelected()) {
            final SystemeEndiguement sd              = getSelectedSE();
            final DigueRepository digueRepo          = (DigueRepository) Injector.getSession().getRepositoryForClass(Digue.class);
            final TronconDigueRepository tronconRepo = (TronconDigueRepository) Injector.getSession().getRepositoryForClass(TronconDigue.class);
            final Set<TronconDigue> troncons         = new HashSet<>();
            final List<Digue> digues                 = digueRepo.getBySystemeEndiguement(sd);
            for(Digue digue : digues){
                troncons.addAll(tronconRepo.getByDigue(digue));
            }
            return troncons;
            
        } else {
            return  uiTronconsList.getSelectionModel().getSelectedItems();
        }
    }
    
}
