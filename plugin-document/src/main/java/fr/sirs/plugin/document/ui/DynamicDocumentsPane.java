package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.component.RapportModeleDocumentRepository;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RapportModeleDocument;
import fr.sirs.core.model.RapportSectionDocument;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.plugin.document.ODTUtils;
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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import org.apache.sis.measure.NumberRange;
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
    
    public DynamicDocumentsPane() {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
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
        String docName = uiDocumentNameField.getText();
        if (docName.isEmpty()) {
            showErrorDialog("Vous devez remplir le nom du fichier");
            return;
        }
        
        if (!docName.endsWith(".odt")) {
            docName = docName + ".odt";
        }
        final File newDoc = new File ("/home/guilhem/Bureau/" + docName);
        
        RapportModeleDocument modele = uiModelsList.getSelectionModel().getSelectedItem();
        try {
            ODTUtils.write(modele, newDoc, getElements());
            showConfirmDialog("Les documents ont été generés.");
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
            showErrorDialog(ex.getMessage());
        }
        
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
    
    private void showConfirmDialog(final String errorMsg) {
        final Dialog dialog    = new Alert(Alert.AlertType.CONFIRMATION);
        final DialogPane pane  = new DialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Succés");
        dialog.setContentText(errorMsg);
        dialog.showAndWait();
    }
    
    private Map<String, Objet> getElements() {
        final ObservableList<TronconDigue> troncons = uiTronconsList.getSelectionModel().getSelectedItems();
        final Map<String, Objet> elements = new LinkedHashMap<>();
        for (TronconDigue troncon : troncons) {
            if (troncon == null) {
                continue;
            }

            final List<Objet> objetList = TronconUtils.getObjetList(troncon.getDocumentId());
            for (Objet obj : objetList) {
                elements.put(obj.getDocumentId(), obj);
            }
        }
        return elements;
    }
}
