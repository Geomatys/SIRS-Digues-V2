
package fr.sirs.plugin.vegetation;

import com.vividsolutions.jts.geom.Geometry;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.RefTraitementVegetation;
import fr.sirs.core.model.ZoneVegetation;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.GridPane;
import javax.measure.unit.NonSI;
import javax.swing.ImageIcon;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.data.memory.MemoryFeatureStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.feature.type.FeatureType;
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
import static org.geotoolkit.style.StyleConstants.DEFAULT_ANCHOR_POINT;
import static org.geotoolkit.style.StyleConstants.DEFAULT_DISPLACEMENT;
import static org.geotoolkit.style.StyleConstants.DEFAULT_GRAPHIC_ROTATION;
import static org.geotoolkit.style.StyleConstants.LITERAL_ONE_FLOAT;
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
import org.opengis.style.Stroke;

/**
 * Panneau d'ajout de couche de données analyzées.
 * 
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPlanLayerPane extends GridPane{

    @FXML private ChoiceBox<Integer> uiAnnee;
    @FXML private CheckBox uiParcelleType;
    @FXML private CheckBox uiTrmtReel;
    @FXML private CheckBox uiTrmtPlanif;
    @FXML private Button uiAddButton;

    private final PlanVegetation plan;

    public FXPlanLayerPane() {
        plan = VegetationSession.INSTANCE.planProperty().get();
        SIRS.loadFXML(this);
    }

    public void initialize(){
        final int anneDebut = plan.getAnneDebut();
        final int anneFin = plan.getAnneFin();

        final ObservableList<Integer> years = FXCollections.observableArrayList();
        for(int year = anneDebut;year<anneFin;year++) years.add(year);
        uiAnnee.setItems(years);
        if(!years.isEmpty()) uiAnnee.valueProperty().set(years.get(0));

        uiAddButton.disableProperty().bind(
                uiTrmtReel.selectedProperty().not()
                .and(uiTrmtPlanif.selectedProperty().not())
                .and(uiParcelleType.selectedProperty().not())
                .or(uiAnnee.valueProperty().isNull())
        );
    }

    @FXML
    public void addLayer(ActionEvent event) {
        final Integer year = uiAnnee.valueProperty().get();
        final MapItem vegetationGroup = VegetationSession.INSTANCE.getVegetationGroup();

        if(uiParcelleType.isSelected()){
            //etat des parcelles : planifié, traité, non planifié etc...

            final ParcelleVegetationRepository parcelleRepo = VegetationSession.INSTANCE.getParcelleRepo();

            final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
            ftb.setName("type");
            ftb.add("id", String.class);
            ftb.add("geometry", Geometry.class, Injector.getSession().getProjection());
            ftb.add("etat", String.class);
            final FeatureType ft = ftb.buildFeatureType();

            final MemoryFeatureStore store = new MemoryFeatureStore(ft, true);
            FeatureWriter writer;
            try {
                writer = store.getFeatureWriterAppend(ft.getName());
            } catch (DataStoreException ex) {
                //n'arrive pas avec un feature store en mémoire
                throw new RuntimeException(ex);
            }
            for(ParcelleVegetation pv : parcelleRepo.getByPlanId(plan.getId())){
                final List<Boolean> planifications = pv.getPlanifications();
                int index = year - plan.getAnneDebut();
                boolean planifié = false;
                if(planifications!=null && planifications.size()>index) planifié = planifications.get(index);

                final Feature feature = writer.next();
                feature.setPropertyValue("id", pv.getId());
                feature.setPropertyValue("geometry", pv.getGeometry());
                feature.setPropertyValue("etat", VegetationSession.getParcelleEtat(pv, planifié, year));
                feature.getUserData().put(BeanFeature.KEY_BEAN, pv);
                writer.write();
            }
            writer.close();


            final FeatureCollection col = store.createSession(true).getFeatureCollection(QueryBuilder.all(ft.getName()));
            final MapLayer layer = MapBuilder.createFeatureLayer(col);
            layer.setName("Etat parcelle "+year);
            layer.setStyle(createParcelleStateStyle());

            vegetationGroup.items().add(layer);
        }

        if(uiTrmtReel.isSelected()){
            //etat des parcelles : traité, non traité

            final ParcelleVegetationRepository parcelleRepo = VegetationSession.INSTANCE.getParcelleRepo();

            final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
            ftb.setName("type");
            ftb.add("id", String.class);
            ftb.add("geometry", Geometry.class, Injector.getSession().getProjection());
            ftb.add("etat", String.class);
            final FeatureType ft = ftb.buildFeatureType();

            final MemoryFeatureStore store = new MemoryFeatureStore(ft, true);
            FeatureWriter writer;
            try {
                writer = store.getFeatureWriterAppend(ft.getName());
            } catch (DataStoreException ex) {
                //n'arrive pas avec un feature store en mémoire
                throw new RuntimeException(ex);
            }
            for(ParcelleVegetation pv : parcelleRepo.getByPlanId(plan.getId())){
                final Feature feature = writer.next();
                feature.setPropertyValue("id", pv.getId());
                feature.setPropertyValue("geometry", pv.getGeometry());
                feature.setPropertyValue("etat", VegetationSession.isParcelleTraite(pv, year));
                feature.getUserData().put(BeanFeature.KEY_BEAN, pv);
                writer.write();
            }
            writer.close();


            final FeatureCollection col = store.createSession(true).getFeatureCollection(QueryBuilder.all(ft.getName()));
            final MapLayer layer = MapBuilder.createFeatureLayer(col);
            layer.setName("Traitement réel "+year);
            layer.setStyle(createTraitementReelStyle());

            vegetationGroup.items().add(layer);
        }

        if(uiTrmtPlanif.isSelected()){
            //etat des vegetations par type de traitement a faire

            final ParcelleVegetationRepository parcelleRepo = VegetationSession.INSTANCE.getParcelleRepo();

            final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
            ftb.setName("type");
            ftb.add("id", String.class);
            ftb.add("geometry", Geometry.class, Injector.getSession().getProjection());
            ftb.add("etat", String.class);
            final FeatureType ft = ftb.buildFeatureType();

            final MemoryFeatureStore store = new MemoryFeatureStore(ft, true);
            FeatureWriter writer;
            try {
                writer = store.getFeatureWriterAppend(ft.getName());
            } catch (DataStoreException ex) {
                //n'arrive pas avec un feature store en mémoire
                throw new RuntimeException(ex);
            }

            //on liste tous les traitements
            final AbstractSIRSRepository<RefTraitementVegetation> trmtRepo = Injector.getSession().getRepositoryForClass(RefTraitementVegetation.class);
            final Map<String,String> trmts = new HashMap<>();
            for(RefTraitementVegetation trmt : trmtRepo.getAll()){
                trmts.put(trmt.getDocumentId(), trmt.getLibelle());
            }
            trmts.put(null, "-");

            for(ParcelleVegetation pv : parcelleRepo.getByPlanId(plan.getId())){

                //on regarde si c'est la premiere année
                final int planifState = VegetationSession.getParcellePlanifState(plan, pv, year);

                final List<ZoneVegetation> vegetations = new ArrayList<>();
                vegetations.addAll(VegetationSession.INSTANCE.getHerbaceeRepo().getByParcelleId(pv.getDocumentId()));
                vegetations.addAll(VegetationSession.INSTANCE.getInvasiveRepo().getByParcelleId(pv.getDocumentId()));
                vegetations.addAll(VegetationSession.INSTANCE.getPeuplementRepo().getByParcelleId(pv.getDocumentId()));
                vegetations.addAll(VegetationSession.INSTANCE.getArbreRepo().getByParcelleId(pv.getDocumentId()));

                for(ZoneVegetation zone : vegetations){
                    String etat = null;

                    if(planifState == 1){
                        //premiere année
                        String tid = zone.getTypeTraitementPonctuelId();
                        if(tid==null || tid.isEmpty()){
                            tid = zone.getTypeTraitementId();
                        }
                        etat = trmts.get(tid);
                    }else if(planifState == 1){
                        final String tid = zone.getTypeTraitementId();
                        etat = trmts.get(tid);
                    }

                    final Feature feature = writer.next();
                    feature.setPropertyValue("id", pv.getId());
                    feature.setPropertyValue("geometry", zone.getGeometry());
                    feature.setPropertyValue("etat", etat);
                    feature.getUserData().put(BeanFeature.KEY_BEAN, pv);
                    writer.write();
                }
            }
            writer.close();


            final FeatureCollection col = store.createSession(true).getFeatureCollection(QueryBuilder.all(ft.getName()));
            final MapLayer layer = MapBuilder.createFeatureLayer(col);
            layer.setName("Traitement planifié "+year);
            layer.setStyle(createTraitementPlanifiéStyle(trmts.values()));

            vegetationGroup.items().add(layer);

        }
        
    }

    private static MutableStyle createTraitementReelStyle() {
        final MutableStyleFactory SF = GO2Utilities.STYLE_FACTORY;

        final MutableStyle style = SF.style();
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        style.featureTypeStyles().add(fts);

        final MutableRule traiteRule = createParcelleRule(Boolean.TRUE,      new Color(0.0f, 0.7f, 0.0f, 0.6f));
        traiteRule.setName("Traité");
        traiteRule.setDescription(SF.description(traiteRule.getName(),traiteRule.getName()));

        final MutableRule nonTraiteRule = createParcelleRule(Boolean.FALSE,      new Color(0.7f, 0.0f, 0.0f, 0.6f));
        nonTraiteRule.setName("Non traité");
        nonTraiteRule.setDescription(SF.description(nonTraiteRule.getName(),nonTraiteRule.getName()));

        fts.rules().add(traiteRule);
        fts.rules().add(nonTraiteRule);

        return style;
    }

    private static MutableStyle createTraitementPlanifiéStyle(Collection<String> types){

        final MutableStyleFactory SF = GO2Utilities.STYLE_FACTORY;
        final FilterFactory2 FF = GO2Utilities.FILTER_FACTORY;

        final MutableStyle style = SF.style();
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        style.featureTypeStyles().add(fts);

        for(String str : types){

            final Color color;
            if(str.equals("-")){
                color = Color.LIGHT_GRAY;
            }else{
                color = RandomStyleBuilder.randomColor();
            }
            

            //point rule
            final MutableRule rulePoint = SF.rule();
            rulePoint.setName(str);
            rulePoint.setDescription(SF.description(str, str));
            rulePoint.setFilter(
                    FF.and(
                        FF.equals(FF.function("geometryType", FF.property("geometry")),FF.literal("Point")),
                        FF.equals(FF.property("etat"), FF.literal(str)))
                    );

            final Stroke stroke = SF.stroke(Color.BLACK, 1);
            final Fill fill = SF.fill(color);
            final ExternalMark extMark;
            try {
                extMark = SF.externalMark(
                        SF.onlineResource(IconBuilder.FONTAWESOME.toURI()),
                        "ttf",FontAwesomeIcons.ICON_TREE.codePointAt(0));
            } catch (URISyntaxException ex) {
                //n'arrivera pas
                throw new RuntimeException(ex);
            }

            final Mark mark = SF.mark(extMark, fill, stroke);

            final Expression size = GO2Utilities.FILTER_FACTORY.literal(16);
            final List<GraphicalSymbol> symbols = new ArrayList<>();
            symbols.add(mark);
            final Graphic graphic = SF.graphic(symbols, LITERAL_ONE_FLOAT,
                    size, DEFAULT_GRAPHIC_ROTATION, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);
            rulePoint.symbolizers().add(SF.pointSymbolizer("", FF.property("geometry"), null, NonSI.PIXEL, graphic));

            fts.rules().add(rulePoint);

            //polygon rule
            final MutableRule rulePoly = SF.rule();
            rulePoly.setName(str);
            rulePoly.setDescription(SF.description(str, str));
            rulePoly.setFilter(
                    FF.and(
                        FF.equals(FF.function("geometryType", FF.property("geometry")),FF.literal("Polygon")),
                        FF.equals(FF.property("etat"), FF.literal(str)))
                    );
            final Stroke strokePoly = SF.stroke(Color.BLACK, 1);
            final Fill fillPoly = SF.fill(color);
            rulePoly.symbolizers().add(SF.polygonSymbolizer(strokePoly, fillPoly, null));
            

            fts.rules().add(rulePoly);
        }

        return style;
    }

    private static MutableStyle createParcelleStateStyle() {
        final MutableStyleFactory SF = GO2Utilities.STYLE_FACTORY;

        final MutableStyle style = SF.style();
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        style.featureTypeStyles().add(fts);
        
        fts.rules().add(createParcelleRule(VegetationSession.ETAT_PLANIFIE_TRAITE,      new Color(0.0f, 0.7f, 0.0f, 0.6f)));
        fts.rules().add(createParcelleRule(VegetationSession.ETAT_PLANIFIE_NONTRAITE,   new Color(0.7f, 0.0f, 0.0f, 0.6f)));
        fts.rules().add(createParcelleRule(VegetationSession.ETAT_PLANIFIE_FUTUR,       new Color(0.0f, 0.0f, 0.7f, 0.6f)));
        fts.rules().add(createParcelleRule(VegetationSession.ETAT_NONPLANIFIE_TRAITE,   new Color(0.6f, 0.4f, 0.0f, 0.6f)));
        fts.rules().add(createParcelleRule(VegetationSession.ETAT_NONPLANIFIE_NONTRAITE,new Color(0.7f, 0.7f, 0.7f, 0.6f)));
        fts.rules().add(createParcelleRule(VegetationSession.ETAT_NONPLANIFIE_FUTUR,    new Color(0.7f, 0.7f, 0.7f, 0.6f)));

        return style;
    }

    private static MutableRule createParcelleRule(Object state, Color color){
        final MutableStyleFactory SF = GO2Utilities.STYLE_FACTORY;
        final FilterFactory2 FF = GO2Utilities.FILTER_FACTORY;
        final MutableRule rule = SF.rule();

        rule.setFilter(FF.equals(FF.property("etat"), FF.literal(state)));
        rule.setName(String.valueOf(state));
        rule.setDescription(SF.description(String.valueOf(state), String.valueOf(state)));


        final BufferedImage img = new BufferedImage(4, 100, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, 2, 100);
        g.dispose();

        final ExternalGraphic external = SF.externalGraphic(new ImageIcon(img),Collections.EMPTY_LIST);

        final Expression rotationStart = FF.subtract(FF.literal(0),FF.function("toDegrees", FF.function("startAngle", FF.property("geometry"))));
        final Expression rotationEnd = FF.subtract(FF.literal(0),FF.function("toDegrees", FF.function("endAngle", FF.property("geometry"))));

        final Expression size = GO2Utilities.FILTER_FACTORY.literal(200);
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
        final Stroke lineStroke = SF.stroke(color, 4, null);
        final LineSymbolizer lineSymbol = SF.lineSymbolizer(lineStroke, null);
        rule.symbolizers().add(lineSymbol);

        return rule;
    }


}
