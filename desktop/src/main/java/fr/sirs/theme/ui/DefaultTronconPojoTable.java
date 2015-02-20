
package fr.sirs.theme.ui;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Role;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.AbstractTronconTheme;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import org.apache.sis.util.logging.Logging;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultTronconPojoTable extends PojoTable {
    
    private final ObjectProperty<TronconDigue> troncon = new SimpleObjectProperty<>();
    private final AbstractTronconTheme.ThemeGroup group;

    public DefaultTronconPojoTable(AbstractTronconTheme.ThemeGroup group) {
        super(group.getDataClass(), group.getTableTitle());
        this.group = group;
        
        final ChangeListener listener = (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) -> {
            updateTable();
//            if(uiFicheMode.isSelected()){
//                ficheModeProperty.set(false);
//                navigationToolbar.setVisible(false);
//                setCenter(uiTable);
//                editableProperty.setValue(true);
//                uiFicheMode.setGraphic(playIcon);
//            }
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
    protected Object createPojo() {
        Objet pojo = null;
        try {
            final TronconDigue trc = troncon.get();
            final Constructor pojoConstructor = pojoClass.getConstructor();
            pojo = (Objet) pojoConstructor.newInstance();
            trc.getStructures().add(pojo);
            pojo.setParent(trc);
            pojo.setAuthor(session.getUtilisateur().getId());
            pojo.setValid(!(session.getRole().equals(Role.EXTERN)));
            session.getTronconDigueRepository().update(trc);
        } catch (Exception ex) {
            Logging.getLogger(DefaultTronconPojoTable.class).log(Level.WARNING, null, ex);
        }
        return pojo;
    }
}
