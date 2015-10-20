package fr.sirs.theme.ui;

import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.PositionableVegetation;
import java.util.Arrays;

/**
 * Form editor allowing to update linear and geographic/projected position of a
 * {@link Positionable} element.
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPositionableVegetationPane extends FXPositionablePane {

    public FXPositionableVegetationPane() {
        super(Arrays.asList(
                new FXPositionableCoordAreaMode(),
                new FXPositionableLinearAreaMode(),
                new FXPositionableExplicitMode()
                ), PositionableVegetation.class);
    }

}
