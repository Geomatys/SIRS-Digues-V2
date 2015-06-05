package fr.sirs.theme.ui;

import fr.sirs.theme.Theme;
import javafx.scene.Parent;
import javafx.scene.image.Image;

/**
 * Boutons qui seront affichés dans la toolbar des plugins, après avoir sélectionné un plugin dans la liste
 * déroulante.
 *
 * @author Cédric Briançon (Geomatys)
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

    /**
     * Génère un bouton à partir du nom, de la description et de l'image fournie.
     *
     * @param name Nom du bouton. Non {@code null}.
     * @param description Description du bouton, sera affiché dans une tooltip.
     * @param img Image du bouton.
     */
    protected AbstractPluginsButtonTheme(String name, String description, Image img) {
        super(name, Type.PLUGINS);
        this.description = description;
        this.img = img;
    }

    /**
     * Description du bouton.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Image du bouton.
     */
    public Image getImg() {
        return img;
    }

    /**
     * {@inheritDoc}
     */
    public abstract Parent createPane();
}
