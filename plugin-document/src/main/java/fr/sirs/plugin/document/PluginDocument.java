package fr.sirs.plugin.document;

import fr.sirs.Plugin;
import javafx.scene.image.Image;

/**
 * Minimal example of a plugin.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginDocument extends Plugin {
    private static final String NAME = "plugin-document";
    private static final String TITLE = "Module document d'ouvrages";

    public PluginDocument() {
        name = NAME;
        loadingMessage.set("module document d'ouvrages");
        themes.add(new DocumentManagementTheme());
        themes.add(new DynamicDocumentTheme());
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
