package fr.sirs.plugin.vegetation;

import fr.sirs.Plugin;
import fr.sirs.core.model.sql.SQLHelper;
import fr.sirs.core.model.sql.VegetationSqlHelper;
import java.util.Collections;
import java.util.List;

import fr.sirs.map.FXMapPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;

/**
 * Minimal example of a plugin.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginVegetation extends Plugin {
    private static final String NAME = "plugin-vegetation";
    private static final String TITLE = "Module végétation";

    private final VegetationToolBar toolbar = new VegetationToolBar();

    public PluginVegetation() {
        name = NAME;
        loadingMessage.set("module végétation");
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
        return null;
    }

    @Override
    public List<ToolBar> getMapToolBars(final FXMapPane mapPane) {
        return Collections.singletonList(toolbar);
    }

    @Override
    public SQLHelper getSQLHelper() {
        return VegetationSqlHelper.getInstance();
    }
}
