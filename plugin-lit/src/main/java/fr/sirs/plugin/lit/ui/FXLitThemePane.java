
package fr.sirs.plugin.lit.ui;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.Lit;
import fr.sirs.theme.AbstractTheme;
import fr.sirs.theme.TronconTheme;
import fr.sirs.theme.ui.ForeignParentPojoTable;
import fr.sirs.util.SimpleFXEditMode;
import fr.sirs.util.SirsStringConverter;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author guilhem
 */
public class FXLitThemePane extends BorderPane {

    private final StringProperty linearIdProperty = new SimpleStringProperty();
    private final Session session = Injector.getBean(Session.class);

    public StringProperty linearIdProperty(){return linearIdProperty;}

    public FXLitThemePane(ComboBox<Lit> uiLinearChoice, TronconTheme.ThemeManager ... groups) {

        if (groups.length==1) {
            setCenter(createContent(groups[0]));
        }else{
            final TabPane pane = new TabPane();
            pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            for(int i=0; i<groups.length; i++){
                final Tab tab = new Tab(groups[i].getName());
                tab.setContent(createContent(groups[i]));
                pane.getTabs().add(tab);
            }
            setCenter(pane);
        }

        final List<Lit> linearPreviews = session.getRepositoryForClass(Lit.class).getAll();
        uiLinearChoice.setItems(FXCollections.observableList(linearPreviews));
        uiLinearChoice.setConverter(new SirsStringConverter());

        uiLinearChoice.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Lit> observable, Lit oldValue, Lit newValue) -> {
            linearIdProperty.set(newValue.getId());
        });

        if(!linearPreviews.isEmpty()){
            uiLinearChoice.getSelectionModel().select(linearPreviews.get(0));
        }
    }

    protected class LitThemePojoTable<T extends AvecForeignParent> extends ForeignParentPojoTable<T>{

        private final TronconTheme.ThemeManager<T> group;

        public LitThemePojoTable(TronconTheme.ThemeManager<T> group) {
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
        
        public BooleanProperty getEditableProperty() {
            return editableProperty;
        }
    }

    protected Parent createContent(AbstractTheme.ThemeManager manager) {
        final Separator separator = new Separator();
        separator.setVisible(false);
        final SimpleFXEditMode editMode = new SimpleFXEditMode();
        final HBox topPane = new HBox(separator, editMode);
        HBox.setHgrow(separator, Priority.ALWAYS);

        final LitThemePojoTable table = new LitThemePojoTable(manager);
        table.setDeletor(manager.getDeletor());
        table.getEditableProperty().bind(editMode.editionState());
        table.foreignParentProperty().bindBidirectional(linearIdProperty);

        return new BorderPane(table, topPane, null, null, null);
    }
}
