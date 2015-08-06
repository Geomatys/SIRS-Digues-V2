package fr.sirs.plugin.vegetation;

import fr.sirs.Plugin;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.StructBeanSupplier;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.ArbreVegetation;
import fr.sirs.core.model.HerbaceeVegetation;
import fr.sirs.core.model.InvasiveVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.RefTypeInvasiveVegetation;
import fr.sirs.core.model.RefTypePeuplementVegetation;
import fr.sirs.core.model.sql.SQLHelper;
import fr.sirs.core.model.sql.VegetationSqlHelper;
import fr.sirs.plugin.vegetation.map.CreateParcelleTool;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import fr.sirs.map.FXMapPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import javax.measure.unit.NonSI;
import javax.swing.ImageIcon;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.bean.BeanStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.style.RandomStyleBuilder;
import static org.geotoolkit.style.StyleConstants.*;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.style.ExternalGraphic;
import org.opengis.style.ExternalMark;
import org.opengis.style.Fill;
import org.opengis.style.Graphic;
import org.opengis.style.GraphicalSymbol;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.Mark;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.PolygonSymbolizer;
import org.opengis.style.Stroke;

/**
 * Minimal example of a plugin.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginVegetation extends Plugin {
    private static final String NAME = "plugin-vegetation";
    private static final String TITLE = "Module végétation";
    private static final MutableStyleFactory SF = GO2Utilities.STYLE_FACTORY;
    private static final FilterFactory2 FF = GO2Utilities.FILTER_FACTORY;

    private final VegetationToolBar toolbar = new VegetationToolBar();

    public PluginVegetation() {
        name = NAME;
        loadingMessage.set("module végétation");
        themes.add(new ParcelleTheme());
    }

    @Override
    public void load() throws Exception {
        getConfiguration();

        //on force le chargement
        final CreateParcelleTool.Spi spi = CreateParcelleTool.SPI;
        spi.getTitle().toString();
        spi.getAbstract().toString();

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

    @Override
    public List<MapItem> getMapItems() {
        final List<MapItem> items = new ArrayList<>();
        final MapItem vegetationGroup = MapBuilder.createItem();
        vegetationGroup.setName("Végétation");
        vegetationGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
        items.add(vegetationGroup);

        try{
            //parcelles
            final StructBeanSupplier parcelleSupplier = new StructBeanSupplier(ParcelleVegetation.class, () -> getSession().getRepositoryForClass(ParcelleVegetation.class).getAll());
            final BeanStore parcelleStore = new BeanStore(parcelleSupplier);
            final MapLayer parcelleLayer = MapBuilder.createFeatureLayer(parcelleStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(parcelleStore.getNames().iterator().next())));
            parcelleLayer.setName("Parcelles");
            parcelleLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            vegetationGroup.items().add(0,parcelleLayer);

            try{
                final MutableStyle style = createParcelleStyle();
                parcelleLayer.setStyle(style);
            }catch(Exception ex){
                SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }

            //strates herbacée
            final StructBeanSupplier herbeSupplier = new StructBeanSupplier(HerbaceeVegetation.class, () -> getSession().getRepositoryForClass(HerbaceeVegetation.class).getAll());
            final BeanStore herbeStore = new BeanStore(herbeSupplier);
            final MapLayer herbeLayer = MapBuilder.createFeatureLayer(herbeStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(herbeStore.getNames().iterator().next())));
            herbeLayer.setName("Strates herbacée");
            herbeLayer.setStyle(createPolygonStyle(Color.ORANGE));
            herbeLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            vegetationGroup.items().add(0,herbeLayer);

            //arbres
            final StructBeanSupplier arbreSupplier = new StructBeanSupplier(ArbreVegetation.class, () -> getSession().getRepositoryForClass(ArbreVegetation.class).getAll());
            final BeanStore arbreStore = new BeanStore(arbreSupplier);
            final MapLayer arbreLayer = MapBuilder.createFeatureLayer(arbreStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(arbreStore.getNames().iterator().next())));
            arbreLayer.setName("Arbres exceptionnels");
            arbreLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            arbreLayer.setStyle(createArbreStyle());
            vegetationGroup.items().add(0,arbreLayer);

            //peuplements
            final StructBeanSupplier peuplementSupplier = new StructBeanSupplier(PeuplementVegetation.class, () -> getSession().getRepositoryForClass(PeuplementVegetation.class).getAll());
            final BeanStore peuplementStore = new BeanStore(peuplementSupplier);
            final org.geotoolkit.data.session.Session peuplementSession = peuplementStore.createSession(true);
            final MapItem peuplementGroup = MapBuilder.createItem();
            peuplementGroup.setName("Peuplements");
            peuplementGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            vegetationGroup.items().add(0,peuplementGroup);
            //une couche pour chaque type
            final AbstractSIRSRepository<RefTypePeuplementVegetation> typePeuplementRepo = getSession().getRepositoryForClass(RefTypePeuplementVegetation.class);
            for(RefTypePeuplementVegetation ref : typePeuplementRepo.getAll()){
                final String id = ref.getId();
                final Filter filter = FF.equal(FF.property("typePeuplementId"),FF.literal(id));
                final FeatureCollection col = peuplementSession.getFeatureCollection(
                        QueryBuilder.filtered(peuplementStore.getNames().iterator().next(),filter));
                final MapLayer layer = MapBuilder.createFeatureLayer(col);
                layer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
                layer.setStyle(createPolygonStyle(RandomStyleBuilder.randomColor()));
                layer.setName(ref.getLibelle());
                peuplementGroup.items().add(layer);
            }

            //invasives
            final StructBeanSupplier invasiveSupplier = new StructBeanSupplier(InvasiveVegetation.class, () -> getSession().getRepositoryForClass(InvasiveVegetation.class).getAll());
            final BeanStore invasiveStore = new BeanStore(invasiveSupplier);
            final org.geotoolkit.data.session.Session invasiveSession = invasiveStore.createSession(true);
            final MapItem invasivesGroup = MapBuilder.createItem();
            invasivesGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            invasivesGroup.setName("Invasives");
            vegetationGroup.items().add(0,invasivesGroup);
            //une couche pour chaque type
            final AbstractSIRSRepository<RefTypeInvasiveVegetation> typeInvasiveRepo = getSession().getRepositoryForClass(RefTypeInvasiveVegetation.class);
            for(RefTypeInvasiveVegetation ref : typeInvasiveRepo.getAll()){
                final String id = ref.getId();
                final Filter filter = FF.equal(FF.property("typeInvasive"),FF.literal(id));
                final FeatureCollection col = invasiveSession.getFeatureCollection(
                        QueryBuilder.filtered(invasiveStore.getNames().iterator().next(),filter));
                final MapLayer layer = MapBuilder.createFeatureLayer(col);
                layer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
                layer.setStyle(createPolygonStyle(RandomStyleBuilder.randomColor()));
                layer.setName(ref.getLibelle());
                invasivesGroup.items().add(layer);
            }

            //traitements
            final MapItem traitementGroup = MapBuilder.createItem();
            traitementGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            traitementGroup.setName("Traitements");
            vegetationGroup.items().add(0,traitementGroup);

        }catch(Exception ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        return items;
    }

    private static MutableStyle createParcelleStyle() throws IOException{
        final MutableStyle style = SF.style();
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        final MutableRule rule = SF.rule();
        style.featureTypeStyles().add(fts);
        fts.rules().add(rule);

        final BufferedImage img = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("fr/sirs/plugin/vegetation/style/parcelle.png"));
        final ExternalGraphic external = SF.externalGraphic(new ImageIcon(img),Collections.EMPTY_LIST);

//        final Mark mark = SF.getTriangleMark();

        final Expression rotationStart = FF.subtract(FF.literal(0),FF.function("toDegrees", FF.function("startAngle", FF.property("geometry"))));
        final Expression rotationEnd = FF.subtract(FF.literal(0),FF.function("toDegrees", FF.function("endAngle", FF.property("geometry"))));

        final Expression size = GO2Utilities.FILTER_FACTORY.literal(252);
        final List<GraphicalSymbol> symbols = new ArrayList<>();
        symbols.add(external);
        final Graphic graphicStart = SF.graphic(symbols, LITERAL_ONE_FLOAT,
                size, rotationStart, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);
        final Graphic graphicEnd = SF.graphic(symbols, LITERAL_ONE_FLOAT,
                size, rotationEnd, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);

        final PointSymbolizer ptStart = SF.pointSymbolizer("", FF.function("startPoint", FF.property("geometry")), null, NonSI.PIXEL, graphicStart);
        final PointSymbolizer ptEnd = SF.pointSymbolizer("", FF.function("endPoint", FF.property("geometry")), null, NonSI.PIXEL, graphicEnd);

        rule.symbolizers().add(ptStart);
        rule.symbolizers().add(ptEnd);

        //line
        final Stroke lineStroke = SF.stroke(Color.GRAY, 2, new float[]{8,8,8,8,8});
        final LineSymbolizer lineSymbol = SF.lineSymbolizer(lineStroke, null);
        rule.symbolizers().add(lineSymbol);

        return style;
    }

    private static MutableStyle createArbreStyle() throws URISyntaxException{
        final MutableStyle style = SF.style();
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        final MutableRule rule = SF.rule();
        style.featureTypeStyles().add(fts);
        fts.rules().add(rule);

        final Stroke stroke = SF.stroke(Color.BLACK, 1);
        final Fill fill = SF.fill(new Color(0, 200, 0));
        final ExternalMark extMark = SF.externalMark(
                    SF.onlineResource(IconBuilder.FONTAWESOME.toURI()),
                    "ttf",FontAwesomeIcons.ICON_TREE.codePointAt(0));

        final Mark mark = SF.mark(extMark, fill, stroke);

        final Expression size = GO2Utilities.FILTER_FACTORY.literal(16);
        final List<GraphicalSymbol> symbols = new ArrayList<>();
        symbols.add(mark);
        final Graphic graphic = SF.graphic(symbols, LITERAL_ONE_FLOAT,
                size, DEFAULT_GRAPHIC_ROTATION, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);

        final PointSymbolizer ptSymbol = SF.pointSymbolizer("", FF.property("geometry"), null, NonSI.PIXEL, graphic);

        rule.symbolizers().add(ptSymbol);
        return style;
    }


    private static MutableStyle createPolygonStyle(Color color){
        final MutableStyle style = SF.style();
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        final MutableRule rule = SF.rule();
        style.featureTypeStyles().add(fts);
        fts.rules().add(rule);

        final Stroke stroke = SF.stroke(Color.BLACK, 1);
        final Fill fill = SF.fill(color);
        final PolygonSymbolizer symbolizer = SF.polygonSymbolizer(stroke, fill, null);
        rule.symbolizers().add(symbolizer);
        return style;
    }
    
}
