package fr.sirs.plugin.berge;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Panneau regroupant les fonctionnalités de description des ouvrages.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class StructuresDescriptionTheme extends AbstractPluginsButtonTheme {
    public StructuresDescriptionTheme() {
        super("Description des ouvrages", "Description des ouvrages", null);
        getSubThemes().add(new StructureElementTheme());
        getSubThemes().add(new ReseauxVoirieTheme());
        getSubThemes().add(new ReseauxOuvrageTheme());
        getSubThemes().add(new DesordresTheme());
        getSubThemes().add(new PrestationsTheme());
        getSubThemes().add(new MesureEventHydroTheme());
        getSubThemes().add(new DocumentTheme());

    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();

        return borderPane;
    }
}