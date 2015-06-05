package fr.sirs.theme.ui;

import fr.sirs.theme.Theme;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 * Boutons qui seront affichés dans la toolbar des plugins, après avoir sélectionné un plugin dans la liste
 * déroulante.
 */
public abstract class AbstractPluginsButtonTheme extends Theme {
    /**
     * Description du bouton affichée au survol.
     */
    protected final String description;

    /**
     * Image à utiliser pour ce bouton.
     */
    protected final Image img;

    public AbstractPluginsButtonTheme(String name, Type type, String description, Image img) {
        super(name, type);
        this.description = description;
        this.img = img;
    }

    public String getDescription() {
        return description;
    }

    public Image getImg() {
        return img;
    }

    public abstract Parent createPane();
}
