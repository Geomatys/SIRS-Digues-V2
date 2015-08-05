package fr.sirs.plugin.vegetation;

import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.theme.AbstractTheme;
import fr.sirs.theme.AbstractTheme.ThemeManager;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import fr.sirs.theme.ui.FXTronconThemePane;
import javafx.scene.Parent;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class ParcelleTheme extends AbstractPluginsButtonTheme {
    public ParcelleTheme() {
        super("Parcelles", "Gestion des parcelles", null);
    }

    @Override
    public Parent createPane() {
        final ThemeManager themeManager = AbstractTheme.generateThemeManager(ParcelleVegetation.class);
        final FXTronconThemePane pane = new FXTronconThemePane(themeManager);
        return pane;
    }


}