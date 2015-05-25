package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.AbstractTronconTheme;
import fr.sirs.util.SirsStringConverter;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTronconThemePane extends BorderPane {

    @FXML private BorderPane uiCenter;
    @FXML private ComboBox<Preview> uiLinearChoice;
    private final StringProperty linearIdProperty = new SimpleStringProperty();
    private final Session session = Injector.getBean(Session.class);
        
    public FXTronconThemePane(AbstractTronconTheme.ThemeManager ... groups) {
        SIRS.loadFXML(this);
        
        if(groups.length==1){
            final TronconThemePojoTable table = new TronconThemePojoTable(groups[0]);
            table.setDeletor(groups[0].getDeletor());
            table.editableProperty.bind(session.nonGeometryEditionProperty());
            table.foreignParentProperty().bindBidirectional(linearIdProperty);
            uiCenter.setCenter(table);
        }else{
            final TabPane pane = new TabPane();
            pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            for(int i=0; i<groups.length; i++){
                final TronconThemePojoTable table = new TronconThemePojoTable(groups[i]);
                table.setDeletor(groups[i].getDeletor());
                table.foreignParentProperty().bindBidirectional(linearIdProperty);
                final Tab tab = new Tab(groups[i].getName());
                tab.setContent(table);
                pane.getTabs().add(tab);
            }
            uiCenter.setCenter(pane);
        }
        
        final List<Preview> linearPreviews = session.getPreviews().getByClass(TronconDigue.class);
        uiLinearChoice.setItems(FXCollections.observableList(linearPreviews));        
        uiLinearChoice.setConverter(new SirsStringConverter());

        uiLinearChoice.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Preview> observable, Preview oldValue, Preview newValue) -> {
            linearIdProperty.set(newValue.getElementId());
        });
        
        if(!linearPreviews.isEmpty()){
            uiLinearChoice.getSelectionModel().select(linearPreviews.get(0));
        }
    }
    
    private class TronconThemePojoTable<T extends AvecForeignParent> extends ForeignParentPojoTable<T>{

        private final AbstractTronconTheme.ThemeManager<T> group;
        
        public TronconThemePojoTable(AbstractTronconTheme.ThemeManager<T> group) {
            super(group.getDataClass(), group.getTableTitle());
            foreignParentIdProperty.addListener(this::updateTable);
            this.group = group;
        }
        
        private void updateTable(ObservableValue<? extends String> observable, String oldValue, String newValue){
            if(newValue==null || group==null) {
                setTableItems(FXCollections::emptyObservableList);
            } else {
                //JavaFX bug : sortable is not possible on filtered list
                // http://stackoverflow.com/questions/17958337/javafx-tableview-with-filteredlist-jdk-8-does-not-sort-by-column
                // https://javafx-jira.kenai.com/browse/RT-32091
//                setTableItems(() -> {
//                    final SortedList<T> sortedList = new SortedList<>(group.getExtractor().apply(newValue));
//                    sortedList.comparatorProperty().bind(getUiTable().comparatorProperty());
//                    return sortedList;
//                });
                setTableItems(() -> (ObservableList) group.getExtractor().apply(newValue));
            }
        }
    }
     
}
