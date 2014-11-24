
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.ProfilTraversRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LeveeProfilTravers;
import fr.sirs.core.model.ProfilTravers;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXProfilTraversSubPane extends BorderPane implements ThemePane {
    
    private ProfilTravers profilTravers;
    private ObservableList<LeveeProfilTravers> leves;
//    private final ObjectProperty<Positionable> cretePotitionable = new SimpleObjectProperty<>();
    private final BooleanProperty disableFields = new SimpleBooleanProperty();
    private final BooleanProperty tronconChanged = new SimpleBooleanProperty(false);
    
    private final ProfilTraversRepository profilTraversRepository;
    private final LeveProfilTraversTable levesTable = new LeveProfilTraversTable();
    
//    // Propriétés de Positionnable
//    @FXML FXPositionnablePane uiPositionnable;
//    
//    // Propriétés de Structure
//    @FXML HTMLEditor uiComment;
//    @FXML FXDateField uiDebut;
//    @FXML FXDateField uiFin;
//    @FXML ComboBox<TronconDigue> uiTroncons;
//    
//    // Propriétés de Crête
//    @FXML FXNumberSpinner uiEpaisseur;
//    @FXML FXNumberSpinner uiCouches;
    @FXML private TextField uiLibelle;
    @FXML private VBox uiVbox;
    
    private FXProfilTraversSubPane(){
        SIRS.loadFXML(this);
        final Session session = Injector.getBean(Session.class);
        profilTraversRepository = session.getProfilTraversRepository();
//        cretePotitionable.addListener((ObservableValue<? extends Positionable> observable, Positionable oldValue, Positionable newValue) -> {
//            initFields();
//        });
//        uiPositionnable.disableFieldsProperty().bind(disableFields);
    }
    
    public FXProfilTraversSubPane(final ProfilTravers profilTravers){
        this();
        this.profilTravers = profilTravers;
        initFields();
    }       
            
    private void initFields(){
        
//        // Propriétés héritées de Positionnable
//        uiPositionnable.positionableProperty().bindBidirectional(cretePotitionable);
//        final ProfilTravers crete = (Crete) cretePotitionable.get();
//        
//        // Propriétés héritées de Structure
//        final StringConverter<TronconDigue> tronconsConverter = new StringConverter<TronconDigue>() {
//
//            @Override
//            public String toString(TronconDigue object) {
//                if(object == null) return "Pas de tronçon.";
//                return object.getLibelle()+ " ("+object.getId()+ ")";
//            }
//
//            @Override
//            public TronconDigue fromString(String string) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//        };
//        
//        final ObservableList<TronconDigue> troncons = FXCollections.observableArrayList();
//        TronconDigue troncon=null;
//        
//        for(final TronconDigue t : profilTraversRepository.getAll()){
//            troncons.add(t);
//            if(t.getId().equals(crete.getTroncon())) troncon=t;
//        }
//        troncons.add(null);
//        uiTroncons.setConverter(tronconsConverter);
//        uiTroncons.setItems(troncons);
//        uiTroncons.setValue(troncon);
//        uiTroncons.disableProperty().bind(disableFields);
//        uiTroncons.valueProperty().addListener(new ChangeListener<TronconDigue>() {
//
//            @Override
//            public void changed(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) {
//                tronconChanged.set(true);
//            }
//        });
//        
//        uiComment.setHtmlText(crete.getCommentaire());
//        uiComment.disableProperty().bind(disableFields);
//        
//        uiDebut.valueProperty().bindBidirectional(crete.date_debutProperty());
//        uiDebut.disableProperty().bind(disableFields);
//        
//        uiFin.valueProperty().bindBidirectional(crete.date_finProperty());
//        uiFin.disableProperty().bind(disableFields);
        
        
        // Propriétés propres à la Crête
        uiLibelle.textProperty().bindBidirectional(profilTravers.libelleProperty());
        uiLibelle.disableProperty().bind(disableFields);
        
        final List<LeveeProfilTravers> leves = profilTravers.getLeveeIds();
        uiVbox.getChildren().add(levesTable);
        levesTable.updateTable();
        levesTable.editableProperty().bind(disableFields.not());
//        for(final LeveeProfilTravers leve : leves){
//            //bonjour
//            uiLeves.getChildren().add(new FXLeveProfilTraversSubPane(leve));
//            uiLeves.getChildren().add(new Separator(Orientation.HORIZONTAL));
//        }
//        uiEpaisseur.valueProperty().bindBidirectional(crete.epaisseurProperty());
//        uiEpaisseur.disableProperty().bind(disableFields);
//        
//        uiCouches.numberTypeProperty().set(NumberField.NumberType.Integer);
//        uiCouches.minValueProperty().set(0);
//        uiCouches.valueProperty().bindBidirectional(crete.num_coucheProperty());
//        uiCouches.disableProperty().bind(disableFields);
    }

    @Override
    public BooleanProperty disableFieldsProperty() {
        return disableFields;
    }

    @Override
    public void preSave() {
        profilTravers.setDateMaj(LocalDateTime.now());
        profilTraversRepository.update(profilTravers);
//        uiPositionnable.preSave();
//        if(uiTroncons.getValue()!=null){
//            crete.setTroncon(uiTroncons.getValue().getId());
//        } else {
//            crete.setTroncon(null);
//        }
    }

    @Override
    public BooleanProperty tronconChangedProperty() {
        return this.tronconChanged;
    }
    
    
    private void reloadLeves(){
        
        final List<LeveeProfilTravers> items = this.profilTravers.getLeveeIds();
        this.leves = FXCollections.observableArrayList();
        items.stream().forEach((item) -> {
            this.leves.add(item);
        });
    }
    
    

    private class LeveProfilTraversTable extends PojoTable {

        public LeveProfilTraversTable() {
            super(LeveeProfilTravers.class, "Liste des levés de profils en travers");
        }

        @Override
        protected void deletePojos(Element ... pojo) {
            
        }
        
        @Override
        protected void editPojo(Element pojo){
        final Tab tab = new Tab();
        
        Node content = new BorderPane();
        if (pojo instanceof LeveeProfilTravers){
            final Map<String, Object> resources = new HashMap<>();
            resources.put("profilTravers", profilTravers);
            content = new FXThemePane((LeveeProfilTravers) pojo, resources);
        }
        tab.setContent(content);
        
        
        tab.setText(pojo.getClass().getSimpleName());
        tab.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if(tab.isSelected()){
                    session.prepareToPrint(pojo);
                }
            }
        });
        session.getFrame().addTab(tab);
    }
        
        
        private void updateTable() {
            reloadLeves();
            if (leves == null) {
                setTableItems(FXCollections::emptyObservableList);
            } else {
            //JavaFX bug : sortable is not possible on filtered list
                // http://stackoverflow.com/questions/17958337/javafx-tableview-with-filteredlist-jdk-8-does-not-sort-by-column
                // https://javafx-jira.kenai.com/browse/RT-32091
                final SortedList sortedList = new SortedList(leves);
                setTableItems(()->sortedList);
                sortedList.comparatorProperty().bind(getUiTable().comparatorProperty());
            }
        }
        
        @Override
        protected void createPojo() {
            final LeveeProfilTravers leve = new LeveeProfilTravers();
            profilTravers.getLeveeIds().add(leve);
            updateTable();
        }
    }
}
