package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.AbstractTheme;
import fr.sirs.theme.TronconTheme;
import fr.sirs.util.SimpleFXEditMode;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
 * @author Johann Sorel (Geomatys)
 */
public class FXTronconThemePane extends BorderPane {

    @FXML private BorderPane uiCenter;
    @FXML private ComboBox<Preview> uiLinearChoice;

    private final StringProperty linearIdProperty = new SimpleStringProperty();
    private final Session session = Injector.getBean(Session.class);

    public StringProperty linearIdProperty(){return linearIdProperty;}

    public FXTronconThemePane(TronconTheme.ThemeManager ... groups) {
        SIRS.loadFXML(this, FXTronconThemePane.class, null);

        if (groups.length==1) {
            final Parent content = createContent(groups[0]);
            uiCenter.setCenter(content);
            if(content instanceof BorderPane){
                content.requestFocus();
                final Node center = ((BorderPane)content).getCenter();
                center.requestFocus();
            }

        }else{
            final TabPane pane = new TabPane();
            pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            for(int i=0; i<groups.length; i++){
                final Tab tab = new Tab(groups[i].getName());
                tab.setContent(createContent(groups[i]));
                pane.getTabs().add(tab);
            }
            uiCenter.setCenter(pane);
        }

        uiLinearChoice.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Preview> observable, Preview oldValue, Preview newValue) -> {
            if (newValue != null) {
                linearIdProperty.set(newValue.getElementId());
            }
        });
        
        final ObservableList<Preview> linearPreviews = SIRS.observableList(session.getPreviews().getByClass(TronconDigue.class)).sorted();
        SIRS.initCombo(uiLinearChoice, linearPreviews, linearPreviews.isEmpty()? null : linearPreviews.get(0));

    }

    protected class TronconThemePojoTable<T extends AvecForeignParent> extends ForeignParentPojoTable<T>{

        private final TronconTheme.ThemeManager<T> group;

        public TronconThemePojoTable(TronconTheme.ThemeManager<T> group) {
            super(group.getDataClass(), group.getTableTitle());
            foreignParentIdProperty.addListener(this::updateTable);
            this.group = group;
        }

        private void updateTable(ObservableValue<? extends String> observable, String oldValue, String newValue){
            if(newValue==null || group==null) {
                setTableItems(FXCollections::emptyObservableList);
            } else {
                setTableItems(() -> (ObservableList) group.getExtractor().apply(newValue));
            }
        }
    }

    protected Parent createContent(AbstractTheme.ThemeManager manager) {
        final Separator separator = new Separator();
        separator.setVisible(false);
        final SimpleFXEditMode editMode = new SimpleFXEditMode();
        final HBox topPane = new HBox(separator, editMode);
        HBox.setHgrow(separator, Priority.ALWAYS);

        final TronconThemePojoTable table = new TronconThemePojoTable(manager);
        table.setDeletor(manager.getDeletor());
        table.editableProperty.bind(editMode.editionState());
        table.foreignParentProperty().bindBidirectional(linearIdProperty);

        return new BorderPane(table, topPane, null, null, null);
    }
}
