package fr.sirs.plugin.carto;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;

/**
 * Ouverture du panneau des favoris
 *
 * @author Johann Sorel (Geomatys)
 */
public final class BookMarkTheme extends AbstractPluginsButtonTheme {
    public BookMarkTheme() {
        super("Favoris", "Favoris", null);
    }

    @Override
    public Parent createPane() {
        return new FXBookMarks();
    }
    
}