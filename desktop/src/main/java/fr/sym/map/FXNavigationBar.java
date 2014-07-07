

package fr.sym.map;

import fr.sym.map.navigation.FXPanAction;
import fr.sym.map.navigation.FXZoomAllAction;
import fr.sym.map.navigation.FXZoomInAction;
import fr.sym.map.navigation.FXZoomOutAction;
import javafx.scene.control.ToolBar;
import org.controlsfx.control.action.ActionUtils;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXNavigationBar extends ToolBar {

    public FXNavigationBar(FXMap map) {
        
        getItems().add(ActionUtils.createButton(new FXZoomAllAction(map)));
        getItems().add(ActionUtils.createButton(new FXZoomInAction(map)));
        getItems().add(ActionUtils.createButton(new FXZoomOutAction(map)));
        getItems().add(ActionUtils.createButton(new FXPanAction(map, false)));
    }
    

    

}
