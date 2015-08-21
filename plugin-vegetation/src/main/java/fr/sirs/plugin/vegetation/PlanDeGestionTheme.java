package fr.sirs.plugin.vegetation;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class PlanDeGestionTheme extends AbstractPluginsButtonTheme {

    public PlanDeGestionTheme() {
        super("Plan de gestion", "Plan de gestion", new Image("fr/sirs/plugin/vegetation/vegetation-plan gestion.png"));
    }

    @Override
    public Parent createPane() {
        return new FXPlanDeGestionPane();
    }
}