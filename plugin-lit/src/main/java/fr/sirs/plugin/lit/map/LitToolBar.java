
package fr.sirs.plugin.lit.map;

import fr.sirs.map.FXOpenElementEditorAction;
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
        importBornes.getStyleClass().add(CENTER);
        final ToggleButton butInfo = new FXOpenElementEditorAction(map, "informations sur l'élément", "Ouvre la fiche du tronçon de lit.").createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butInfo.getStyleClass().add(RIGHT);
        
        getItems().add(new HBox(butEditLit, butCut, butMerge, butEditSr, butCalc, butEditConvert, importBornes, butInfo));

    }
    
}
