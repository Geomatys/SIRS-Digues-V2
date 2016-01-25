package fr.sirs.plugin.lit;

import fr.sirs.CorePlugin;
import static fr.sirs.CorePlugin.buildLayers;
import static fr.sirs.CorePlugin.createDefaultSelectionStyle;
import static fr.sirs.CorePlugin.createTronconSelectionStyle;
import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.StructBeanSupplier;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.model.AutreOuvrageLit;
import fr.sirs.core.model.DesordreLit;
import fr.sirs.core.model.DomanialiteLit;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.IleBancLit;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.LargeurLit;
import fr.sirs.core.model.Lit;
import fr.sirs.core.model.OccupationRiveraineLit;
import fr.sirs.core.model.OuvrageAssocieLit;
import fr.sirs.core.model.PenteLit;
import fr.sirs.core.model.PlageDepotLit;
import fr.sirs.core.model.RegimeEcoulementLit;
import fr.sirs.core.model.SeuilLit;
import fr.sirs.core.model.TronconLit;
import fr.sirs.core.model.ZoneAtterrissementLit;
import fr.sirs.map.FXMapPane;
import fr.sirs.plugin.lit.map.LitToolBar;
import fr.sirs.util.FXFreeTab;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javax.measure.unit.NonSI;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.data.bean.BeanFeatureSupplier;
import org.geotoolkit.data.bean.BeanStore;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import static org.geotoolkit.style.StyleConstants.DEFAULT_DESCRIPTION;
import static org.geotoolkit.style.StyleConstants.LITERAL_ONE_FLOAT;
import static org.geotoolkit.style.StyleConstants.LITERAL_ZERO_FLOAT;
import static org.geotoolkit.style.StyleConstants.STROKE_CAP_SQUARE;
import static org.geotoolkit.style.StyleConstants.STROKE_JOIN_BEVEL;
import org.opengis.filter.FilterFactory2;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.Stroke;

/**
 * Minimal example of a plugin.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginLit extends Plugin {
    private static final String NAME = "plugin-lit";
    private static final String TITLE = "Module lit";

    private static final FilterFactory2 FF = GO2Utilities.FILTER_FACTORY;
    private static final MutableStyleFactory SF = GO2Utilities.STYLE_FACTORY;
    private final SuiviLitTheme suiviTheme;

    public PluginLit() {
        name = NAME;
        loadingMessage.set("module lit");
        suiviTheme = new SuiviLitTheme();
        themes.add(suiviTheme);
        themes.add(new StructureDescriptionTheme());
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
    public List<ToolBar> getMapToolBars(final FXMapPane mapPane) {
        return Collections.singletonList(new LitToolBar(mapPane.getUiMap()));
    }

    @Override
    public boolean handleTronconType(final Class<? extends Element> element){
        return TronconLit.class.equals(element)
                || Lit.class.equals(element);
    }

    @Override
    public FXFreeTab openTronconPane(final Element element){
        final FXFreeTab tab = Injector.getSession().getOrCreateThemeTab(suiviTheme);
        suiviTheme.display(element);
        return tab;
    }

    public static String LAYER_NAME = LabelMapper.get(TronconLit.class).mapClassNamePlural();

    final Color[] colors = new Color[]{
                Color.BLACK,
                Color.BLUE,
                Color.CYAN,
                Color.RED,
                Color.DARK_GRAY,
                Color.GREEN,
                Color.MAGENTA,
                Color.ORANGE,
                Color.PINK,
                Color.RED
            };

    private final HashMap<Class, BeanFeatureSupplier> suppliers = new HashMap<>();

    private synchronized void loadDataSuppliers() {
        suppliers.clear();

        final Function<Class<? extends Element>, StructBeanSupplier> getDefaultSupplierForClass = (Class<? extends Element> c) ->{
            return new StructBeanSupplier(c, () -> getSession().getRepositoryForClass(c).getAllStreaming());
        };

        suppliers.put(TronconLit.class, getDefaultSupplierForClass.apply(TronconLit.class));

        // Ouvrages dans le lit
        suppliers.put(SeuilLit.class, getDefaultSupplierForClass.apply(SeuilLit.class));
        suppliers.put(PlageDepotLit.class, getDefaultSupplierForClass.apply(PlageDepotLit.class));
        suppliers.put(AutreOuvrageLit.class, getDefaultSupplierForClass.apply(AutreOuvrageLit.class));

        suppliers.put(IleBancLit.class, getDefaultSupplierForClass.apply(IleBancLit.class));

        suppliers.put(DesordreLit.class, getDefaultSupplierForClass.apply(DesordreLit.class));

        suppliers.put(OuvrageAssocieLit.class, getDefaultSupplierForClass.apply(OuvrageAssocieLit.class));
        suppliers.put(OccupationRiveraineLit.class, getDefaultSupplierForClass.apply(OccupationRiveraineLit.class));
        suppliers.put(PenteLit.class, getDefaultSupplierForClass.apply(PenteLit.class));
        suppliers.put(LargeurLit.class, getDefaultSupplierForClass.apply(LargeurLit.class));
        suppliers.put(RegimeEcoulementLit.class, getDefaultSupplierForClass.apply(RegimeEcoulementLit.class));
        suppliers.put(DomanialiteLit.class, getDefaultSupplierForClass.apply(DomanialiteLit.class));
        suppliers.put(ZoneAtterrissementLit.class, getDefaultSupplierForClass.apply(ZoneAtterrissementLit.class));
    }


    @Override
    public List<MapItem> getMapItems() {
        loadDataSuppliers();
        try {
            final Function<Class<? extends Element>, StructBeanSupplier> getDefaultSupplierForClass = (Class<? extends Element> c) ->{
                return new StructBeanSupplier(c, () -> getSession().getRepositoryForClass(c).getAll());
            };
            //troncons
            final BeanStore tronconStore = new BeanStore(getDefaultSupplierForClass.apply(TronconLit.class));
            List<MapLayer> layers = CorePlugin.buildLayers(tronconStore, LAYER_NAME, createLitStyle(), createTronconSelectionStyle(false),true);

            MapItem container = MapBuilder.createItem();
            container.setName("Module lits");
            container.items().addAll(layers);


            final Map<String, String> nameMap = new HashMap<>();
            for(Class elementClass : suppliers.keySet()) {
                final LabelMapper mapper = LabelMapper.get(elementClass);
                nameMap.put(elementClass.getSimpleName(), mapper.mapClassName());
            }

            try {
                final MapItem structLayer = MapBuilder.createItem();
                structLayer.setName("Ouvrages dans le lit");
                final BeanStore ouvrageStore = new BeanStore(suppliers.get(SeuilLit.class),
                        suppliers.get(PlageDepotLit.class),
                        suppliers.get(AutreOuvrageLit.class));
                structLayer.items().addAll(buildLayers(ouvrageStore, nameMap, colors, createDefaultSelectionStyle(), false));
                structLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
                container.items().add(structLayer);

                final MapItem ilesLayer = MapBuilder.createItem();
                ilesLayer.setName("Îles et bancs");
                final BeanStore ilesStore = new BeanStore(suppliers.get(IleBancLit.class));
                ilesLayer.items().addAll(buildLayers(ilesStore, nameMap, colors, createDefaultSelectionStyle(), false));
                ilesLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
                container.items().add(ilesLayer);


                final MapItem desordreLayer = MapBuilder.createItem();
                desordreLayer.setName("Désordres (lit)");
                final BeanStore desordreStore = new BeanStore(suppliers.get(DesordreLit.class));
                desordreLayer.items().addAll(buildLayers(desordreStore, nameMap, colors, createDefaultSelectionStyle(), false));
                desordreLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
                container.items().add(desordreLayer);


                final MapItem otherLayer = MapBuilder.createItem();
                otherLayer.setName("Autre (lit)");
                final BeanStore otherStore = new BeanStore(suppliers.get(OuvrageAssocieLit.class),
                        suppliers.get(OccupationRiveraineLit.class),
                        suppliers.get(PenteLit.class),
                        suppliers.get(LargeurLit.class),
                        suppliers.get(RegimeEcoulementLit.class),
                        suppliers.get(DomanialiteLit.class),
                        suppliers.get(ZoneAtterrissementLit.class));
                otherLayer.items().addAll(buildLayers(otherStore, nameMap, colors, createDefaultSelectionStyle(), false));
                otherLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
                container.items().add(otherLayer);


            } catch (Exception ex) {
                SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }


            return Collections.singletonList(container);
        } catch (Exception e) {
            throw new SirsCoreRuntimeException(e);
        }
    }

    public static MutableStyle createLitStyle() throws CQLException, URISyntaxException{
        final Stroke stroke1 = SF.stroke(SF.literal(new Color(0, 100, 80)),LITERAL_ONE_FLOAT,FF.literal(9),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line1 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke1,LITERAL_ONE_FLOAT);

        final Stroke stroke2 = SF.stroke(SF.literal(new Color(204, 230, 255)),LITERAL_ONE_FLOAT,FF.literal(7),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line2 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke2,LITERAL_ONE_FLOAT);

        final Stroke stroke3 = SF.stroke(SF.literal(new Color(0, 100, 80)),LITERAL_ONE_FLOAT,FF.literal(1),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line3 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke3,LITERAL_ONE_FLOAT);

        return SF.style(line1,line2,line3);
    }

    @Override
    public Optional<Image> getModelImage() throws IOException {
        final Image image;

        try (final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("fr/sirs/litModel.png")) {
            image = new Image(in);
        }
        return Optional.of(image);
    }
}
