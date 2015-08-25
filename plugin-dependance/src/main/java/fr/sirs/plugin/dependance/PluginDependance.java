package fr.sirs.plugin.dependance;

import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.StructBeanSupplier;
import fr.sirs.core.component.AireStockageDependanceRepository;
import fr.sirs.core.component.AutreDependanceRepository;
import fr.sirs.core.component.CheminAccesDependanceRepository;
import fr.sirs.core.component.DesordreDependanceRepository;
import fr.sirs.core.component.OuvrageVoirieDependanceRepository;
import fr.sirs.core.model.AireStockageDependance;
import fr.sirs.core.model.AutreDependance;
import fr.sirs.core.model.CheminAccesDependance;
import fr.sirs.core.model.DesordreDependance;
import fr.sirs.core.model.OuvrageVoirieDependance;
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
    public static final String AUTRE_LAYER_NAME = "Autres";
    public static final String CHEMIN_ACCES_LAYER_NAME = "Chemins d'accès";
    public static final String OUVRAGE_VOIRIE_LAYER_NAME = "Ouvrages de voirie";
    public static final String DESORDRE_LAYER_NAME = "Désordres";
    private static final String NAME = "plugin-dependance";
    private static final String TITLE = "Module dépendance";

    private static FeatureMapLayer aireLayer;
    private static FeatureMapLayer autreLayer;
    private static FeatureMapLayer cheminLayer;
    private static FeatureMapLayer ouvrageLayer;
    private static FeatureMapLayer desordreLayer;

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
        final AutreDependanceRepository autreRepo = Injector.getBean(AutreDependanceRepository.class);
        final CheminAccesDependanceRepository cheminRepo = Injector.getBean(CheminAccesDependanceRepository.class);
        final OuvrageVoirieDependanceRepository ouvrageRepo = Injector.getBean(OuvrageVoirieDependanceRepository.class);

        try {
            final StructBeanSupplier ouvrageSupplier = new StructBeanSupplier(OuvrageVoirieDependance.class, ouvrageRepo::getAll);
            final BeanStore ouvrageStore = new BeanStore(ouvrageSupplier);
            ouvrageLayer = MapBuilder.createFeatureLayer(ouvrageStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(ouvrageStore.getNames().iterator().next())));
            ouvrageLayer.setName(OUVRAGE_VOIRIE_LAYER_NAME);
            ouvrageLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            depGroup.items().add(0, ouvrageLayer);
        } catch(Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        try {
            final StructBeanSupplier cheminSupplier = new StructBeanSupplier(CheminAccesDependance.class, cheminRepo::getAll);
            final BeanStore cheminStore = new BeanStore(cheminSupplier);
            cheminLayer = MapBuilder.createFeatureLayer(cheminStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(cheminStore.getNames().iterator().next())));
            cheminLayer.setName(CHEMIN_ACCES_LAYER_NAME);
            cheminLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            depGroup.items().add(1, cheminLayer);
        } catch(Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        try {
            final StructBeanSupplier autreSupplier = new StructBeanSupplier(AutreDependance.class, autreRepo::getAll);
            final BeanStore autreStore = new BeanStore(autreSupplier);
            autreLayer = MapBuilder.createFeatureLayer(autreStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(autreStore.getNames().iterator().next())));
            autreLayer.setName(AUTRE_LAYER_NAME);
            autreLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            depGroup.items().add(2, autreLayer);
        } catch(Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        try {
            final StructBeanSupplier aireSupplier = new StructBeanSupplier(AireStockageDependance.class, aireRepo::getAll);
            final BeanStore aireStore = new BeanStore(aireSupplier);
            aireLayer = MapBuilder.createFeatureLayer(aireStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(aireStore.getNames().iterator().next())));
            aireLayer.setName(AIRE_STOCKAGE_LAYER_NAME);
            aireLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            depGroup.items().add(3, aireLayer);
        } catch(Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        items.add(depGroup);

        final DesordreDependanceRepository desordreRepo = Injector.getBean(DesordreDependanceRepository.class);
        final MapItem desordreGroup = MapBuilder.createItem();
        desordreGroup.setName("Désordres");
        desordreGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
        try {
            final StructBeanSupplier desSupplier = new StructBeanSupplier(DesordreDependance.class, desordreRepo::getAll);
            final BeanStore desStore = new BeanStore(desSupplier);
            desordreLayer = MapBuilder.createFeatureLayer(desStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(desStore.getNames().iterator().next())));
            desordreLayer.setName(DESORDRE_LAYER_NAME);
            desordreLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            desordreGroup.items().add(desordreLayer);
        } catch(Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        items.add(desordreGroup);

        return items;
    }

    public static FeatureMapLayer getAireLayer() {
        return aireLayer;
    }

    public static FeatureMapLayer getAutreLayer() {
        return autreLayer;
    }

    public static FeatureMapLayer getCheminLayer() {
        return cheminLayer;
    }

    public static FeatureMapLayer getOuvrageLayer() {
        return ouvrageLayer;
    }

    public static FeatureMapLayer getDesordreLayer() {
        return desordreLayer;
    }

    @Override
    public SQLHelper getSQLHelper() {
        return DependanceSqlHelper.getInstance();
    }
}
