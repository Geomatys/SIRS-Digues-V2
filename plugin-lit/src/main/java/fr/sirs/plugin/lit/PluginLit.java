package fr.sirs.plugin.lit;

import fr.sirs.Plugin;
import fr.sirs.core.model.sql.SQLHelper;
import javafx.scene.image.Image;

/**
 * Minimal example of a plugin.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginLit extends Plugin {
    private static final String NAME = "plugin-lit";
    private static final String TITLE = "Module lit";

    public PluginLit() {
        name = NAME;
        loadingMessage.set("module lit");
        themes.add(new ButtonExampleTheme());
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

    @Override
    public SQLHelper getSQLHelper() {
        // TODO: renvoyer le SQLHelper du plugin pour l'export RDBMS !
        return null;
    }
}
