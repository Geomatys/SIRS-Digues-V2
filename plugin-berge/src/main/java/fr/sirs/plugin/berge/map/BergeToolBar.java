
package fr.sirs.plugin.berge.map;

import fr.sirs.map.BorneEditAction;
import fr.sirs.map.ConvertGeomToTronconAction;
import fr.sirs.map.FXImportBornesAction;
import fr.sirs.map.FXOpenElementEditorAction;
import fr.sirs.map.PointCalculatorAction;
import fr.sirs.map.TronconCutAction;
import fr.sirs.map.TronconEditAction;
import fr.sirs.map.TronconMergeAction;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import org.controlsfx.control.action.ActionUtils;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author guilhem
 */
public class BergeToolBar extends ToolBar {

    private static final String LEFT = "buttongroup-left";
    private static final String CENTER = "buttongroup-center";
    private static final String RIGHT = "buttongroup-right";

    public BergeToolBar(FXMap map) {
        getStylesheets().add("/org/geotoolkit/gui/javafx/buttonbar.css");

        getItems().add(new Label("Berges"));
        
        final ToggleButton butEditBerge = new BergeEditAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butEditBerge.getStyleClass().add(LEFT);
        final ToggleButton butCut = new TronconCutAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butCut.getStyleClass().add(CENTER);
        final ToggleButton butMerge = new TronconMergeAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butMerge.getStyleClass().add(CENTER);
        final ToggleButton butEditSr = new BorneEditAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butEditSr.getStyleClass().add(CENTER);
        final ToggleButton butCalc = new PointCalculatorAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butCalc.getStyleClass().add(CENTER);
        final ToggleButton butEditConvert = new ConvertGeomToTronconAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butEditConvert.getStyleClass().add(CENTER);
        final Button importBornes = new FXImportBornesAction(map).createButton(ActionUtils.ActionTextBehavior.HIDE);
        importBornes.getStyleClass().add(CENTER);
        final ToggleButton butInfo = new FXOpenElementEditorAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butInfo.getStyleClass().add(RIGHT);
        
        getItems().add(new HBox(butEditBerge, butCut, butMerge, butEditSr, butCalc, butEditConvert, importBornes, butInfo));

    }
    
}
