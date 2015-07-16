package fr.sirs.plugin.berge;

import fr.sirs.Plugin;
import javafx.scene.image.Image;

/**
 * Minimal example of a plugin.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginBerge extends Plugin {
    private static final String NAME = "plugin-berge";
    private static final String TITLE = "Module berge";

    public PluginBerge() {
        name = NAME;
        loadingMessage.set("module berge");
        themes.add(new SuiviBergeTheme());
        themes.add(new StructuresDescriptionTheme());
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
