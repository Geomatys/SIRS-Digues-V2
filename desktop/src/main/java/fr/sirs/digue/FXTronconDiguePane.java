

package fr.sirs.digue;

import fr.sirs.FXEditMode;
import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.SIRS;
import static fr.sirs.core.model.Role.ADMIN;
import static fr.sirs.core.model.Role.EXTERN;
import static fr.sirs.core.model.Role.USER;
import fr.sirs.map.FXMapTab;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.model.ContactTroncon;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.RefRive;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.AbstractFXElementPane;
import fr.sirs.theme.ui.PojoTable;
import java.awt.geom.NoninvertibleTransformException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.util.StringConverter;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.util.FXDateField;
import org.opengis.referencing.operation.TransformException;
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
    @FXML private Tab uiContactTab;
    private final ContactTable uiContactTable = new ContactTable();
    
    //flag afin de ne pas faire de traitement lors de l'initialisation
    private boolean initializing = false;
    
    public FXTronconDiguePane() {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
        
        //mode edition
        uiMode.setAllowedRoles(ADMIN, USER, EXTERN);
        uiMode.setSaveAction(this::save);
        final BooleanProperty editBind = uiMode.editionState();
        final BooleanBinding editSR = Bindings.and(editBind, new SimpleBooleanProperty(session.getRole()!=USER));
        
        // Binding
        uiName.editableProperty().bind(editBind);
        uiDigue.disableProperty().bind(editBind.not());
        uiSrDefault.disableProperty().bind(editBind.not());
        uiRive.disableProperty().bind(editBind.not());
        
        uiDateStart.disableProperty().bind(editBind.not());
        uiDateEnd.disableProperty().bind(editBind.not());
        uiComment.disableProperty().bind(editBind.not());
        
        srController.editableProperty().bind(editSR);
        uiSRAdd.disableProperty().bind(editSR.not());
        uiSRDelete.disableProperty().bind(editSR.not());
        
        uiContactTable.editableProperty().bind(editBind);
        
        // Troncon change listener
        elementProperty.addListener((ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) -> {
            initFields();
        });
           
        // Layout
        uiSrTab.setCenter(srController);
        uiSRDelete.setGraphic(new ImageView(SIRS.ICON_TRASH));
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
        
        uiContactTab.setContent(uiContactTable);
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
        final SystemeReperage sr = new SystemeReperage();
        sr.setLibelle("Nouveau SR");
        sr.setTronconId(troncon.getId());
        repo.add(sr);
        
        //maj de la liste
        final List<SystemeReperage> srs = repo.getByTroncon(troncon);
        uiSRList.setItems(FXCollections.observableArrayList(srs));
    }

    @FXML
    private void srDelete(ActionEvent event) {
        final SystemeReperage sr = uiSRList.getSelectionModel().getSelectedItem();
        if(sr==null) return;
        
        final ButtonType res = new Alert(Alert.AlertType.CONFIRMATION,"Confirmer la suppression ?", 
                ButtonType.NO, ButtonType.YES).showAndWait().get();
        if(ButtonType.YES != res) return;
        
        //suppression du SR
        final SystemeReperageRepository repo = session.getSystemeReperageRepository();
        repo.remove(sr);
        
        //maj de la liste
        final TronconDigue troncon = elementProperty.get();
        final List<SystemeReperage> srs = repo.getByTroncon(troncon);
        uiSRList.setItems(FXCollections.observableArrayList(srs));
    }
    
    @FXML
    private void showOnMap(){
        final FXMapTab tab = session.getFrame().getMapTab();
        tab.show();
        final FXMap map = tab.getMap().getUiMap();
        try {
            map.getCanvas().setVisibleArea(JTS.toEnvelope(elementProperty.get().getGeometry()));
        } catch (NoninvertibleTransformException | TransformException ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
        }
    }
    
    private void save(){
        elementProperty.get().setCommentaire(uiComment.getHtmlText());
        srController.save();
        session.update(getTroncon());
    }
    
    private void initFields(){
        initializing = true;
        
        final TronconDigue troncon = elementProperty.get();
        
        this.uiName.textProperty().bindBidirectional(troncon.libelleProperty());
        this.uiComment.setHtmlText(troncon.getCommentaire());
                
        final ObservableList<Digue> allDigues = FXCollections.observableList(session.getDigueRepository().getAll());
        allDigues.add(0,null);
        this.uiDigue.setItems(allDigues);
        
        Digue currentDigue = null;
        if(troncon.getDigueId()!=null){
            currentDigue = session.getDigueById(troncon.getDigueId());
        }
        this.uiDigue.setConverter(new StringConverter<Digue>() {
            @Override
            public String toString(Digue digue) {
                if(digue==null) return "-";
                return digue.getLibelle();
            }
            @Override
            public Digue fromString(String string) {
                return null;
            }
        });
        this.uiDigue.setValue(currentDigue);
        this.uiDigue.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Digue>() {
            @Override
            public void changed(ObservableValue<? extends Digue> observable, Digue oldValue, Digue newValue) {
                if(initializing) return;
                Digue digue = null;
                if(troncon.getDigueId()!=null){
                    digue = session.getDigueById(troncon.getDigueId());
                }
                // Do not open dialog if the levee list is reset to the old value.
                if(!Objects.equals(newValue,digue)){
                    if(newValue==null){
                        troncon.setDigueId(null);
                    }else{
                        troncon.setDigueId(newValue.getId());
                    }
                }
            }
        });
        
        
        final ObservableList<RefRive> allRives = FXCollections.observableArrayList(session.getRefRiveRepository().getAll());
        allRives.add(0, null);
        uiRive.setItems(allRives);
        uiRive.setConverter(new StringConverter<RefRive>() {

            @Override
            public String toString(RefRive object) {
                return object==null ? "-" : object.getLibelle();
            }

            @Override
            public RefRive fromString(String string) {
                return null;
            }
        });
        RefRive currentRive = null;
        if(troncon.getTypeRiveId()!=null){
            currentRive = session.getRefRiveRepository().get(troncon.getTypeRiveId());
        }
        uiRive.setValue(currentRive);
        uiRive.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<RefRive>() {
            @Override
            public void changed(ObservableValue<? extends RefRive> observable, RefRive oldValue, RefRive newValue) {
                if(initializing) return;
                
                if(newValue==null) troncon.setTypeRiveId(null);
                else if(!Objects.equals(newValue.getId(),troncon.getTypeRiveId())){
                    troncon.setTypeRiveId(newValue.getId());
                }
            }
        });
        
        final List<SystemeReperage> allSrs = session.getSystemeReperageRepository().getByTroncon(troncon);
        allSrs.add(0, null);
        uiSrDefault.setItems(FXCollections.observableArrayList(allSrs));
        uiSrDefault.setConverter(new StringConverter<SystemeReperage>() {

            @Override
            public String toString(SystemeReperage object) {
                return object==null ? "-" : object.getLibelle();
            }

            @Override
            public SystemeReperage fromString(String string) {
                return null;
            }
        });
        final SystemeReperage srDefault = session.getSystemeReperageRepository().get(troncon.getSystemeRepDefautId());
        uiSrDefault.setValue(srDefault);
        uiSrDefault.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SystemeReperage>() {

            @Override
            public void changed(ObservableValue<? extends SystemeReperage> observable, SystemeReperage oldValue, SystemeReperage newValue) {
                if(initializing) return;
                
                if(newValue==null) troncon.setSystemeRepDefautId(null);
                else if(!Objects.equals(newValue.getId(),troncon.getSystemeRepDefautId())){
                    troncon.setSystemeRepDefautId(newValue.getId());
                }
            }
        });
        
        
        
        this.uiDateStart.valueProperty().bindBidirectional(troncon.date_debutProperty());
        this.uiDateEnd.valueProperty().bindBidirectional(troncon.date_finProperty());
                
        
        //liste des systemes de reperage
        uiSRList.setItems(FXCollections.observableArrayList(session.getSystemeReperageRepository().getByTroncon(troncon)));
        
        uiContactTable.setTableItems(() -> (ObservableList)troncon.contacts);
        
        initializing = false;
    }

    @Override
    public void preSave() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private final class ContactTable extends PojoTable{

        public ContactTable() {
            super(ContactTroncon.class, "Liste des contacts");
        }

        @Override
        protected void deletePojos(Element... pojos) {
            final TronconDigue troncon = elementProperty.get();
            for(Element ele : pojos){
                troncon.contacts.remove(ele);
            }
        }

        @Override
        protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
            //on ne sauvegarde pas, le formulaire conteneur s'en charge
        }

        @Override
        protected Object createPojo() {
            final ContactTroncon contact = new ContactTroncon();
            final TronconDigue troncon = elementProperty.get();
            troncon.contacts.add(contact);
            return contact;
        }
    }
}
