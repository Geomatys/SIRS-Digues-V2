package fr.sirs.theme.ui;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.AbstractTronconTheme;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @param <T>
 */
public class TronconThemePojoTable<T extends Element> extends PojoTable {
    
    protected final SimpleObjectProperty<TronconDigue> tronconProperty = new SimpleObjectProperty<>();
    private final AbstractTronconTheme.ThemeManager<T> group;

    public TronconThemePojoTable(AbstractTronconTheme.ThemeManager<T> group) {
        super(group.getDataClass(), group.getTableTitle());
        this.group = group;
        tronconProperty.addListener(this::updateTable);
    }
    
    public SimpleObjectProperty<TronconDigue> tronconProperty(){
        return tronconProperty;
    }
        
    private void updateTable(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue){
        if(newValue==null || group==null) {
            setTableItems(FXCollections::emptyObservableList);
        } else {
            //JavaFX bug : sortable is not possible on filtered list
            // http://stackoverflow.com/questions/17958337/javafx-tableview-with-filteredlist-jdk-8-does-not-sort-by-column
            // https://javafx-jira.kenai.com/browse/RT-32091
//            setTableItems(() -> {
//                final SortedList sortedList = new SortedList(group.getExtractor().apply(newValue));
//                sortedList.comparatorProperty().bind(getUiTable().comparatorProperty());
//                return sortedList;
//            });
            setTableItems(() -> (ObservableList) group.getExtractor().apply(newValue));
        }
    }
}
