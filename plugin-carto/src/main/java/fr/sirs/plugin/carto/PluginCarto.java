package fr.sirs.plugin.carto;

import fr.sirs.Plugin;
import fr.sirs.core.model.sql.SQLHelper;
import javafx.scene.image.Image;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class PluginCarto extends Plugin {
    private static final String NAME = "plugin-carto";
    private static final String TITLE = "Module cartographie";

    public PluginCarto() {
        name = NAME;
        loadingMessage.set("Chargement du module de cartographie");
        themes.add(new AddLayerTheme());
        themes.add(new BookMarkTheme());
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
