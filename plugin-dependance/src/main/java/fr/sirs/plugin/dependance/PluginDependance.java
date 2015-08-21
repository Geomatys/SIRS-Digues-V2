package fr.sirs.plugin.dependance;

import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.StructBeanSupplier;
import fr.sirs.core.component.AireStockageDependanceRepository;
import fr.sirs.core.model.AireStockageDependance;
import fr.sirs.core.model.sql.DependanceSqlHelper;
import fr.sirs.core.model.sql.SQLHelper;
import fr.sirs.map.FXMapPane;
import fr.sirs.plugin.dependance.map.DependanceToolBar;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import org.geotoolkit.data.bean.BeanStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Plugin correspondant au module dépendance.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginDependance extends Plugin {
    public static final String AIRE_STOCKAGE_LAYER_NAME = "Aires de stockage";
    private static final String NAME = "plugin-dependance";
    private static final String TITLE = "Module dépendance";

    private static FeatureMapLayer aireLayer;

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
    public List<ToolBar> getMapToolBars(final FXMapPane mapPane) {
        return Collections.singletonList(new DependanceToolBar(mapPane.getUiMap()));
    }

    @Override
    public List<MapItem> getMapItems() {
        final List<MapItem> items = new ArrayList<>();
        final MapItem depGroup = MapBuilder.createItem();
        depGroup.setName("Dépendances");
        depGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);

        final AireStockageDependanceRepository aireRepo = Injector.getBean(AireStockageDependanceRepository.class);

        try {
            final StructBeanSupplier aireSupplier = new StructBeanSupplier(AireStockageDependance.class, aireRepo::getAll);
            final BeanStore aireStore = new BeanStore(aireSupplier);
            aireLayer = MapBuilder.createFeatureLayer(aireStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(aireStore.getNames().iterator().next())));
            aireLayer.setName(AIRE_STOCKAGE_LAYER_NAME);
            aireLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            depGroup.items().add(0, aireLayer);
        } catch(Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        items.add(depGroup);
        return items;
    }

    public static FeatureMapLayer getAireLayer() {
        return aireLayer;
    }

    @Override
    public SQLHelper getSQLHelper() {
        return DependanceSqlHelper.getInstance();
    }
}
