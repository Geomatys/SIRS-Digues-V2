package fr.sirs.plugin.dependance.map;

import fr.sirs.SIRS;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.controlsfx.control.action.ActionUtils;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.internal.GeotkFX;

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

        final ToggleButton buttonTransform = new ToggleButton(null, new ImageView(GeotkFX.ICON_DUPLICATE));
        buttonTransform.setTooltip(new Tooltip("Transformer une géométrie en dépendance"));
        buttonTransform.getStyleClass().add(CENTER);

        final ToggleButton buttonCreateDesorder = new ToggleButton(null, new ImageView(SIRS.ICON_WARNING));
        buttonCreateDesorder.setTooltip(new Tooltip("Créer un désordre"));
        buttonCreateDesorder.getStyleClass().add(RIGHT);

        getItems().add(new HBox(buttonEdit, buttonTransform, buttonCreateDesorder));
    }
}
