package fr.sirs.plugins;

import fr.sirs.Plugin;
import fr.sirs.core.model.sql.SQLHelper;
import javafx.scene.image.Image;

/**
 * Minimal example of a plugin.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginExample extends Plugin {
    private static final String NAME = "plugin-example";
    private static final String TITLE = "Module exemple";

    public PluginExample() {
        name = NAME;
        loadingMessage.set("Chargement du module d'exemple");
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
