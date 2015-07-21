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
import fr.sirs.util.SirsStringConverter;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Panneau de gestion de création de documents dynamiques.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class DynamicDocumentsPane extends BorderPane implements Initializable {
    @FXML private ComboBox<Preview> uiSECombo;

    @FXML private ListView<TronconDigue> uiTronconsList;

    @FXML private ListView<RapportModeleDocument> uiModelsList;

    @FXML private VBox uiRightVBox;

    @FXML private VBox uiParagraphesVbox;

    @FXML private Button uiAddParagrapheBtn;

    @FXML private TextField uiModelNameTxtField;

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
}
