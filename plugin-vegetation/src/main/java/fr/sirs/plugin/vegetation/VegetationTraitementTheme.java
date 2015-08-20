package fr.sirs.plugin.vegetation;

import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.theme.AbstractTheme;
import fr.sirs.theme.AbstractTheme.ThemeManager;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import fr.sirs.theme.ui.FXTronconThemePane;
import fr.sirs.theme.ui.FXVegetationTronconThemePane;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class VegetationTraitementTheme extends AbstractPluginsButtonTheme {

    public VegetationTraitementTheme() {
        super("Végétation et traitements", "Végétation et traitements", new Image("fr/sirs/plugin/vegetation/vegetation-description.png"));
    }

    @Override
    public Parent createPane() {
        final ThemeManager themeManager = AbstractTheme.generateThemeManager(ParcelleVegetation.class);
        final FXTronconThemePane pane = new FXVegetationTronconThemePane(themeManager);
        return pane;
    }


}