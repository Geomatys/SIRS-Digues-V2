package fr.sirs.theme.ui;

import fr.sirs.core.model.Positionable;

/**
 * Edition des bornes d'un {@link Positionable}.
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPositionableLinearMode extends FXPositionableAbstractLinearMode {

    private static final String MODE = "LINEAR";

    public FXPositionableLinearMode() {
        super();
    }

    @Override
    public String getID() {
        return MODE;
    }
}
