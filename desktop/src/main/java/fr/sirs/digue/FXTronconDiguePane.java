

package fr.sirs.digue;

import fr.sirs.FXEditMode;
import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.SIRS;
import static fr.sirs.Session.Role.ADMIN;
import static fr.sirs.Session.Role.EXTERNE;
import static fr.sirs.Session.Role.USER;
import fr.sirs.map.FXMapTab;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.model.ContactTroncon;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.FXElementPane;
import fr.sirs.theme.ui.PojoTable;
import java.awt.geom.NoninvertibleTransformException;
import java.util.List;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.control.Label;
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
public class FXTronconDiguePane extends BorderPane implements FXElementPane{
    
    private final ObjectProperty<TronconDigue> tronconProperty = new SimpleObjectProperty<>();
    
    @Autowired
    private Session session;
    
    @FXML private Label uiId;
    @FXML private TextField uiName;
    @FXML private HTMLEditor uiComment;
    @FXML private ChoiceBox<Digue> uiDigue;
    @FXML private ChoiceBox<Digue> uiSrDefault;
    @FXML private ChoiceBox<String> uiRive;
    @FXML private FXDateField uiDateStart;
    @FXML private FXDateField uiDateEnd;
    
    @FXML private FXEditMode uiMode;
    @FXML private BorderPane uiSrTab;
    @FXML private Button uiSRDelete;
    @FXML private Button uiSRAdd;
    @FXML private ListView<SystemeReperage> uiSRList;
    @FXML private Tab uiContactTab;
    
    private final FXSystemeReperagePane srController = new FXSystemeReperagePane();
    private final ContactTable uiContactTable = new ContactTable();
    
    //flag afin de ne pas faire de traitement lors de l'initialisation
    private boolean initializing = false;
    
    public FXTronconDiguePane() {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
        
        //mode edition
        uiMode.setAllowedRoles(ADMIN, USER, EXTERNE);
        uiMode.setSaveAction(this::save);
        final BooleanProperty editBind = uiMode.editionState();
        uiName.editableProperty().bind(editBind);
        uiDigue.disableProperty().bind(editBind.not());
        uiSrDefault.disableProperty().bind(editBind.not());
        uiRive.disableProperty().bind(editBind.not());
        uiDateStart.disableProperty().bind(editBind.not());
        uiDateEnd.disableProperty().bind(editBind.not());
        uiComment.disableProperty().bind(editBind.not());
        uiContactTable.editableProperty().bind(editBind);
        srController.editableProperty().bind(editBind);
        uiSRAdd.disableProperty().bind(editBind.not());
        uiSRDelete.disableProperty().bind(editBind.not());
        
        tronconProperty.addListener((ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) -> {
            initFields();
        });
           
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
        return tronconProperty;
    }
    
    public TronconDigue getTroncon(){
        return tronconProperty.get();
    }
    
    @Override
    public void setElement(Element troncon){
        this.tronconProperty.set((TronconDigue) troncon);
    }
        
    @FXML
    private void srAdd(ActionEvent event) {
        final SystemeReperageRepository repo = session.getSystemeReperageRepository();
        
        final TronconDigue troncon = tronconProperty.get();
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
        final TronconDigue troncon = tronconProperty.get();
        final List<SystemeReperage> srs = repo.getByTroncon(troncon);
        uiSRList.setItems(FXCollections.observableArrayList(srs));
    }
    
    @FXML
    private void showOnMap(){
        final FXMapTab tab = session.getFrame().getMapTab();
        tab.show();
        final FXMap map = tab.getMap().getUiMap();
        try {
            map.getCanvas().setVisibleArea(JTS.toEnvelope(tronconProperty.get().getGeometry()));
        } catch (NoninvertibleTransformException | TransformException ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
        }
    }
    
    private void save(){
        tronconProperty.get().setCommentaire(uiComment.getHtmlText());
        srController.save();
        session.update(getTroncon());
    }
    
    private void initFields(){
        initializing = true;
        
        final TronconDigue troncon = tronconProperty.get();
        final ObservableList<Digue> allDigues = FXCollections.observableList(session.getDigueRepository().getAll());
        final Digue digue = session.getDigueById(troncon.getDigueId());
        
        this.uiId.setText(troncon.getId());
        this.uiName.textProperty().bindBidirectional(troncon.libelleProperty());
        this.uiComment.setHtmlText(troncon.getCommentaire());
                
        this.uiDigue.setItems(allDigues);
        final StringConverter<Digue> digueStringConverter = new StringConverter<Digue>() {
            @Override
            public String toString(Digue digue) {return digue.getLibelle();}
            @Override
            public Digue fromString(String string) {return null;}
        };
        
        this.uiDigue.setConverter(digueStringConverter);
        this.uiDigue.setValue(digue);
        this.uiDigue.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Digue>() {

            @Override
            public void changed(ObservableValue<? extends Digue> observable, Digue oldValue, Digue newValue) {
                if(initializing) return;
                /* TODO ?
                Le changement de digue d'un tronçon a des implication immédiates
                sur la mise à jour de l'arbre de navigation des entités. Il 
                entraîne a priori le changement d'élément de l'arbre en cours de
                sélection et donc le chargement de la vue d'un nouvel élément 
                avant d'avoir eu la possibilité d'enregistrer les changements
                apportés au modèle manuellement dans la base.
                Il y a donc deux possibilités :
                1- Ouvrir ici une fenêtre d'avertissement expliquant que cette 
                modification est sauvegardée d'office car elle provoque un 
                changement de vue de digue. (choix pour le moment).
                2- Rester sur la vue du bon tronçon en sélectionnant le bon 
                élément de l'arbre et en contournant le rechargement de la page
                (plus compliqué mais plus léger pour l'utilisateur).
                */
                
                // Do not open dialog if the levee list is reset to the old value.
                if (!newValue.equals(digue)){
                    
                    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Le changement de digue est enregistré d'office.",
                            ButtonType.OK,ButtonType.CANCEL
                            );
                    final ButtonType res = alert.showAndWait().get();
                    if(res==ButtonType.OK){
                        tronconProperty.get().setDigueId(newValue.getId());
                        save();
                    }
                }
            }
        });
        this.uiDigue.getValue().getId();
        
        this.uiRive.setValue(troncon.getTypeRiveId());
                
        this.uiDateStart.valueProperty().bindBidirectional(troncon.date_debutProperty());
        this.uiDateEnd.valueProperty().bindBidirectional(troncon.date_finProperty());
                
        
        //liste des systemes de reperage
        final SystemeReperageRepository repo = session.getSystemeReperageRepository();
        final List<SystemeReperage> srs = repo.getByTroncon(troncon);
        uiSRList.setItems(FXCollections.observableArrayList(srs));
        
        uiContactTable.setTableItems(() -> (ObservableList)troncon.contacts);
        
        initializing = false;
    }
    
    private final class ContactTable extends PojoTable{

        public ContactTable() {
            super(ContactTroncon.class, "Liste des contacts", true);
        }

        @Override
        protected void deletePojos(Element... pojos) {
            final TronconDigue troncon = tronconProperty.get();
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
            final TronconDigue troncon = tronconProperty.get();
            troncon.contacts.add(contact);
            return contact;
        }
    }
}
