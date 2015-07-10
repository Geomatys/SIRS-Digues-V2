package fr.sirs.digue;

import fr.sirs.FXEditMode;
import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.core.component.Previews;
import fr.sirs.map.FXMapTab;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.GardeTroncon;
import fr.sirs.core.model.GestionTroncon;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.ProprieteTroncon;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefRive;
import fr.sirs.core.model.RefTypeTroncon;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.AbstractFXElementPane;
import fr.sirs.theme.ui.FXValidityPeriodPane;
import fr.sirs.theme.ui.ForeignParentPojoTable;
import fr.sirs.theme.ui.PojoTable;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.util.FXDateField;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTronconDiguePane extends AbstractFXElementPane<TronconDigue> {

    @Autowired private Session session;
    private final Previews previewRepository;

    // En-tete
    @FXML private FXEditMode uiMode;
    //@FXML private TextField uiName;
    @FXML private TextField uiPseudoId;
    @FXML private FXDateField date_maj;

    // Onglet "Information"
    @FXML FXValidityPeriodPane uiValidityPeriod;
    @FXML TextField ui_libelle;
    @FXML HTMLEditor ui_commentaire;
    @FXML ComboBox ui_digueId;
    @FXML ComboBox ui_positionId;
    @FXML ComboBox ui_typeRiveId;
    @FXML ComboBox ui_typeTronconId;
    @FXML ComboBox ui_systemeRepDefautId;

    // Onglet "SR"
    @FXML private ListView<SystemeReperage> uiSRList;
    @FXML private Button uiSRDelete;
    @FXML private Button uiSRAdd;
    @FXML private BorderPane uiSrTab;
    private final FXSystemeReperagePane srController = new FXSystemeReperagePane();

    // Onglet "Contacts"
    @FXML private Tab uiGestionsTab;
    private final GestionsTable uiGestionsTable = new GestionsTable();
    @FXML private Tab uiProprietesTab;
    private final ForeignParentPojoTable<ProprieteTroncon> uiProprietesTable = new ForeignParentPojoTable<>(ProprieteTroncon.class, "Période de propriété");
    @FXML private Tab uiGardesTab;
    private final ForeignParentPojoTable<GardeTroncon> uiGardesTable = new ForeignParentPojoTable<>(GardeTroncon.class, "Période de gardiennage");

    public FXTronconDiguePane(final TronconDigue troncon) {
        SIRS.loadFXML(this, TronconDigue.class);
        Injector.injectDependencies(this);

        //mode edition
        previewRepository = Injector.getBean(Session.class).getPreviews();
        elementProperty().addListener(this::initFields);

        uiMode.requireEditionForElement(troncon);
        uiMode.setSaveAction(this::save);
        disableFieldsProperty().bind(uiMode.editionState().not());

        uiValidityPeriod.disableFieldsProperty().bind(disableFieldsProperty());
        uiValidityPeriod.targetProperty().bind(elementProperty());

	/*
	 * Disabling rules.
	 */
        ui_libelle.disableProperty().bind(disableFieldsProperty());
        ui_commentaire.disableProperty().bind(disableFieldsProperty());
        ui_digueId.disableProperty().bind(disableFieldsProperty());
        ui_positionId.disableProperty().bind(disableFieldsProperty());
        ui_typeRiveId.disableProperty().bind(disableFieldsProperty());
        ui_typeTronconId.disableProperty().bind(disableFieldsProperty());
        ui_systemeRepDefautId.disableProperty().bind(disableFieldsProperty());

        srController.editableProperty().bind(disableFieldsProperty().not());
        uiSRAdd.disableProperty().set(true);
        uiSRAdd.setVisible(false);
        uiSRDelete.disableProperty().set(true);
        uiSRDelete.setVisible(false);

        uiGestionsTable.editableProperty().bind(disableFieldsProperty().not());
        uiProprietesTable.editableProperty().bind(disableFieldsProperty().not());
        uiGardesTable.editableProperty().bind(disableFieldsProperty().not());

        // Troncon change listener
        elementProperty.addListener(this::initFields);
        setElement(troncon);

        // Layout
        uiSrTab.setCenter(srController);
        uiSRDelete.setGraphic(new ImageView(SIRS.ICON_TRASH_WHITE));
        uiSRAdd.setGraphic(new ImageView(SIRS.ICON_ADD_WHITE));

        uiSRList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        uiSRList.setCellFactory(new Callback<ListView<SystemeReperage>, ListCell<SystemeReperage>>() {
            @Override
            public ListCell<SystemeReperage> call(ListView<SystemeReperage> param) {
                return new ListCell(){
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(null);
                        if(!empty && item!=null){
                            setText(((SystemeReperage)item).getLibelle());
                        }else{
                            setText("");
                        }
                    }
                };
            }
        });

        uiSRList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SystemeReperage>() {
            @Override
            public void changed(ObservableValue<? extends SystemeReperage> observable, SystemeReperage oldValue, SystemeReperage newValue) {
                srController.getSystemeReperageProperty().set(newValue);
            }
        });

        uiGestionsTab.setContent(uiGestionsTable);
        uiProprietesTab.setContent(uiProprietesTable);
        uiGardesTab.setContent(uiGardesTable);
    }

    public ObjectProperty<TronconDigue> tronconProperty(){
        return elementProperty;
    }

    public TronconDigue getTroncon(){
        return elementProperty.get();
    }

    @FXML
    private void srAdd(ActionEvent event) {
        final SystemeReperageRepository repo = (SystemeReperageRepository) session.getRepositoryForClass(SystemeReperage.class);

        final TronconDigue troncon = elementProperty.get();
        final SystemeReperage sr = Injector.getSession().getElementCreator().createElement(SystemeReperage.class);
        sr.setLibelle("Nouveau SR");
        sr.setLinearId(troncon.getId());
        repo.add(sr, troncon);

        //maj de la liste
        final List<SystemeReperage> srs = repo.getByLinear(troncon);
        uiSRList.setItems(FXCollections.observableArrayList(srs));
    }

    @FXML
    private void srDelete(ActionEvent event) {
        final SystemeReperage sr = uiSRList.getSelectionModel().getSelectedItem();
        if(sr==null) return;

        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Confirmer la suppression ?",
                ButtonType.NO, ButtonType.YES);
        alert.setResizable(true);

        final ButtonType res = alert.showAndWait().get();
        if(ButtonType.YES != res) return;

        final TronconDigue troncon = elementProperty.get();

        //suppression du SR
        final SystemeReperageRepository repo = (SystemeReperageRepository) session.getRepositoryForClass(SystemeReperage.class);
        repo.remove(sr, troncon);

        //maj de la liste
        final List<SystemeReperage> srs = repo.getByLinear(troncon);
        uiSRList.setItems(FXCollections.observableArrayList(srs));
    }

    @FXML
    private void showOnMap() {
        final FXMapTab tab = session.getFrame().getMapTab();

        tab.getMap().focusOnElement(elementProperty.get());
        tab.show();
    }

    private void save(){
        preSave();
        srController.save();
        session.getRepositoryForClass(TronconDigue.class).update(getTroncon());
    }

    private void initFields(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newElement) {

        // Unbind fields bound to previous element.
        if (oldValue != null) {
        // Propriétés de TronconDigue
            ui_libelle.textProperty().unbindBidirectional(oldValue.libelleProperty());
            uiPseudoId.textProperty().unbindBidirectional(oldValue.designationProperty());
        }

        if (newElement != null) {
            //this.uiName.textProperty().bindBidirectional(newElement.libelleProperty());
            uiMode.authorIDProperty().bind(newElement.authorProperty());
            Injector.getSession().getPrintManager().prepareToPrint(newElement);

            uiPseudoId.textProperty().bindBidirectional(newElement.designationProperty());

            date_maj.valueProperty().bind(newElement.dateMajProperty());

            ui_libelle.textProperty().bindBidirectional(newElement.libelleProperty());
            // * commentaire
            ui_commentaire.setHtmlText(newElement.getCommentaire());

            SIRS.initCombo(ui_digueId, FXCollections.observableArrayList(
                    previewRepository.getByClass(Digue.class)),
                    newElement.getDigueId() == null ? null : previewRepository.get(newElement.getDigueId()));
            SIRS.initCombo(ui_typeRiveId, FXCollections.observableArrayList(
                    previewRepository.getByClass(RefRive.class)),
                    newElement.getTypeRiveId() == null ? null : previewRepository.get(newElement.getTypeRiveId()));
            SIRS.initCombo(ui_typeTronconId, FXCollections.observableArrayList(
                    previewRepository.getByClass(RefTypeTroncon.class)),
                    newElement.getTypeTronconId() == null ? null : previewRepository.get(newElement.getTypeTronconId()));


            final SystemeReperageRepository srRepo = Injector.getBean(SystemeReperageRepository.class);
            final SystemeReperage defaultSR = newElement.getSystemeRepDefautId() == null? null : srRepo.get(newElement.getSystemeRepDefautId());;
            final ObservableList<SystemeReperage> srList = FXCollections.observableArrayList(srRepo.getByLinear(newElement));

            SIRS.initCombo(ui_systemeRepDefautId, srList, defaultSR);

            //liste des systemes de reperage
            uiSRList.setItems(srList);
            uiGestionsTable.setParentElement(newElement);
            uiGestionsTable.setTableItems(() -> (ObservableList) newElement.gestions);
            uiProprietesTable.setForeignParentId(newElement.getId());
            uiProprietesTable.setTableItems(() -> (ObservableList) FXCollections.observableList(session.getProprietesByTronconId(newElement.getId())));
            uiGardesTable.setForeignParentId(newElement.getId());
            uiGardesTable.setTableItems(() -> (ObservableList) FXCollections.observableList(session.getGardesByTronconId(newElement.getId())));
        }
    }

    @Override
    public void preSave() {
        final TronconDigue element = (TronconDigue) elementProperty().get();
        element.setCommentaire(ui_commentaire.getHtmlText());

        Object cbValue;
        cbValue = ui_digueId.getValue();
        if (cbValue instanceof Preview) {
            element.setDigueId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setDigueId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setDigueId(null);
        }
        cbValue = ui_typeRiveId.getValue();
        if (cbValue instanceof Preview) {
            element.setTypeRiveId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setTypeRiveId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setTypeRiveId(null);
        }
        cbValue = ui_typeTronconId.getValue();
        if (cbValue instanceof Preview) {
            element.setTypeTronconId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setTypeTronconId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setTypeTronconId(null);
        }
        cbValue = ui_systemeRepDefautId.getValue();
        if (cbValue instanceof Preview) {
            element.setSystemeRepDefautId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setSystemeRepDefautId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setSystemeRepDefautId(null);
        }
    }

    private final class GestionsTable extends PojoTable{

        public GestionsTable() {
            super(GestionTroncon.class, "Périodes de gestion");
        }

        @Override
        protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
            //on ne sauvegarde pas, le formulaire conteneur s'en charge
        }
    }
}
