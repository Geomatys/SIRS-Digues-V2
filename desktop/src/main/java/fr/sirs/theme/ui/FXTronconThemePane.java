
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.PreviewLabel;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.AbstractTronconTheme;
import fr.sirs.util.SirsStringConverter;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
    @FXML private ComboBox<PreviewLabel> uiTronconChoice;
    private final SimpleObjectProperty<TronconDigue> currentTronconProperty = new SimpleObjectProperty<>();
    private final Session session = Injector.getBean(Session.class);
        
    public FXTronconThemePane(AbstractTronconTheme.ThemeManager ... groups) {
        SIRS.loadFXML(this);
        
        if(groups.length==1){
            final TronconThemePojoTable table = new TronconThemePojoTable(groups[0]);
            table.setDeletor(groups[0].getDeletor());
            table.editableProperty.bind(session.nonGeometryEditionProperty());
            table.tronconProperty().bindBidirectional(currentTronconProperty);
            uiCenter.setCenter(table);
        }else{
            final TabPane pane = new TabPane();
            pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            for(int i=0; i<groups.length; i++){
                final TronconThemePojoTable table = new TronconThemePojoTable(groups[i]);
                table.setDeletor(groups[i].getDeletor());
                table.tronconProperty().bindBidirectional(currentTronconProperty);
                final Tab tab = new Tab(groups[i].getName());
                tab.setContent(table);
                pane.getTabs().add(tab);
            }
            uiCenter.setCenter(pane);
        }
        
        final List<PreviewLabel> tronconPreviews = session.getPreviewLabelRepository().getPreviewLabels(TronconDigue.class);
        uiTronconChoice.setItems(FXCollections.observableList(tronconPreviews));        
        uiTronconChoice.setConverter(new SirsStringConverter());

        uiTronconChoice.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends PreviewLabel> observable, PreviewLabel oldValue, PreviewLabel newValue) -> {
            final TronconDigueRepository tronconDigueRepository = session.getTronconDigueRepository();
            currentTronconProperty.set(tronconDigueRepository.get(newValue.getObjectId()));
        });
        
        if(!tronconPreviews.isEmpty()){
            uiTronconChoice.getSelectionModel().select(tronconPreviews.get(0));
        }
    }
    
    public SimpleObjectProperty<TronconDigue> currentTronconProperty(){return currentTronconProperty;}
}
