
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.DocumentTroncon;
import fr.sirs.core.model.PreviewLabel;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.AbstractTronconTheme;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTronconThemePane extends BorderPane {

    @FXML private BorderPane uiCenter;
    @FXML private ComboBox<PreviewLabel> uiTronconChoice;
    private final ObjectProperty<TronconDigue> currentTronconProperty = new SimpleObjectProperty<>();
    private final Session session = Injector.getBean(Session.class);
        
    public FXTronconThemePane(AbstractTronconTheme.ThemeGroup ... groups) {
        SIRS.loadFXML(this);
        
        if(groups.length==1){
            final TronconThemePojoTable table = getPojoTable(groups[0]);
            table.editableProperty.bind(session.nonGeometryEditionProperty());
            table.tronconPropoerty().bindBidirectional(currentTronconProperty);
            uiCenter.setCenter(table);
        }else{
            final TabPane pane = new TabPane();
            pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            for(int i=0; i<groups.length; i++){
                final TronconThemePojoTable table = getPojoTable(groups[i]);
                table.tronconPropoerty().bindBidirectional(currentTronconProperty);
                final Tab tab = new Tab(groups[i].getName());
                tab.setContent(table);
                pane.getTabs().add(tab);
            }
            uiCenter.setCenter(pane);
        }
        
        final List<PreviewLabel> tronconPreviews = session.getPreviewLabelRepository().getPreviewLabels(TronconDigue.class);
        uiTronconChoice.setItems(FXCollections.observableList(tronconPreviews));        
        uiTronconChoice.setConverter(new StringConverter<PreviewLabel>() {
            @Override
            public String toString(PreviewLabel object) {
                if(object==null) return "";
                else return object.getLabel();
            }
            
            @Override
            public PreviewLabel fromString(String string) {
                if(string==null) return null;
                final ObservableList<PreviewLabel> items = uiTronconChoice.getItems();
                for(PreviewLabel troncon : items){
                    if(troncon!=null && troncon.getLabel().toLowerCase().startsWith(string.toLowerCase())){
                        return troncon;
                    }
                }
                return null;
            }
        });
        

        uiTronconChoice.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                final TronconDigueRepository tronconDigueRepository = session.getTronconDigueRepository();
                currentTronconProperty.set(tronconDigueRepository.get(uiTronconChoice.getSelectionModel().getSelectedItem().getObjectId()));
            }
        });
        
        if(!tronconPreviews.isEmpty()){
            uiTronconChoice.getSelectionModel().select(tronconPreviews.get(0));
        }
    }
    
    public ObjectProperty<TronconDigue> currentTronconProperty(){return currentTronconProperty;}
    
    private TronconThemePojoTable getPojoTable(final AbstractTronconTheme.ThemeGroup group){
        if(group.getDataClass()==DocumentTroncon.class) return new TronconThemeDocumentTronconPojoTable(group);
        else return new TronconThemeObjetPojoTable(group);
    }
}
