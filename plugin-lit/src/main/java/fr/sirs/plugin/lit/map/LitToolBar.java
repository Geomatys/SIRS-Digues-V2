
package fr.sirs.plugin.lit.map;

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
public class LitToolBar extends ToolBar {

    private static final String LEFT = "buttongroup-left";
    private static final String CENTER = "buttongroup-center";
    private static final String RIGHT = "buttongroup-right";

    public LitToolBar(FXMap map) {
        getStylesheets().add("/org/geotoolkit/gui/javafx/buttonbar.css");

        getItems().add(new Label("Lits"));
        
        final ToggleButton butEditLit = new LitEditAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butEditLit.getStyleClass().add(LEFT);
        final ToggleButton butCut = new LitCutAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butCut.getStyleClass().add(CENTER);
        final ToggleButton butMerge = new LitMergeAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butMerge.getStyleClass().add(CENTER);
        final ToggleButton butEditSr = new LitBorneEditAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butEditSr.getStyleClass().add(CENTER);
        final ToggleButton butCalc = new LitPointCalculatorAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butCalc.getStyleClass().add(CENTER);
        final ToggleButton butEditConvert = new ConvertGeomToLitAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butEditConvert.getStyleClass().add(CENTER);
        final Button importBornes = new FXImportBornesLitAction(map).createButton(ActionUtils.ActionTextBehavior.HIDE);
        importBornes.getStyleClass().add(RIGHT);
        
        getItems().add(new HBox(butEditLit, butCut, butMerge, butEditSr, butCalc, butEditConvert, importBornes));

    }
    
}
