package fr.sirs.theme.ui;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.AbstractTronconTheme;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @param <T>
 */
public abstract class TronconThemePojoTable<T extends Element> extends PojoTable {
    
    protected final ObjectProperty<TronconDigue> troncon = new SimpleObjectProperty<>();
    private final AbstractTronconTheme.ThemeGroup group;

    public TronconThemePojoTable(AbstractTronconTheme.ThemeGroup group) {
        super(group.getDataClass(), group.getTableTitle());
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
            setTableItems(FXCollections::emptyObservableList);
        }
        else{
            //JavaFX bug : sortable is not possible on filtered list
            // http://stackoverflow.com/questions/17958337/javafx-tableview-with-filteredlist-jdk-8-does-not-sort-by-column
            // https://javafx-jira.kenai.com/browse/RT-32091
            setTableItems(() -> {
                final SortedList sortedList = new SortedList(group.getExtractor().apply(trc));
                sortedList.comparatorProperty().bind(getUiTable().comparatorProperty());
                return sortedList;
            });
        }
    }

    @Override
    protected void deletePojos(Element ... pojos) {
        for(Element pojo : pojos){
            // Si l'utilisateur est un externe, il faut qu'il soit l'auteur de 
            // l'élément et que celui-ci soit invalide, sinon, on court-circuite
            // la suppression.
            if(!authoriseElementDeletion(pojo)) continue;
                
            final TronconDigue trc = troncon.get();
            if(trc==null) return;
            group.getDeletor().delete(trc, pojo);
            //sauvegarde des modifications du troncon
            session.getTronconDigueRepository().update(trc);
        }
    }

    @Override
    protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
        final TronconDigue obj = troncon.get();
        session.getTronconDigueRepository().update(obj);
    }
    
    @Override
    protected abstract T createPojo();
}
