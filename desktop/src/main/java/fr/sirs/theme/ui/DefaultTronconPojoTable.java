
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.query.ElementHit;
import fr.sirs.theme.AbstractTronconTheme;
import fr.sirs.util.FXFreeTab;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;
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
            final Session session = Injector.getBean(Session.class);
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
            session.getTronconDigueRepository().update(trc);
        } catch (Exception ex) {
            Logging.getLogger(DefaultTronconPojoTable.class).log(Level.WARNING, null, ex);
        }
        return pojo;
    }
    
    
    
    @Override
    protected void editPojo(Object pojo){
        editElement(pojo, troncon.get().getId());
    }
    
    
        
    public static void editElement(Object pojo, final String tronconId){
        if (pojo instanceof ElementHit) {
            final ElementHit hit = (ElementHit) pojo;
            try {
                pojo = (Element) Injector.getSession().getConnector().get(hit.geteElementClass(), hit.getDocumentId());
            } catch (ClassNotFoundException ex) {
                SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        
        try {
            Injector.getSession().getFrame().addTab(openEditionTab(pojo, tronconId));
        } catch (Exception ex) {
            Dialog d = new Alert(Alert.AlertType.ERROR, "Impossible d'afficher un éditeur", ButtonType.OK);
            d.showAndWait();
            throw new UnsupportedOperationException("Failed to load panel : " + ex.getMessage(), ex);
            
        }
    }
    
    private static Tab openEditionTab(final Object pojo, final String tronconId){
        final FXFreeTab tab = new FXFreeTab();
        Node content = (Node) SIRS.generateEditionPane((Element)pojo);
        if (content==null) content = new BorderPane(new Label("Pas d'éditeur pour le type : " + pojo.getClass().getSimpleName()));

        tab.setContent(content);
        final Element ele = (Element) pojo;
        tab.setTextAbrege(generateEditionTabTitle(ele, tronconId));
        tab.setOnSelectionChanged((Event event) -> {
            if (tab.isSelected()) {
                 Injector.getSession().prepareToPrint(ele);
            }
        });
        return tab;
    }
    
    private static String generateEditionTabTitle(final Element element, final String tronconId){
        String title =  Injector.getSession().getPreviewLabelRepository().getPreview(element.getId());
        if(title==null){
            final ResourceBundle bundle = ResourceBundle.getBundle(element.getClass().getName());
            title = bundle.getString("class");
        }
        String tronconTitle = Injector.getSession().getPreviewLabelRepository().getPreview(tronconId);
        if(tronconTitle!=null){
            title+=" ("+tronconTitle+")";
        }
        return title;
    }
}
