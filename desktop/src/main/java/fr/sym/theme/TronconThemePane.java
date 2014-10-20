
package fr.sym.theme;

import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.TronconDigue;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Johann Sorel
 */
public class TronconThemePane extends BorderPane {

    @FXML
    private BorderPane uiCenter;
    @FXML
    private ChoiceBox<TronconDigue> uiTronconChoice;
    
    private AbstractTronconTheme.ThemeGroup[] groups;
    
    public TronconThemePane(AbstractTronconTheme.ThemeGroup ... groups) {
        Symadrem.loadJRXML(this);
        
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
        
    }
    
    /**
     * Called by FXMLLoader after creating controller.
     */
    public void initialize(){
        
    }
    
    
}
