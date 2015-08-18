package fr.sirs.plugin.dependance;

import fr.sirs.Plugin;
import fr.sirs.core.model.sql.DependanceSqlHelper;
import fr.sirs.core.model.sql.SQLHelper;
import fr.sirs.plugin.dependance.map.DependanceToolBar;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;

import java.util.Collections;
import java.util.List;

/**
 * Plugin correspondant au module dépendance.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginDependance extends Plugin {
    private static final String NAME = "plugin-dependance";
    private static final String TITLE = "Module dépendance";

    private final DependanceToolBar toolbar = new DependanceToolBar();

    public PluginDependance() {
        name = NAME;
        loadingMessage.set("module dépendance");
        themes.add(new DependancesTheme());
        themes.add(new DesordresDependanceTheme());
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
    public List<ToolBar> getMapToolBars() {
        return Collections.singletonList(toolbar);
    }

    @Override
    public SQLHelper getSQLHelper() {
        return DependanceSqlHelper.getInstance();
    }
}
