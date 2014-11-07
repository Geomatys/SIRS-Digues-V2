package fr.sirs.map;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import org.controlsfx.control.action.ActionUtils;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTronconEditBar extends ToolBar {

    private static final String LEFT = "buttongroup-left";
    private static final String CENTER = "buttongroup-center";
    private static final String RIGHT = "buttongroup-right";

    public FXTronconEditBar(FXMap map) {
        getStylesheets().add("/org/geotoolkit/gui/javafx/buttonbar.css");

        final ToggleButton butEdit = new TronconEditAction(map).createToggleButton(ActionUtils.ActionTextBehavior.SHOW);
        final HBox hboxAction = new HBox(butEdit);

        getItems().add(hboxAction);

    }

}
