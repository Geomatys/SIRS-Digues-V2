package fr.sirs.plugin.lit;

import fr.sirs.CorePlugin;
import static fr.sirs.CorePlugin.createTronconSelectionStyle;
import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.StructBeanSupplier;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.Lit;
import fr.sirs.core.model.TronconLit;
import fr.sirs.core.model.sql.LitSqlHelper;
import fr.sirs.core.model.sql.SQLHelper;
import fr.sirs.map.FXMapPane;
import fr.sirs.plugin.lit.map.LitToolBar;
import fr.sirs.util.FXFreeTab;
import java.awt.Color;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javax.measure.unit.NonSI;
import org.geotoolkit.cql.CQLException;
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
    public SQLHelper getSQLHelper() {
        return LitSqlHelper.getInstance();
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
    
    @Override
    public List<MapItem> getMapItems() {
        try {
        final Function<Class<? extends Element>, StructBeanSupplier> getDefaultSupplierForClass = (Class<? extends Element> c) ->{
            return new StructBeanSupplier(c, () -> getSession().getRepositoryForClass(c).getAll());
        };
            //troncons
            final BeanStore tronconStore = new BeanStore(getDefaultSupplierForClass.apply(TronconLit.class));
            List<MapLayer> layers = CorePlugin.buildLayers(tronconStore, LAYER_NAME, createLitStyle(), createTronconSelectionStyle(false),true);

            MapItem bergeContainer = MapBuilder.createItem();
            bergeContainer.setName("Module lits");
            bergeContainer.items().addAll(layers);
            return Collections.singletonList(bergeContainer);
        } catch (Exception e) {
            throw new SirsCoreRuntimeException(e);
        }
    }

    public static MutableStyle createLitStyle() throws CQLException, URISyntaxException{
        final Stroke stroke1 = SF.stroke(SF.literal(new Color(0, 100, 80)),LITERAL_ONE_FLOAT,FF.literal(9),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line1 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke1,LITERAL_ONE_FLOAT);

        final Stroke stroke2 = SF.stroke(SF.literal(new Color(0, 255, 200)),LITERAL_ONE_FLOAT,FF.literal(7),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line2 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke2,LITERAL_ONE_FLOAT);

        final Stroke stroke3 = SF.stroke(SF.literal(new Color(0, 100, 80)),LITERAL_ONE_FLOAT,FF.literal(1),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line3 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke3,LITERAL_ONE_FLOAT);

        return SF.style(line1,line2,line3);
    }
}
