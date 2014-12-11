
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.AbstractTronconTheme;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import org.geotoolkit.gui.javafx.util.ComboBoxCompletion;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTronconThemePane extends BorderPane {

    @FXML private BorderPane uiCenter;
    @FXML private ComboBox<TronconDigue> uiTronconChoice;
        
    public FXTronconThemePane(AbstractTronconTheme.ThemeGroup ... groups) {
        SIRS.loadFXML(this);
        
        if(groups.length==1){
            final DefaultTronconPojoTable table = new DefaultTronconPojoTable(groups[0]);
            table.tronconPropoerty().bindBidirectional(uiTronconChoice.valueProperty());
            uiCenter.setCenter(table);
        }else{
            final TabPane pane = new TabPane();
            pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            for(int i=0;i<groups.length;i++){
                final DefaultTronconPojoTable table = new DefaultTronconPojoTable(groups[i]);
                table.tronconPropoerty().bindBidirectional(uiTronconChoice.valueProperty());
                final Tab tab = new Tab(groups[i].getName());
                tab.setContent(table);
                pane.getTabs().add(tab);
            }
            uiCenter.setCenter(pane);
        }
        
        //chargement de la liste des troncons disponibles
        final Session session = Injector.getBean(Session.class);
        final List<TronconDigue> troncons = session.getTronconDigueRepository().getAll();
        uiTronconChoice.setItems(FXCollections.observableList(troncons));        
        uiTronconChoice.setConverter(new StringConverter<TronconDigue>() {
            @Override
            public String toString(TronconDigue object) {
                if(object==null) return "";
                else return object.getLibelle();
            }
            @Override
            public TronconDigue fromString(String string) {
                if(string==null) return null;
                final ObservableList<TronconDigue> items = uiTronconChoice.getItems();
                for(TronconDigue troncon : items){
                    if(troncon!=null && troncon.getLibelle().toLowerCase().startsWith(string.toLowerCase())){
                        return troncon;
                    }
                }
                return null;
            }
        });
        
        new ComboBoxCompletion(uiTronconChoice);
        
        if(!troncons.isEmpty()){
            uiTronconChoice.getSelectionModel().select(troncons.get(0));
        }
    }
    
    
    
}
