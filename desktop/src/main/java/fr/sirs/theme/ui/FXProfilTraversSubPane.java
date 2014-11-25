
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
    
    private final BooleanProperty disableFields = new SimpleBooleanProperty();
    private final BooleanProperty tronconChanged = new SimpleBooleanProperty(false);
    
    private final ProfilTraversRepository profilTraversRepository;
    private final LeveProfilTraversTable levesTable = new LeveProfilTraversTable();
    
    
    @FXML private TextField uiLibelle;
    @FXML private VBox uiVbox;
    
    private FXProfilTraversSubPane(){
        SIRS.loadFXML(this);
        final Session session = Injector.getBean(Session.class);
        profilTraversRepository = session.getProfilTraversRepository();
    }
    
    public FXProfilTraversSubPane(final ProfilTravers profilTravers){
        this();
        this.profilTravers = profilTravers;
        initFields();
    }       
            
    private void initFields(){
        
        uiLibelle.textProperty().bindBidirectional(profilTravers.libelleProperty());
        uiLibelle.disableProperty().bind(disableFields);
        
        final List<LeveeProfilTravers> leves = profilTravers.getLeveeIds();
        uiVbox.getChildren().add(levesTable);
        levesTable.updateTable();
        levesTable.editableProperty().bind(disableFields.not());
        
    }

    @Override
    public BooleanProperty disableFieldsProperty() {
        return disableFields;
    }

    @Override
    public void preSave() {
        profilTravers.setDateMaj(LocalDateTime.now());
        profilTraversRepository.update(profilTravers);
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
