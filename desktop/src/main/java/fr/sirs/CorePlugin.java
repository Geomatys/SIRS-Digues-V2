

package fr.sirs;

import com.vividsolutions.jts.geom.Geometry;
import fr.sirs.core.SirsCore;
import java.io.IOException;
import java.sql.SQLException;

import fr.sirs.digue.FXDiguePane;
import fr.sirs.digue.FXTronconDiguePane;
import org.geotoolkit.data.bean.BeanStore;
import fr.sirs.theme.ContactsTheme;
import fr.sirs.theme.DesordreTheme;
import fr.sirs.theme.DocumentsTheme;
import fr.sirs.theme.EmpriseCommunaleTheme;
import fr.sirs.theme.EvenementsHydrauliquesTheme;
import fr.sirs.theme.FrancBordTheme;
import fr.sirs.theme.MesureEvenementsTheme;
import fr.sirs.theme.PrestationsTheme;
import fr.sirs.theme.ProfilsEnTraversTheme;
import fr.sirs.theme.ReseauxDeVoirieTheme;
import fr.sirs.theme.ReseauxEtOuvragesTheme;
import fr.sirs.theme.StructuresTheme;
import fr.sirs.theme.ui.FXStructurePane;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.CommuneTroncon;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Fondation;
import fr.sirs.core.model.FrontFrancBord;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.OuvertureBatardable;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.OuvrageRevanche;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.PiedDigue;
import fr.sirs.core.model.PiedFrontFrancBord;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.SommetRisberme;
import fr.sirs.core.model.TalusDigue;
import fr.sirs.core.model.TalusRisberme;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.map.BorneDigueCache;
import fr.sirs.util.FXFreeTab;
import java.awt.Color;
import java.beans.PropertyDescriptor;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javax.measure.unit.NonSI;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.ArraysExt;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.bean.BeanFeatureSupplier;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.ext.graduation.GraduationSymbolizer;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.filter.DefaultLiteral;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.style.RandomStyleBuilder;
import org.geotoolkit.style.StyleConstants;
import static org.geotoolkit.style.StyleConstants.*;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.style.Fill;
import org.opengis.style.Graphic;
import org.opengis.style.GraphicStroke;
import org.opengis.style.GraphicalSymbol;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.Mark;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.Stroke;
import org.opengis.style.TextSymbolizer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CorePlugin extends Plugin {
    
    private static final FilterFactory2 FF = GO2Utilities.FILTER_FACTORY;
    private static final MutableStyleFactory SF = GO2Utilities.STYLE_FACTORY;
    
    /**
     * Plugin correspondant au desktop et au launcher.
     */
    public static final String NAME = "core";
    
    private static final Class[] VALID_CLASSES = new Class[]{
        byte.class,
        short.class,
        int.class,
        long.class,
        float.class,
        double.class,
        boolean.class,
        Boolean.class,
        Byte.class,
        Short.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        String.class,
        LocalDateTime.class
    };
    
    private static final Predicate<PropertyDescriptor> MAPPROPERTY_PREDICATE = new Predicate<PropertyDescriptor>(){

        @Override
        public boolean test(PropertyDescriptor t) {
            final Class c = t.getReadMethod().getReturnType();
            return ArraysExt.contains(VALID_CLASSES, c) || Geometry.class.isAssignableFrom(c);
        }
        
    };
    
    public CorePlugin() {
        name = NAME;
    }

    @Override
    public List<MapItem> getMapItems() {
        final List<MapItem> items = new ArrayList<>();
        
        final TronconDigueRepository repo = getSession().getTronconDigueRepository();
        final BorneDigueRepository bornesRepo = getSession().getBorneDigueRepository();
        
        try{
            
            //troncons
            final BeanStore tronconStore = new BeanStore(
                    new BeanFeatureSupplier(TronconDigue.class, "id", "geometry", 
                            (PropertyDescriptor t) -> MAPPROPERTY_PREDICATE.test(t), 
                            null, SirsCore.getEpsgCode(), repo::getAll)
            );
            items.addAll(buildLayers(tronconStore,createTronconStyle(),createTronconSelectionStyle(),true));
            
            //bornes
            final BorneDigueCache cache = new BorneDigueCache();
            final BeanStore borneStore = new BeanStore(
                    new BeanFeatureSupplier(BorneDigue.class, "id", "geometry", 
                            (PropertyDescriptor t) -> MAPPROPERTY_PREDICATE.test(t), 
                            null, SirsCore.getEpsgCode(), cache::getAll)
            );
            items.addAll(buildLayers(borneStore,createBorneStyle(),createBorneSelectionStyle(),true));
            
            //structures
            final BeanStore structStore = new BeanStore(
                    new StructBeanSupplier(Crete.class),
                    new StructBeanSupplier(OuvrageRevanche.class),
                    new StructBeanSupplier(TalusDigue.class),
                    new StructBeanSupplier(SommetRisberme.class),
                    new StructBeanSupplier(TalusRisberme.class),
                    new StructBeanSupplier(PiedDigue.class),
                    new StructBeanSupplier(Fondation.class),
                    new StructBeanSupplier(FrontFrancBord.class),
                    new StructBeanSupplier(PiedFrontFrancBord.class),
                    new StructBeanSupplier(VoieAcces.class),
                    new StructBeanSupplier(OuvrageFranchissement.class),
                    new StructBeanSupplier(OuvertureBatardable.class),
                    new StructBeanSupplier(VoieDigue.class),
                    new StructBeanSupplier(OuvrageVoirie.class),
                    new StructBeanSupplier(Desordre.class),
                    new StructBeanSupplier(Prestation.class),
                    new StructBeanSupplier(LaisseCrue.class),
                    new StructBeanSupplier(MonteeEaux.class),
                    new StructBeanSupplier(LigneEau.class),
                    new StructBeanSupplier(CommuneTroncon.class)
            );
                        
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
            
            final MapItem structLayer = MapBuilder.createItem();
            structLayer.setName("Structures");
            structLayer.items().addAll( buildLayers(structStore, colors, createTronconSelectionStyle(),false) );
            items.add(structLayer);
               
            
        }catch(Exception ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        
        return items;
    }

    private class StructBeanSupplier extends BeanFeatureSupplier{

        public StructBeanSupplier(final Class clazz) {
            super(clazz, "id", "geometry", 
                (PropertyDescriptor t) -> MAPPROPERTY_PREDICATE.test(t), 
                null, SirsCore.getEpsgCode(), new StructSupplier(clazz::isInstance));
        }
        
    }
    
    private class StructSupplier implements BeanStore.FeatureSupplier{

        private final Predicate predicate;
        
        public StructSupplier(Predicate predicate) {
            this.predicate = predicate;
        }
        
        @Override
        public Iterable get() {
            return new SubIterable(predicate);
        }
        
    }
    
    private class SubIterable implements Iterable{

        private final Predicate predicate;

        public SubIterable(Predicate predicate) {
            this.predicate = predicate;
        }
        
        public Iterator iterator() {
            final TronconDigueRepository repo = getSession().getTronconDigueRepository();
            final List<TronconDigue> troncons = repo.getAll();
            final List col = new ArrayList();
            for(TronconDigue td : troncons){
                col.addAll(td.structures.filtered(predicate));
            }
            return col.iterator();
        }
        
    }
    
    private List<MapLayer> buildLayers(FeatureStore store, MutableStyle baseStyle, MutableStyle selectionStyle, boolean visible) throws DataStoreException{
        final List<MapLayer> layers = new ArrayList<>();
        final org.geotoolkit.data.session.Session symSession = store.createSession(false);
        for(Name name : store.getNames()){
            final FeatureCollection col = symSession.getFeatureCollection(QueryBuilder.all(name));
            final MutableStyle style = (baseStyle==null) ? RandomStyleBuilder.createRandomVectorStyle(col.getFeatureType()) : baseStyle;
            final FeatureMapLayer fml = MapBuilder.createFeatureLayer(col, style);
            
            final FeatureMapLayer.DimensionDef datefilter = new FeatureMapLayer.DimensionDef(
                    CommonCRS.Temporal.JAVA.crs(), 
                    GO2Utilities.FILTER_FACTORY.property("date_debut"), 
                    GO2Utilities.FILTER_FACTORY.property("date_fin")
            );
            fml.getExtraDimensions().add(datefilter);
            fml.setVisible(visible);
            fml.setName(name.getLocalPart());
            
            if(selectionStyle!=null) fml.setSelectionStyle(selectionStyle);
            
            layers.add(fml);
        }
        return layers;
    }
    
    private List<MapLayer> buildLayers(FeatureStore store, Color[] colors, MutableStyle selectionStyle, boolean visible) throws DataStoreException{
        final List<MapLayer> layers = new ArrayList<>();
        final org.geotoolkit.data.session.Session symSession = store.createSession(false);
        int i=0;
        for(Name name : store.getNames()){
            final FeatureCollection col = symSession.getFeatureCollection(QueryBuilder.all(name));
            final int d = (int)((i%colors.length)*1.5);
            final MutableStyle baseStyle = createStructureStyle(colors[i%colors.length],d);
            final MutableStyle style = (baseStyle==null) ? RandomStyleBuilder.createRandomVectorStyle(col.getFeatureType()) : baseStyle;
            final FeatureMapLayer fml = MapBuilder.createFeatureLayer(col, style);
            
            final FeatureMapLayer.DimensionDef datefilter = new FeatureMapLayer.DimensionDef(
                    CommonCRS.Temporal.JAVA.crs(), 
                    GO2Utilities.FILTER_FACTORY.property("date_debut"), 
                    GO2Utilities.FILTER_FACTORY.property("date_fin")
            );
            fml.getExtraDimensions().add(datefilter);
            fml.setVisible(visible);
            fml.setName(name.getLocalPart());
            
            if(selectionStyle!=null) fml.setSelectionStyle(selectionStyle);
            
            layers.add(fml);
            i++;
        }
        return layers;
    }

    @Override
    public List<MenuItem> getMapActions(Object obj) {
        final List<MenuItem> lst = new ArrayList<>();
        
        if(obj instanceof TronconDigue){
            lst.add(new ViewFormItem(obj));
            
        }else if(obj instanceof Objet){
            lst.add(new ViewFormItem(obj));
        }
        
        return lst;
    }
    
    @Override
    public void load() throws SQLException, IOException {
        themes.add(new StructuresTheme());
        themes.add(new FrancBordTheme());
        themes.add(new ReseauxDeVoirieTheme());
        themes.add(new ReseauxEtOuvragesTheme());
        themes.add(new DesordreTheme());
        themes.add(new PrestationsTheme());
        themes.add(new MesureEvenementsTheme());
        themes.add(new EmpriseCommunaleTheme());
        themes.add(new ProfilsEnTraversTheme());
        themes.add(new ContactsTheme());
        themes.add(new EvenementsHydrauliquesTheme());
        themes.add(new DocumentsTheme());
        
    }

    private static MutableStyle createTronconStyle() throws CQLException, URISyntaxException{
        final Stroke stroke1 = SF.stroke(SF.literal(Color.DARK_GRAY),LITERAL_ONE_FLOAT,FF.literal(4),
                STROKE_JOIN_BEVEL, STROKE_CAP_BUTT, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line1 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke1,LITERAL_ONE_FLOAT);
        
        final Stroke stroke2 = SF.stroke(SF.literal(Color.WHITE),LITERAL_ONE_FLOAT,FF.literal(2),
                STROKE_JOIN_BEVEL, STROKE_CAP_BUTT, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line2 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke2,LITERAL_ONE_FLOAT);
        
//        final Expression size = GO2Utilities.FILTER_FACTORY.literal(18);
//        final List<GraphicalSymbol> symbols = new ArrayList<>();
//        final GraphicalSymbol external = SF.externalGraphic(
//                    SF.onlineResource(CorePlugin.class.getResource("/fr/sirs/arrow-white.png").toURI()),
//                    "image/png",null);
//        symbols.add(external);        
//        final Graphic graphic = SF.graphic(symbols, LITERAL_ONE_FLOAT, 
//                size, DEFAULT_GRAPHIC_ROTATION, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);
//
//        final Expression initialGap = FF.literal(10);
//        final Expression strokeGap = FF.literal(100);
//        final GraphicStroke graphicStroke = SF.graphicStroke(graphic,strokeGap,initialGap);
//        
//        final Stroke gstroke = SF.stroke(graphicStroke,DEFAULT_FILL_COLOR,LITERAL_ONE_FLOAT,LITERAL_ONE_FLOAT,
//                STROKE_JOIN_BEVEL,STROKE_CAP_ROUND,null,LITERAL_ZERO_FLOAT);
//        final LineSymbolizer direction = SF.lineSymbolizer("",(Expression)null,null,null,gstroke,null);
        
        return SF.style(line1,line2);
    }
    
    private static MutableStyle createTronconSelectionStyle() throws URISyntaxException{
        final Stroke stroke1 = SF.stroke(SF.literal(Color.GREEN),LITERAL_ONE_FLOAT,FF.literal(2),
                STROKE_JOIN_BEVEL, STROKE_CAP_BUTT, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line1 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke1,LITERAL_ONE_FLOAT);
                
        
        final Expression size = GO2Utilities.FILTER_FACTORY.literal(18);
        final List<GraphicalSymbol> symbols = new ArrayList<>();
        final GraphicalSymbol external = SF.externalGraphic(
                    SF.onlineResource(CorePlugin.class.getResource("/fr/sirs/arrow-green.png").toURI()),
                    "image/png",null);
        symbols.add(external);        
        final Graphic graphic = SF.graphic(symbols, LITERAL_ONE_FLOAT, 
                size, DEFAULT_GRAPHIC_ROTATION, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);

        final Expression initialGap = FF.literal(10);
        final Expression strokeGap = FF.literal(200);
        final GraphicStroke graphicStroke = SF.graphicStroke(graphic,strokeGap,initialGap);
        
        final Stroke gstroke = SF.stroke(graphicStroke,DEFAULT_FILL_COLOR,LITERAL_ONE_FLOAT,LITERAL_ONE_FLOAT,
                STROKE_JOIN_BEVEL,STROKE_CAP_ROUND,null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer direction = SF.lineSymbolizer("",(Expression)null,null,null,gstroke,null);
        
        
        final GraduationSymbolizer grad = new GraduationSymbolizer();
        //tous les 100metres
        final GraduationSymbolizer.Graduation g1 = new GraduationSymbolizer.Graduation();
        g1.setUnit(new DefaultLiteral("m"));
        g1.setStep(FF.literal(100));
        g1.setStroke(SF.stroke(Color.RED, 3));
        g1.setFont(SF.font(12));
        g1.setSize(FF.literal(12));
        grad.getGraduations().add(g1);
        //tous les 10metres
        final GraduationSymbolizer.Graduation g2 = new GraduationSymbolizer.Graduation();
        g2.setUnit(new DefaultLiteral("m"));
        g2.setStep(FF.literal(10));
        g2.setStroke(SF.stroke(Color.BLACK, 1));
        g2.setFont(SF.font(10));
        g2.setSize(FF.literal(4));
        grad.getGraduations().add(g2);
        
        final MutableStyle style = SF.style(line1,direction,grad);
        return style;
    }
    
    private static MutableStyle createBorneStyle(){
        final Expression size = GO2Utilities.FILTER_FACTORY.literal(7);

        final List<GraphicalSymbol> symbols = new ArrayList<>();
        final Stroke stroke = SF.stroke(SF.literal(Color.BLACK),LITERAL_ONE_FLOAT,LITERAL_ONE_FLOAT,
                STROKE_JOIN_BEVEL, STROKE_CAP_BUTT, null,LITERAL_ZERO_FLOAT);
        final Fill fill = SF.fill(Color.WHITE);
        final Mark mark = SF.mark(StyleConstants.MARK_CIRCLE, fill, stroke);
        symbols.add(mark);
        final Graphic graphic = SF.graphic(symbols, LITERAL_ONE_FLOAT, 
                size, LITERAL_ONE_FLOAT, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);

        final PointSymbolizer pointSymbolizer = SF.pointSymbolizer("symbol",(String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,graphic);
        
        final TextSymbolizer ts = SF.textSymbolizer(
                SF.fill(Color.BLACK), DEFAULT_FONT, 
                SF.halo(Color.WHITE, 2), 
                FF.property("libelle"), 
                SF.pointPlacement(SF.anchorPoint(0, 0.25), SF.displacement(5, 0), FF.literal(0)), null);
        
        final MutableRule ruleClose = SF.rule(pointSymbolizer, ts);
        ruleClose.setMaxScaleDenominator(50000);
        
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        fts.rules().add(ruleClose);
        final MutableStyle style = SF.style();
        style.featureTypeStyles().add(fts);
        return style;
    }
    
    private static MutableStyle createBorneSelectionStyle(){
        final Expression size = GO2Utilities.FILTER_FACTORY.literal(10);

        final List<GraphicalSymbol> symbols = new ArrayList<>();
        final Stroke stroke = SF.stroke(Color.BLACK, 1);
        final Fill fill = SF.fill(Color.GREEN);
        final Mark mark = SF.mark(StyleConstants.MARK_CIRCLE, fill, stroke);
        symbols.add(mark);
        final Graphic graphic = SF.graphic(symbols, LITERAL_ONE_FLOAT, 
                size, LITERAL_ONE_FLOAT, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);

        final PointSymbolizer pointSymbolizer = SF.pointSymbolizer("symbol",(String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,graphic);
        
        final TextSymbolizer ts = SF.textSymbolizer(
                SF.fill(Color.BLACK), SF.font(13), 
                SF.halo(Color.GREEN, 2), 
                FF.property("libelle"), 
                SF.pointPlacement(SF.anchorPoint(0, 0.25), SF.displacement(5, 0), FF.literal(0)), null);
        
        final MutableRule ruleClose = SF.rule(pointSymbolizer, ts);
        ruleClose.setMaxScaleDenominator(50000);
        
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        fts.rules().add(ruleClose);
        final MutableStyle style = SF.style();
        style.featureTypeStyles().add(fts);
        return style;
    }
    
    
    private static MutableStyle createStructureStyle(Color col, int doffset){
        final Expression offset = GO2Utilities.FILTER_FACTORY.literal(doffset);
        final Expression color = SF.literal(col);
        final Expression width = GO2Utilities.FILTER_FACTORY.literal(2);
        final Stroke lineStroke = SF.stroke(color,LITERAL_ONE_FLOAT,width,
                STROKE_JOIN_BEVEL, STROKE_CAP_BUTT, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer lineSymbolizer = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,lineStroke,offset);
        
        //the visual element
        final Expression size = GO2Utilities.FILTER_FACTORY.literal(13);

        final List<GraphicalSymbol> symbols = new ArrayList<>();
        final Stroke stroke = null;
        final Fill fill = SF.fill(col);
        final Mark mark = SF.mark(StyleConstants.MARK_TRIANGLE, fill, stroke);
        symbols.add(mark);
        final Graphic graphic = SF.graphic(symbols, LITERAL_ONE_FLOAT, 
                size, LITERAL_ONE_FLOAT, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);

        final PointSymbolizer pointSymbolizer = SF.pointSymbolizer("symbol",(String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,graphic);
        
        final MutableRule ruleClose = SF.rule(lineSymbolizer);
        ruleClose.setMaxScaleDenominator(500000);
        final MutableRule ruleFar = SF.rule(pointSymbolizer);
        ruleFar.setMinScaleDenominator(500000);
        
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        fts.rules().add(ruleClose);
        fts.rules().add(ruleFar);
        
        final MutableStyle style = SF.style();
        style.featureTypeStyles().add(fts);
        return style;
    }
    
    private static class ViewFormItem extends MenuItem{

        public ViewFormItem(Object candidate) {
            if(candidate instanceof Digue){
                final Digue cdt = (Digue) candidate;
                final String text = "Digue : "+cdt.getLibelle();
                setText(text);
                
                setOnAction((ActionEvent event) -> {
                    final FXDiguePane controller = new FXDiguePane();
                    controller.setDigue(cdt);
                    final Tab tab = new FXFreeTab(text);
                    tab.setContent(controller);
                    Injector.getBean(Session.class).getFrame().addTab(tab);
                });
                
            }else if(candidate instanceof TronconDigue){
                final TronconDigue cdt = (TronconDigue) candidate;
                final String text = "Tronçon : "+cdt.getLibelle();
                setText(text);
                
                setOnAction((ActionEvent event) -> {
                    final FXTronconDiguePane controller = new FXTronconDiguePane();
                    controller.setTroncon(cdt);
                    final Tab tab = new FXFreeTab(text);
                    tab.setContent(controller);
                    Injector.getBean(Session.class).getFrame().addTab(tab);
                });
            }else if(candidate instanceof Objet){
                final Objet cdt = (Objet) candidate;
                final String text = "Objet "+cdt.getClass().getSimpleName()+" : "+cdt.getId();
                setText(text);
                
                setOnAction((ActionEvent event) -> {
                    final FXStructurePane controller = new FXStructurePane(cdt);
                    final Tab tab = new FXFreeTab(text);
                    tab.setContent(controller);
                    Injector.getBean(Session.class).getFrame().addTab(tab);
                });
            }
            
            
        }
        
    }
    
}
