package fr.sirs.plugin.aot.cot;

import fr.sirs.Plugin;
import javafx.scene.image.Image;

/**
 * Plugin correspondant au module AOT COT.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginAotCot extends Plugin {
    private static final String NAME = "plugin-aot-cot";
    private static final String TITLE = "Module AOT COT";

    public PluginAotCot() {
        name = NAME;
        loadingMessage.set("module AOT COT");
        themes.add(new CreateAotCotTheme());
        themes.add(new SuiviAotCotTheme());
        themes.add(new AssociatedDocumentsAotCotTheme());
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
