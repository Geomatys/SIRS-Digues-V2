package fr.sirs.plugin.dependance.map;

import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import org.controlsfx.control.action.ActionUtils;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class DependanceToolBar extends ToolBar {
    private static final String LEFT = "buttongroup-left";
    private static final String CENTER = "buttongroup-center";
    private static final String RIGHT = "buttongroup-right";

    public DependanceToolBar(final FXMap map) {
        getStylesheets().add("/org/geotoolkit/gui/javafx/buttonbar.css");

        getItems().add(new Label("Dépendance"));

        final ToggleButton buttonEdit = new DependanceEditAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        buttonEdit.getStyleClass().add(LEFT);

        final ToggleButton buttonTransform = new DependanceTransformAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        buttonTransform.getStyleClass().add(CENTER);

        final ToggleButton buttonCreateDesorder = new DesordreCreateAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        buttonCreateDesorder.getStyleClass().add(RIGHT);

        getItems().add(new HBox(buttonEdit, buttonTransform, buttonCreateDesorder));
    }
}
