package fr.sirs.plugin.dependance.map;

import fr.sirs.SIRS;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.geotoolkit.internal.GeotkFX;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class DependanceToolBar extends ToolBar {

    public DependanceToolBar() {
        getItems().add(new Label("Dépendance"));

        final ToggleButton buttonEdit = new ToggleButton(null, new ImageView(GeotkFX.ICON_EDIT));
        getItems().add(buttonEdit);

        final ToggleButton buttonTransform = new ToggleButton(null, new ImageView(GeotkFX.ICON_DUPLICATE));
        buttonTransform.setTooltip(new Tooltip("Transformer une géométrie en dépendance"));
        getItems().add(buttonTransform);

        final ToggleButton buttonCreateDesorder = new ToggleButton(null, new ImageView(SIRS.ICON_WARNING));
        buttonCreateDesorder.setTooltip(new Tooltip("Créer un désordre"));
        getItems().add(buttonCreateDesorder);
    }
}
