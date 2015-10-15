package fr.sirs.plugin.carto;

import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 * Ouverture du panneau des favoris
 *
 * @author Johann Sorel (Geomatys)
 */
public final class BookMarkTheme extends AbstractPluginsButtonTheme {
    private static final Image ICON = new Image("/fr/sirs/plugin/carto/bookmarks.png");

    public BookMarkTheme() {
        super("Favoris", "Favoris", ICON);
    }

    @Override
    public Parent createPane() {
        return new FXBookMarks();
    }
    
}