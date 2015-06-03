package fr.sirs.digue;

import fr.sirs.FXEditMode;
import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.map.FXMapTab;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.GardeTroncon;
import fr.sirs.core.model.GestionTroncon;
import fr.sirs.core.model.ProprieteTroncon;
import fr.sirs.core.model.RefRive;
import fr.sirs.core.model.RefTypeTroncon;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.AbstractFXElementPane;
import fr.sirs.theme.ui.ForeignParentPojoTable;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.SirsStringConverter;
import java.util.List;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
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
import javafx.scene.control.ChoiceBox;
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
    
    // En-tete
    @FXML private FXEditMode uiMode;
    @FXML private TextField uiName;
    @FXML private ChoiceBox<Digue> uiDigue;
    @FXML private ChoiceBox<SystemeReperage> uiSrDefault;
    @FXML private ChoiceBox<RefTypeTroncon> uiTypeTroncon;
    @FXML private ChoiceBox<RefRive> uiRive;
    
    // Onglet "Description"
    @FXML private HTMLEditor uiComment;
    @FXML private FXDateField uiDateStart;
    @FXML private FXDateField uiDateEnd;
    
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
    
    //flag afin de ne pas faire de traitement lors de l'initialisation
    private boolean initializing = false;
    
    public FXTronconDiguePane(final TronconDigue troncon) {
        SIRS.loadFXML(this, TronconDigue.class);
        Injector.injectDependencies(this);
        
        //mode edition
        uiMode.requireEditionForElement(troncon);
        uiMode.setSaveAction(this::save);
        final BooleanProperty editBind = uiMode.editionState();
        final BooleanBinding editSR = Bindings.and(editBind, session.geometryEditionProperty());
        
        // Binding
        uiName.editableProperty().bind(editBind);
        uiDigue.disableProperty().bind(editBind.not());
        uiSrDefault.disableProperty().bind(editBind.not());
        uiRive.disableProperty().bind(editBind.not());
        
        uiDateStart.disableProperty().bind(editBind.not());
        uiDateEnd.disableProperty().bind(editBind.not());
        uiComment.disableProperty().bind(editBind.not());
        uiTypeTroncon.disableProperty().bind(editBind.not());
        
        srController.editableProperty().bind(editSR);
        uiSRAdd.disableProperty().bind(editSR.not());
        uiSRDelete.disableProperty().bind(editSR.not());
        
        uiGestionsTable.editableProperty().bind(editBind);
        uiProprietesTable.editableProperty().bind(editBind);
        uiGardesTable.editableProperty().bind(editBind);
        
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
        final SystemeReperageRepository repo = session.getSystemeReperageRepository();
        
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
        
        final ButtonType res = new Alert(Alert.AlertType.CONFIRMATION,"Confirmer la suppression ?", 
                ButtonType.NO, ButtonType.YES).showAndWait().get();
        if(ButtonType.YES != res) return;
        
        final TronconDigue troncon = elementProperty.get();
        
        //suppression du SR
        final SystemeReperageRepository repo = session.getSystemeReperageRepository();
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
        elementProperty.get().setCommentaire(uiComment.getHtmlText());
        srController.save();
        session.getTronconDigueRepository().update(getTroncon());
    }
    
    private void initFields(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) {
        initializing = true;

        // TODO : if new tronçon is null, we should just clean nodes.
        if (oldValue != null) {
            this.uiName.textProperty().unbindBidirectional(oldValue.libelleProperty());
            this.uiDateStart.valueProperty().unbindBidirectional(oldValue.date_debutProperty());
            this.uiDateEnd.valueProperty().unbindBidirectional(oldValue.date_finProperty());
        }

        if (newValue != null) {
            this.uiName.textProperty().bindBidirectional(newValue.libelleProperty());
            this.uiComment.setHtmlText(newValue.getCommentaire());

            final ObservableList<Digue> allDigues = FXCollections.observableList(session.getDigueRepository().getAll());
            allDigues.add(0, null);
            this.uiDigue.setItems(allDigues);
            Digue currentDigue = null;
            if (newValue.getDigueId() != null) {
                currentDigue = session.getDigueRepository().get(newValue.getDigueId());
            }
            this.uiDigue.setConverter(new SirsStringConverter());
            this.uiDigue.setValue(currentDigue);

            this.uiDigue.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Digue>() {
                @Override
                public void changed(ObservableValue<? extends Digue> observableDigue, Digue oldDigue, Digue newDigue) {
                    if (initializing) {
                        return;
                    }
                    Digue digue = null;
                    if (newValue.getDigueId() != null) {
                        digue = session.getDigueRepository().get(newValue.getDigueId());
                    }
                    // Do not open dialog if the levee list is reset to the old value.
                    if (!Objects.equals(newDigue, digue)) {
                        if (newDigue == null) {
                            newValue.setDigueId(null);
                        } else {
                            newValue.setDigueId(newDigue.getId());
                        }
                    }
                }
            });

            final ObservableList<RefRive> allRives = FXCollections.observableArrayList(session.getRefRiveRepository().getAll());
            allRives.add(0, null);
            uiRive.setItems(allRives);
            uiRive.setConverter(new SirsStringConverter());
            RefRive currentRive = null;
            if (newValue.getTypeRiveId() != null) {
                currentRive = session.getRefRiveRepository().get(newValue.getTypeRiveId());
            }
            uiRive.setValue(currentRive);
            uiRive.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<RefRive>() {
                @Override
                public void changed(ObservableValue<? extends RefRive> observableRive, RefRive oldRive, RefRive newRive) {
                    if (initializing) {
                        return;
                    }
                    if (newRive == null) {
                        newValue.setTypeRiveId(null);
                    } else if (!Objects.equals(newRive.getId(), newValue.getTypeRiveId())) {
                        newValue.setTypeRiveId(newRive.getId());
                    }
                }
            });

            final List<SystemeReperage> allSrs = session.getSystemeReperageRepository().getByLinear(newValue);
            allSrs.add(0, null);
            uiSrDefault.setItems(FXCollections.observableArrayList(allSrs));
            uiSrDefault.setConverter(new SirsStringConverter());

            String defaultSRID = newValue.getSystemeRepDefautId();
            if (defaultSRID != null && !defaultSRID.isEmpty()) {
                final SystemeReperage srDefault = session.getSystemeReperageRepository().get(defaultSRID);
                uiSrDefault.setValue(srDefault);
            }
            uiSrDefault.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SystemeReperage>() {

                @Override
                public void changed(ObservableValue<? extends SystemeReperage> observableSR, SystemeReperage oldSR, SystemeReperage newSR) {
                    if (initializing) {
                        return;
                    }
                    if (newSR == null) {
                        newValue.setSystemeRepDefautId(null);
                    } else if (!Objects.equals(newSR.getId(), newValue.getSystemeRepDefautId())) {
                        newValue.setSystemeRepDefautId(newSR.getId());
                    }
                }
            });

            final List<RefTypeTroncon> allTronconTypes = session.getRefTypeTronconRepository().getAll();
            allTronconTypes.add(0, null);
            uiTypeTroncon.setItems(FXCollections.observableArrayList(allTronconTypes));
            uiTypeTroncon.setConverter(new SirsStringConverter());

            final RefTypeTroncon typeTroncon;
            if (newValue.getTypeTronconId() != null) {
                typeTroncon = session.getRefTypeTronconRepository().get(newValue.getTypeTronconId());
            } else {
                typeTroncon = null;
            }
            uiTypeTroncon.setValue(typeTroncon);
            uiTypeTroncon.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<RefTypeTroncon>() {

                @Override
                public void changed(ObservableValue<? extends RefTypeTroncon> observableType, RefTypeTroncon oldType, RefTypeTroncon newType) {
                    if (initializing) {
                        return;
                    }
                    if (newType == null) {
                        newValue.setTypeTronconId(null);
                    } else if (!Objects.equals(newType.getId(), newValue.getTypeTronconId())) {
                        newValue.setTypeTronconId(newType.getId());
                    }
                }
            });

            this.uiDateStart.valueProperty().bindBidirectional(newValue.date_debutProperty());
            this.uiDateEnd.valueProperty().bindBidirectional(newValue.date_finProperty());

            //liste des systemes de reperage
            uiSRList.setItems(FXCollections.observableArrayList(session.getSystemeReperageRepository().getByLinear(newValue)));
            uiGestionsTable.setParentElement(newValue);
            uiGestionsTable.setTableItems(() -> (ObservableList) newValue.gestions);
            uiProprietesTable.setForeignParentId(newValue.getId());
            uiProprietesTable.setTableItems(() -> (ObservableList) FXCollections.observableList(session.getProprietesByTronconId(newValue.getId())));
            uiGardesTable.setForeignParentId(newValue.getId());
            uiGardesTable.setTableItems(() -> (ObservableList) FXCollections.observableList(session.getGardesByTronconId(newValue.getId())));

        }
        initializing = false;
    }

    @Override
    public void preSave() {
        // Nothing to do ?
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
