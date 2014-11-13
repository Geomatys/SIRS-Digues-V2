
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.Injector;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.AbstractTronconTheme;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultTronconPojoTable extends AbstractPojoTable{
    
    private final ObjectProperty<TronconDigue> troncon = new SimpleObjectProperty<>();
    private final AbstractTronconTheme.ThemeGroup group;

    public DefaultTronconPojoTable(AbstractTronconTheme.ThemeGroup group) {
        super(group.getDataClass(),group.getTableTitle());
        this.group = group;
        
        final ChangeListener listener = (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) -> {
            updateTable();
        };
        
        troncon.addListener(listener);
    }
    
    public ObjectProperty<TronconDigue> tronconPropoerty(){
        return troncon;
    }
        
    private void updateTable(){
        final TronconDigue trc = troncon.get();
        if(trc==null || group==null){
            uiTable.setItems(FXCollections.emptyObservableList());
        }else{
            //JavaFX bug : sortable is not possible on filtered list
            // http://stackoverflow.com/questions/17958337/javafx-tableview-with-filteredlist-jdk-8-does-not-sort-by-column
            // https://javafx-jira.kenai.com/browse/RT-32091
            final SortedList sortedList = new SortedList(group.getExtractor().apply(trc));
            uiTable.setItems(sortedList);
            sortedList.comparatorProperty().bind(uiTable.comparatorProperty());
        }
    }

    @Override
    protected void deletePojos(Element ... pojos) {
        for(Element pojo : pojos){
            final TronconDigue trc = troncon.get();
            if(trc==null) return;
            group.getDeletor().delete(trc, pojo);
            //sauvegarde des modifications du troncon
            final Session session = Injector.getBean(Session.class);
            session.getTronconDigueRepository().update(trc);
        }
    }

    @Override
    protected void editPojo(Element pojo) {
        final Session session = Injector.getBean(Session.class);
        final Tab tab = new Tab();
        tab.setContent(new FXStructurePane((Objet) pojo));
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

    @Override
    protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
        final TronconDigue obj = troncon.get();
        final Session session = Injector.getBean(Session.class);
        session.getTronconDigueRepository().update(obj);
    }
    

    @Override
    protected void createPojo() {
    }
    
}
