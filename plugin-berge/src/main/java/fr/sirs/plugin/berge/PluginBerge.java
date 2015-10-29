package fr.sirs.plugin.berge;

import fr.sirs.CorePlugin;
import static fr.sirs.CorePlugin.buildLayers;
import static fr.sirs.CorePlugin.createDefaultSelectionStyle;
import static fr.sirs.CorePlugin.createTronconSelectionStyle;
import fr.sirs.Plugin;
import fr.sirs.Session;
import fr.sirs.StructBeanSupplier;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.model.Berge;
import fr.sirs.core.model.CreteBerge;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.EpiBerge;
import fr.sirs.core.model.FondationBerge;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.OuvrageRevancheBerge;
import fr.sirs.core.model.PiedBerge;
import fr.sirs.core.model.SommetBerge;
import fr.sirs.core.model.TalusBerge;
import fr.sirs.core.model.TalusRisbermeBerge;
import fr.sirs.core.model.TraitBerge;
import fr.sirs.core.model.sql.BergeSqlHelper;
import fr.sirs.core.model.sql.SQLHelper;
import fr.sirs.map.FXMapPane;
import fr.sirs.plugin.berge.map.BergeToolBar;
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
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javax.measure.unit.NonSI;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.data.bean.BeanFeatureSupplier;
import org.geotoolkit.data.bean.BeanStore;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
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
public class PluginBerge extends Plugin {
    private static final String NAME = "plugin-berge";
    private static final String TITLE = "Module berge";

    private static final FilterFactory2 FF = GO2Utilities.FILTER_FACTORY;
    private static final MutableStyleFactory SF = GO2Utilities.STYLE_FACTORY;

    public PluginBerge() {
        name = NAME;
        loadingMessage.set("module berge");
        themes.add(new SuiviBergeTheme());
        themes.add(new StructuresDescriptionTheme());
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
        handleTronconType(Berge.class);
        return BergeSqlHelper.getInstance();
    }

    @Override
    public List<ToolBar> getMapToolBars(final FXMapPane mapPane) {
        return Collections.singletonList(new BergeToolBar(mapPane.getUiMap()));
    }

    //doit avoir la meme valeur que dans le fichier Berge.properties classPlural
    public static final String LAYER_BERGE_NAME = "Berges";
    public static final String LAYER_TRAIT_NAME = "Traits de berge";

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

        suppliers.put(Berge.class, getDefaultSupplierForClass.apply(Berge.class));
        suppliers.put(TraitBerge.class, getDefaultSupplierForClass.apply(TraitBerge.class));

        //
        suppliers.put(PiedBerge.class, getDefaultSupplierForClass.apply(PiedBerge.class));
        suppliers.put(SommetBerge.class, getDefaultSupplierForClass.apply(SommetBerge.class));
        suppliers.put(EpiBerge.class, getDefaultSupplierForClass.apply(EpiBerge.class));
        suppliers.put(FondationBerge.class, getDefaultSupplierForClass.apply(FondationBerge.class));
        suppliers.put(TalusRisbermeBerge.class, getDefaultSupplierForClass.apply(TalusRisbermeBerge.class));
        suppliers.put(TalusBerge.class, getDefaultSupplierForClass.apply(TalusBerge.class));
        suppliers.put(OuvrageRevancheBerge.class, getDefaultSupplierForClass.apply(OuvrageRevancheBerge.class));
        suppliers.put(CreteBerge.class, getDefaultSupplierForClass.apply(CreteBerge.class));
    }


    @Override
    public List<MapItem> getMapItems() {
        loadDataSuppliers();
        try {
            final MapItem container = MapBuilder.createItem();
            container.setName("Module berges");

            final BeanStore bergeStore = new BeanStore(suppliers.get(Berge.class));
            final BeanStore traitStore = new BeanStore(suppliers.get(TraitBerge.class));

            container.items().addAll(CorePlugin.buildLayers(bergeStore, LAYER_BERGE_NAME,
                    createBergeStyle(), createTronconSelectionStyle(false),true));
            container.items().addAll(CorePlugin.buildLayers(traitStore, LAYER_TRAIT_NAME,
                    createTraitBergeStyle(), createTronconSelectionStyle(false),true));

            final Map<String, String> nameMap = new HashMap<>();
            for(Class elementClass : suppliers.keySet()) {
                final LabelMapper mapper = LabelMapper.get(elementClass);
                nameMap.put(elementClass.getSimpleName(), mapper.mapClassName());
            }

            final MapItem structLayer = MapBuilder.createItem();
            structLayer.setName("Autre (berge)");
            final BeanStore otherStore = new BeanStore(suppliers.get(PiedBerge.class),
                    suppliers.get(SommetBerge.class),
                    suppliers.get(EpiBerge.class),
                    suppliers.get(FondationBerge.class),
                    suppliers.get(TalusRisbermeBerge.class),
                    suppliers.get(TalusBerge.class),
                    suppliers.get(OuvrageRevancheBerge.class),
                    suppliers.get(CreteBerge.class));
            structLayer.items().addAll(buildLayers(otherStore, nameMap, colors, createDefaultSelectionStyle(), false));
            structLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            container.items().add(structLayer);

            return Collections.singletonList(container);
        } catch (Exception e) {
            throw new SirsCoreRuntimeException(e);
        }
    }

    public static MutableStyle createBergeStyle() throws CQLException, URISyntaxException{
        final Stroke stroke1 = SF.stroke(SF.literal(Color.BLACK),LITERAL_ONE_FLOAT,FF.literal(9),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line1 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke1,LITERAL_ONE_FLOAT);

        final Stroke stroke2 = SF.stroke(SF.literal(new Color(230, 179, 77)),LITERAL_ONE_FLOAT,FF.literal(7),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line2 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke2,LITERAL_ONE_FLOAT);

        final Stroke stroke3 = SF.stroke(SF.literal(Color.BLACK),LITERAL_ONE_FLOAT,FF.literal(1),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line3 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke3,LITERAL_ONE_FLOAT);

        return SF.style(line1,line2,line3);
    }

    public static MutableStyle createTraitBergeStyle() throws CQLException, URISyntaxException{
        final Stroke stroke1 = SF.stroke(SF.literal(new Color(255,204,128)),LITERAL_ONE_FLOAT,FF.literal(1.5),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line1 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke1,LITERAL_ONE_FLOAT);

        return SF.style(line1);
    }

    @Override
    public Optional<Image> getModelImage() throws IOException {
        final Image image;

        try (final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("fr/sirs/bergeModel.png")) {
            image = new Image(in);
        }
        return Optional.of(image);
    }

}
