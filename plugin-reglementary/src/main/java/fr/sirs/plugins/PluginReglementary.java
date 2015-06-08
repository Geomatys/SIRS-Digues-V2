package fr.sirs.plugins;

import fr.sirs.Plugin;
import javafx.scene.image.Image;

/**
 * Plugin correspondant au module réglementaire, permettant de gérer des documents de suivis.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class PluginReglementary extends Plugin {
    private static final String NAME = "plugin-reglementary";
    private static final String TITLE = "Module réglementaire";

    public PluginReglementary() {
        name = NAME;
        loadingMessage.set("Chargement du module réglementaire");
        themes.add(new DocumentsTheme());
        themes.add(new StatesGeneratorTheme());
    }

    @Override
    public void load() throws Exception {
        getConfiguration();
    }

    @Override
    public CharSequence getTitle() {
        return TITLE;
    }

    @Override
    public Image getImage() {
        // TODO: choisir une image pour ce plugin
        return null;
    }
}
