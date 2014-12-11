package fr.sirs.map;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
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

        final Label text = new Label("Outils de création/édition");
        text.setAlignment(Pos.CENTER);
        
        final ToggleButton butEditTroncon = new TronconEditAction(map).createToggleButton(ActionUtils.ActionTextBehavior.SHOW);
        butEditTroncon.getStyleClass().add(LEFT);
        final ToggleButton butEditSr = new BorneEditAction(map).createToggleButton(ActionUtils.ActionTextBehavior.SHOW);
        butEditSr.getStyleClass().add(CENTER);
        final ToggleButton butEditConvert = new ConvertGeomToTronconAction(map).createToggleButton(ActionUtils.ActionTextBehavior.SHOW);
        butEditConvert.getStyleClass().add(RIGHT);
        final HBox hboxAction = new HBox(butEditTroncon, butEditSr, butEditConvert);

        getItems().add(hboxAction);

    }

}
