package fr.sirs.plugin.vegetation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.StructBeanSupplier;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.AbstractZoneVegetationRepository;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.model.ArbreVegetation;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.HerbaceeVegetation;
import fr.sirs.core.model.InvasiveVegetation;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.ParamFrequenceTraitementVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefFrequenceTraitementVegetation;
import fr.sirs.core.model.RefSousTraitementVegetation;
import fr.sirs.core.model.RefTypeInvasiveVegetation;
import fr.sirs.core.model.RefTypePeuplementVegetation;
import fr.sirs.core.model.TraitementParcelleVegetation;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.core.model.sql.SQLHelper;
import fr.sirs.core.model.sql.VegetationSqlHelper;
import fr.sirs.map.FXMapPane;
import fr.sirs.plugin.vegetation.map.CreateParcelleTool;
import java.awt.Color;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import javax.measure.unit.NonSI;
import javax.swing.ImageIcon;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.bean.BeanStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.primitive.jts.JTSLineIterator;
import org.geotoolkit.displayt2d.style.j2d.DoublePathWalker;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.map.FeatureMapLayer;
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
 * @author Johann Sorel (Geomatys)
 */
public class PluginVegetation extends Plugin {

    public static final String PARCELLE_LAYER_NAME = LabelMapper.get(ParcelleVegetation.class).mapClassName();
    public static final String VEGETATION_GROUP_NAME = "Végétation";

    private static final String NAME = "plugin-vegetation";
    private static final String TITLE = "Module végétation";
    private static final MutableStyleFactory SF = GO2Utilities.STYLE_FACTORY;
    private static final FilterFactory2 FF = GO2Utilities.FILTER_FACTORY;

    public static final String DEFAULT_PEUPLEMENT_VEGETATION_TYPE = "RefTypePeuplementVegetation:99";
    public static final String DEFAULT_INVASIVE_VEGETATION_TYPE = "RefTypeInvasiveVegetation:99";

    private final VegetationToolBar toolbar = new VegetationToolBar();

    public PluginVegetation() {
        name = NAME;
        loadingMessage.set("module végétation");
        themes.add(new VegetationTraitementTheme());
        themes.add(new PlanDeGestionTheme());
        themes.add(new ExploitationTheme());
    }

    @Override
    public void load() throws Exception {
        getConfiguration();

        //on force le chargement
        final CreateParcelleTool.Spi spi = CreateParcelleTool.SPI;
        spi.getTitle().toString();
        spi.getAbstract().toString();

        //on ecoute le changement de plan de gestion
        VegetationSession.INSTANCE.planProperty().addListener(new ChangeListener<PlanVegetation>() {
            @Override
            public void changed(ObservableValue<? extends PlanVegetation> observable, PlanVegetation oldValue, PlanVegetation newValue) {
                updatePlanLayers(newValue);
            }
        });

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
        final MapItem vegetationGroup = VegetationSession.INSTANCE.getVegetationGroup();
        items.add(vegetationGroup);
        return items;
    }

    private void updatePlanLayers(PlanVegetation plan){

        //on efface les anciens layers
        final MapItem vegetationGroup = VegetationSession.INSTANCE.getVegetationGroup();
        vegetationGroup.items().clear();

        if(plan==null) return;

        final VegetationSession vs = VegetationSession.INSTANCE;

        try{
            //parcelles
            final StructBeanSupplier parcelleSupplier = new StructBeanSupplier(ParcelleVegetation.class, () -> 
                    vs.getParcelleRepo().getByPlanId(plan.getId()));
            final BeanStore parcelleStore = new BeanStore(parcelleSupplier);
            final MapLayer parcelleLayer = MapBuilder.createFeatureLayer(parcelleStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(parcelleStore.getNames().iterator().next())));

            parcelleLayer.setName(PARCELLE_LAYER_NAME);
            parcelleLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            vegetationGroup.items().add(0,parcelleLayer);

            parcelleLayer.setStyle(createParcelleStyle());
            parcelleLayer.setSelectionStyle(createParcelleStyleSelected());

            //strates herbacée
            final StructBeanSupplier herbeSupplier = new StructBeanSupplier(HerbaceeVegetation.class, 
                    () -> vs.getHerbaceeRepo().getByParcelleIds(getParcelleIds(plan.getId())));
            final BeanStore herbeStore = new BeanStore(herbeSupplier);
            final MapLayer herbeLayer = MapBuilder.createFeatureLayer(herbeStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(herbeStore.getNames().iterator().next())));
            herbeLayer.setName(LabelMapper.get(HerbaceeVegetation.class).mapClassName());
            herbeLayer.setStyle(createPolygonStyle(Color.ORANGE));
            herbeLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            vegetationGroup.items().add(0,herbeLayer);

            //arbres
            final StructBeanSupplier arbreSupplier = new StructBeanSupplier(ArbreVegetation.class,
                    () -> vs.getArbreRepo().getByParcelleIds(getParcelleIds(plan.getId())));
            final BeanStore arbreStore = new BeanStore(arbreSupplier);
            final MapLayer arbreLayer = MapBuilder.createFeatureLayer(arbreStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(arbreStore.getNames().iterator().next())));
            arbreLayer.setName(LabelMapper.get(ArbreVegetation.class).mapClassName());
            arbreLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            arbreLayer.setStyle(createArbreStyle());
            vegetationGroup.items().add(0,arbreLayer);

            //peuplements
            final StructBeanSupplier peuplementSupplier = new StructBeanSupplier(PeuplementVegetation.class,
                    () -> vs.getPeuplementRepo().getByParcelleIds(getParcelleIds(plan.getId())));
            final BeanStore peuplementStore = new BeanStore(peuplementSupplier);
            final org.geotoolkit.data.session.Session peuplementSession = peuplementStore.createSession(true);
            final MapItem peuplementGroup = MapBuilder.createItem();
            peuplementGroup.setName(LabelMapper.get(PeuplementVegetation.class).mapClassName());
            peuplementGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            vegetationGroup.items().add(0,peuplementGroup);
            //une couche pour chaque type
            final AbstractSIRSRepository<RefTypePeuplementVegetation> typePeuplementRepo = getSession().getRepositoryForClass(RefTypePeuplementVegetation.class);
            for(RefTypePeuplementVegetation ref : typePeuplementRepo.getAll()){
                final String id = ref.getId();
                final Filter filter = FF.equals(FF.property("typePeuplementId"),FF.literal(id));
                final FeatureCollection col = peuplementSession.getFeatureCollection(
                        QueryBuilder.all(peuplementStore.getNames().iterator().next()));
                final FeatureMapLayer layer = MapBuilder.createFeatureLayer(col);
                layer.setQuery(QueryBuilder.filtered(col.getFeatureType().getName(), filter));
                layer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
                final Color color;
                switch(id){
                    case "RefTypePeuplementVegetation:1" : color = new Color( 48, 138, 208); break;
                    case "RefTypePeuplementVegetation:2" : color = new Color(139, 187, 224); break;
                    case "RefTypePeuplementVegetation:3" : color = new Color( 18, 172, 159); break;
                    case "RefTypePeuplementVegetation:4" : color = new Color(139, 224, 217); break;
                    case "RefTypePeuplementVegetation:5" : color = new Color( 18, 172,  21); break;
                    case "RefTypePeuplementVegetation:6" : color = new Color(114, 205, 116); break;
                    case "RefTypePeuplementVegetation:7" : color = new Color( 53, 124, 55); break;
                    case "RefTypePeuplementVegetation:99": color = new Color(100, 200, 250); break;
                    default : color = RandomStyleBuilder.randomColor();
                }

                layer.setStyle(createPolygonStyle(color));
                layer.setName(ref.getLibelle());
                peuplementGroup.items().add(layer);
            }

            //invasives
            final StructBeanSupplier invasiveSupplier = new StructBeanSupplier(InvasiveVegetation.class,
                    () -> vs.getInvasiveRepo().getByParcelleIds(getParcelleIds(plan.getId())));
            final BeanStore invasiveStore = new BeanStore(invasiveSupplier);
            final org.geotoolkit.data.session.Session invasiveSession = invasiveStore.createSession(true);
            final MapItem invasivesGroup = MapBuilder.createItem();
            invasivesGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            invasivesGroup.setName(LabelMapper.get(InvasiveVegetation.class).mapClassName());
            vegetationGroup.items().add(0,invasivesGroup);
            //une couche pour chaque type
            final AbstractSIRSRepository<RefTypeInvasiveVegetation> typeInvasiveRepo = getSession().getRepositoryForClass(RefTypeInvasiveVegetation.class);
            for(RefTypeInvasiveVegetation ref : typeInvasiveRepo.getAll()){
                final String id = ref.getId();
                final Filter filter = FF.equals(FF.property("typeInvasive"),FF.literal(id));
                final FeatureCollection col = invasiveSession.getFeatureCollection(
                        QueryBuilder.filtered(invasiveStore.getNames().iterator().next(),filter));
                final FeatureMapLayer layer = MapBuilder.createFeatureLayer(col);
                layer.setQuery(QueryBuilder.filtered(col.getFeatureType().getName(), filter));
                layer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
                final Color color;
                switch(id){
                    case "RefTypeInvasiveVegetation:1" : color = new Color(189, 205, 147); break;
                    case "RefTypeInvasiveVegetation:2" : color = new Color(142, 184,  28); break;
                    case "RefTypeInvasiveVegetation:3" : color = new Color(243, 234, 158); break;
                    case "RefTypeInvasiveVegetation:4" : color = new Color(184, 167,  28); break;
                    case "RefTypeInvasiveVegetation:5" : color = new Color(217, 178, 137); break;
                    case "RefTypeInvasiveVegetation:6" : color = new Color(198, 107,   9); break;
                    case "RefTypeInvasiveVegetation:7" : color = new Color(227, 159, 148); break;
                    case "RefTypeInvasiveVegetation:8" : color = new Color(198,  35,   9); break;
                    case "RefTypeInvasiveVegetation:9" : color = new Color(143,  68,  56); break;
                    case "RefTypeInvasiveVegetation:99": color = new Color(250, 200, 100); break;
                    default : color = RandomStyleBuilder.randomColor();
                }

                layer.setStyle(createPolygonStyle(color));
                layer.setName(ref.getLibelle());
                invasivesGroup.items().add(layer);
            }

        }catch(Exception ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    private String[] getParcelleIds(String planId){
        final VegetationSession vs = VegetationSession.INSTANCE;
        final List<ParcelleVegetation> parcelles = vs.getParcelleRepo().getByPlanId(planId);
        final String[] parcelleIds = new String[parcelles.size()];
        for(int i=0;i<parcelleIds.length;i++){
            parcelleIds[i] = parcelles.get(i).getDocumentId();
        }
        return parcelleIds;
    }

    private static MutableStyle createParcelleStyle() throws IOException{
        final MutableStyle style = SF.style();
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        final MutableRule rule = SF.rule();
        style.featureTypeStyles().add(fts);
        fts.rules().add(rule);

        final BufferedImage img = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("fr/sirs/plugin/vegetation/style/parcelle.png"));
        final ExternalGraphic external = SF.externalGraphic(new ImageIcon(img),Collections.EMPTY_LIST);

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

    private static MutableStyle createParcelleStyleSelected() throws IOException{
        final MutableStyle style = SF.style();
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        final MutableRule rule = SF.rule();
        style.featureTypeStyles().add(fts);
        fts.rules().add(rule);

        final BufferedImage img = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("fr/sirs/plugin/vegetation/style/parcelleselect.png"));
        final ExternalGraphic external = SF.externalGraphic(new ImageIcon(img),Collections.EMPTY_LIST);

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
        final Stroke lineStroke = SF.stroke(Color.GREEN, 3);
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


    ////////////////////////////////////////////////////////////////////////////
    //
    //  UTILITY METHODS FOR THIS PLUGIN
    //
    ////////////////////////////////////////////////////////////////////////////


    /**
     * Méthode d'initialisation des comboboxes de sous-types de traitements
     * de manière à préserver la cohérence des choix qu'elles proposent en
     * fonction d'un choix de traitement.
     *
     * @param typeTraitementId
     * @param sousTypeTraitementId
     * @param sousTraitementPreviews
     * @param sousTraitements
     * @param comboBox
     */
    public static void initComboSousTraitement(final String typeTraitementId, final String sousTypeTraitementId,
            final List<Preview> sousTraitementPreviews,
            final Map<String, RefSousTraitementVegetation> sousTraitements, final ComboBox comboBox){


        // 1- si le type est null, on ne peut charger aucune liste de sous-types
        if(typeTraitementId == null){
            SIRS.initCombo(comboBox, FXCollections.emptyObservableList(),null);
        }
        // 2- sinon on va chercher ses éventuels sous-types
        else {
            Preview selectedPreview = null;
            final List<Preview> sousTypes = new ArrayList<>();
            for(final Preview sousType : sousTraitementPreviews){
                final String sousTypeId = sousType.getElementId();
                if(sousTypeId!=null){
                    final RefSousTraitementVegetation sousTraitement = sousTraitements.get(sousTypeId);
                    if(typeTraitementId.equals(sousTraitement.getTraitementId())){
                        sousTypes.add(sousType);
                    }

                    if(sousTypeId.equals(sousTypeTraitementId)) selectedPreview = sousType;
                }
            }
            SIRS.initCombo(comboBox, FXCollections.observableList(sousTypes), selectedPreview);
        }
    }

    /**
     * Calcule la fréquence de traitement de la parcelle en parcourant
     * la liste des zones de végétation afin d'examiner leurs deux traitements
     * associés (ponctuel et non ponctuel).
     *
     * NOTE : Cette méthode ignore les parcelles d'invasives.
     * 
     * Le traitement non ponctuel est associé à une certaine fréquence. La
     * fréquence de traitement de la parcelle est égale à la plus petite des
     * fréquences des traitements non ponctuels des zones de végétation de la
     * parcelle (pour celles des parcelles dont le traitement n'est pas
     * spécifié "hors-gestion").
     *
     * S'il n'y a pas de zone de végétation dans la parcelle, ou si aucune
     * d'entre elle n'a de traitement inclus dans la gestion, la fréquence de
     * traitement de la parcelle est fixée par convention à 0.
     *
     * @param parcelle
     * @return
     */
    public static int frequenceTraitementPlanifie(final ParcelleVegetation parcelle){

        // On initialise la plus courte fréquence à la durée du plan
        int plusCourteFrequence = 0;

        // On récupère toutes les fréquences de traitement de la parcelle
        final List<String> frequenceIds = new ArrayList<>();
        final ObservableList<? extends ZoneVegetation> zones = AbstractZoneVegetationRepository.getAllZoneVegetationByParcelleId(parcelle.getId(), Injector.getSession());
        for(final ZoneVegetation zone : zones){
            if(zone.getTraitement()!=null && !zone.getTraitement().getHorsGestion() && !(zone instanceof InvasiveVegetation)){
                final String frequenceId = zone.getTraitement().getFrequenceId();
                if(frequenceId!=null) frequenceIds.add(frequenceId);
            }
        }

        // Si on a récupéré des identifiants de fréquences, il faut obtenir les fréquences elles-mêmes !
        if(!frequenceIds.isEmpty()){
            final List<RefFrequenceTraitementVegetation> frequences = Injector.getSession().getRepositoryForClass(RefFrequenceTraitementVegetation.class).get(frequenceIds);
            for(final RefFrequenceTraitementVegetation frequence : frequences){
                final int freq = frequence.getFrequence();
                if(freq>0 && (freq<plusCourteFrequence || plusCourteFrequence==0)) plusCourteFrequence=freq;
            }
        }

        return plusCourteFrequence;
    }

    /**
     * Set planification values for auto-planification.
     *
     * NOTE : This method does not check if the parcelle planification mode is set to "auto".
     * You must check this condition before to call this method.
     *
     * NOTE : This method do not save process result. You must save the parcelle
     * to make the modification persistant.
     *
     * NOTE : Do nothing if initialIndex is negative.
     *
     * @param parcelle must not be null.
     * @param initialIndex Index correspondant à la date à partir de laquelle on
     * souhaite faire la mise à jour. En pratique cela corresponda à l'année en
     * cours de manière à ce que les modifications dans le plan n'aient pas d'
     * impact sur les planifications des années passées.
     * @param planDuration
     * @throws NullPointerException if parcelle is null
     */
    public static void resetAutoPlanif(final ParcelleVegetation parcelle, final int initialIndex, final int planDuration){

        // 1- on récupère la plus petite fréquence
        final int frequenceTraitement = PluginVegetation.frequenceTraitementPlanifie(parcelle);

        if(initialIndex>=0){
            /*
            On fait une nouvelle liste, de manière à ajouter d'un coup toutes les
            modifications et ne pas déclencher à contretemps les écouteurs des
            planifications sur le tableau de bord.
            */
            final List<Boolean> planifs = new ArrayList<>(parcelle.getPlanifications().subList(0, initialIndex));

            // 3- on réinitialise les planifications
            // a- Si on n'a pas de traitement sur zone de végétation, inclus dans la gestion, on ne planifie rien.
            if(frequenceTraitement==0){
                planifs.add(initialIndex, Boolean.TRUE);
                for(int i=initialIndex+1; i<planDuration; i++){
                    planifs.add(i, Boolean.FALSE);
                }
            }
            // b- Sinon, on initialise les planifications à la fréquence de traitement de la parcelle.
            else {
                for(int i=initialIndex; i<planDuration; i++){
                    planifs.add(i, (i-initialIndex)%frequenceTraitement==0);
                }
            }

            parcelle.setPlanifications(planifs);
        }
    }

    /**
     * Use resetAutoPlanif(ParcelleVegetation parcelle, int planDuration, int initialIndex) if you
     * already know planDuration.
     *
     * @param parcelle
     * @param initialIndex Index correspondant à la date à partir de laquelle on
     * souhaite faire la mise à jour. En pratique cela corresponda à l'année en
     * cours de manière à ce que les modifications dans le plan n'aient pas d'
     * impact sur les planifications des années passées.
     * 
     * @throws NullPointerException if parcelle is null
     * @throws IllegalStateException if:
     * 1) the planId of the parcelle is null,
     * 2) no repository is found for PlanVegetation class,
     * 3) no plan was found for the planId of the parcelle,
     * 4) plan duration is strictly negative.
     */
    public static void resetAutoPlanif(final ParcelleVegetation parcelle, final int initialIndex){
        final String planId = parcelle.getPlanId();

        if(planId==null) throw new IllegalStateException("planId must not be null");

        final AbstractSIRSRepository<PlanVegetation> planRepo = Injector.getSession().getRepositoryForClass(PlanVegetation.class);

        if(planRepo==null) throw new IllegalStateException("No repository found for "+PlanVegetation.class);

        final PlanVegetation plan = planRepo.get(planId);

        if(plan==null) throw new IllegalStateException("No plan found for id "+planId);

        final int planDuration = plan.getAnneeFin()-plan.getAnneeDebut();

        if(planDuration<0) throw new IllegalStateException("Plan duration must be positive.");

        resetAutoPlanif(parcelle, initialIndex, planDuration);
    }


    /**
     * Met à jour la parcelle de la zone de végétation donnée en paramètre
     * au cas où le traitement de celle-ci aurait été modifié, ce qui pourrait
     * avoir une incidence sur la planification automatique de la parcelle.
     *
     * @param changed
     */
    public static void updateParcelleAutoPlanif(final ZoneVegetation changed){


        /*
        Si la parcelle de la zone à laquelle le traitement se réfère est en mode
        de planification automatique, alors la modification du traitement peut
        avoir un effet sur les planfications de la parcelle.
        Il faut donc vérifier si la parcelle est en mode automatique et dans ce
        cas mettre à jour les planifications de la parcelle.

        NOTE (*) : il faut faire cette opération après avoir fait une première
        sauvegarde provisoire du traitement en cours de sauvegarde définitive,
        car sinon, lors du passage en planification automatique, la parcelle n'
        aura accès qu'à l'ancienne valeur du présent traitement. On la nouvelle
        valeur (en particulier avec la nouvelle valeur de fréquence du
        traitement non ponctuel peut être décisive pour le calcul des
        planifications automatiques
        */


        final String parcelleId = changed.getParcelleId();
        if(parcelleId!=null){
            final AbstractSIRSRepository<ParcelleVegetation> parcelleRepo = Injector.getSession().getRepositoryForClass(ParcelleVegetation.class);
            if(parcelleRepo!=null){
                final ParcelleVegetation parcelle=parcelleRepo.get(parcelleId);
                if(parcelle!=null){
                    if(parcelle.getModeAuto()){

                        //(*) opération préalable de sauvegarde du traitement.
                        final AbstractSIRSRepository repo = Injector.getSession().getRepositoryForClass(changed.getClass());
                        repo.update(changed);

                        // Calcul proprement dit de la planification automatique de la parcelle
                        final PlanVegetation plan = Injector.getSession().getRepositoryForClass(PlanVegetation.class).get(parcelle.getPlanId());
                        if(plan!=null){
                            PluginVegetation.resetAutoPlanif(parcelle, LocalDate.now().getYear()-plan.getAnneeDebut());
                        }
                        parcelleRepo.update(parcelle);// Il faut sauvegarder la parcelle car la méthode setAutoPlanifs ne s'en charge pas.
                    }
                }
            }
        }
    }

    /**
     * Gives the information if the parcelle is coherent.
     *
     * @param parcelle
     * @param frequence
     * @return
     * @deprecated : plus utilisé d'après la spec 0.3
     */
    @Deprecated
    public static boolean isCoherent(final ParcelleVegetation parcelle, final int frequence){

        /*
        La fréquence de traitement de la parcelle doit être positive.
        Si elle ne l'est pas (pour une raison inconnue), on enregistre l'erreur dans le log et on signale
        la parcelle incohérente.
        */
        if(frequence<0){
            SIRS.LOGGER.log(Level.WARNING, "La fréquence de la parcelle {0} est indiquée négative ("+frequence+"). Une fréquence de traitement doit être positive (ou nulle).", parcelle);
            return false;
        }

        /*
        D'autre part, si la fréquence est nulle, c'est qu'il n'y a pas de zone
        de végétation dans la parcelle ou qu'aucune d'elle n'a de traitement
        associé. On ne peut donc pas être incohérent dans ce cas et on renvoie
        tout de suite "vrai".
        */
        if(frequence==0) return true;

        /*
        Dans les autre cas, on a maintenant la plus courte fréquence de
        traitement touvée sur toutes les zones de la parcelle.

        Il faut d'autre part examiner les traitements qui ont eu lieu sur la
        parcelle.

        Si l'année courante moins l'année de l'un de ces traitements est
        inférieure à la fréquence la plus courte qui a été trouvée, c'est que le
        dernier traitement ayant eu lieu sur la parcelle remonte à moins
        longtemps que la fréquence minimale. On peut alors arrêter le parcours
        des traitements car la parcelle est a priori cohérente.

        Si a l'issue du parcours des traitements on n'a pas trouvé de traitement
        ayant eu lieu depuis un intervalle de temps inférieur à la fréquence
        minimale, il faut alors lancer une alerte.
        */
        final int anneeCourante = LocalDate.now().getYear();
        boolean coherent = false;// On part de l'a priori d'une parcelle incohérente.
        for(final TraitementParcelleVegetation traitement : parcelle.getTraitements()){
            if(traitement.getDate()!=null){
                final int anneeTraitement = traitement.getDate().getYear();
                if(anneeCourante-anneeTraitement<frequence){
                    coherent = true; break;
                }
            }
        }

        return coherent;
    }
    
    /**
     * Gives the information if the parcelle is coherent.
     * 
     * Cette version calcule la plus courte fréquence de traitement de la 
     * parcelle, ce qui nécessite plusieurs boucles et des appels à des dépôts 
     * de données.
     * 
     * Si la plus courte fréquence a déjà été utilisée dans le contexte d'appel 
     * de cette méthode et qu'elle est a priori toujours valide, préférer 
     * l'utilisation de:
     * 
     * isCoherent(ParcelleVegetation parcelle, int plusCourteFrequence)
     *
     * @param parcelle
     * @return
     * @deprecated : plus utilisé d'après la spec 0.3
     */
    @Deprecated
    public static boolean isCoherent(final ParcelleVegetation parcelle){
        return isCoherent(parcelle, frequenceTraitementPlanifie(parcelle));
    }

    /**
     *
     * @param parcelle the parcelle. Must not be null.
     * @return the date of the last traitement. Null if no traitement was done yet.
     */
    public static LocalDate dernierTraitement(final ParcelleVegetation parcelle){
        LocalDate result = null;
        for(final TraitementParcelleVegetation traitement : parcelle.getTraitements()){
            if(traitement.getDate()!=null){
                if(result==null || traitement.getDate().isAfter(result)){
                    result = traitement.getDate();
                }
            }
        }
        return result;
    }

    /**
     * Initialisation des planifications : ajustement de la taille de la liste
     * (doit être appelée à la création d'une parcelle,
     * soit à partir de la carte, soit à partir du tableau des parcelles,
     * soit lors de la duplication d'un plan).
     * 
     * @param parcelle
     * @param dureePlan
     */
    public static void initPlanifs(final ParcelleVegetation parcelle, final int dureePlan){
        final ObservableList<Boolean> planifications = parcelle.getPlanifications();
        if(planifications.size()<dureePlan){
            while(planifications.size()<dureePlan) planifications.add(Boolean.FALSE);
        } else{
            while(planifications.size()>dureePlan) planifications.remove(planifications.size()-1);
        }
    }
    
    public static List<Class<? extends ZoneVegetation>> zoneVegetationClasses(){
        final List<Class<? extends ZoneVegetation>> vegetationClasses = new ArrayList<>();
        for(final Class<? extends Element> eltClass : Session.getElements()){
            if(ZoneVegetation.class.isAssignableFrom(eltClass))vegetationClasses.add((Class<? extends ZoneVegetation>) eltClass);
        }
        return vegetationClasses;
    }

    /**
     * Update parcelle planifications of a plan.
     *
     * @param plan
     * @param beginShift
     */
    public static void updatePlanifs(final PlanVegetation plan, final int beginShift){
        final ParcelleVegetationRepository parcelleRepo = (ParcelleVegetationRepository) Injector.getSession().getRepositoryForClass(ParcelleVegetation.class);

        final int duration = plan.getAnneeFin()-plan.getAnneeDebut();
        final List<ParcelleVegetation> parcelles = parcelleRepo.getByPlan(plan);

        for(final ParcelleVegetation parcelle : parcelles){
            final List<Boolean> planifs = parcelle.getPlanifications();

            // Si on est en mode automatique, il faut recalculer les planifications [NE PLUS FAIRE POUR L'AUTO PLANIF DE LA SPEC 0.3]
//            if(parcelle.getModeAuto()){
//               PluginVegetation.resetAutoPlanif(parcelle, LocalDate.now().getYear()-plan.getAnneeDebut(), duration);
//            }

            // Si on n'est pas en mode automatique, il faut décaler les planifications déjà construites.
//            else{
                /*==========================================================
                Il faut commencer par décaler les planifications du nombre
                d'années dont on a décalé le début, afin que les
                planifications déjà programmées soient conservées et qu'on
                ne soit pas obligé de tout refaire.
                */

                /*
                Si la nouvelle date est antérieure, il faut ajouter des
                planifications avant.
                */
                if(beginShift<0){
                    for(int i=0; i<(-beginShift); i++){
                        planifs.remove(0);
                    }
                }

                /*
                Si la nouvelle date est postérieure, il faut retrancher autant
                de planifications du début qu'il y a d'années de décalage, dans
                la limite de la taille de la liste des planifications
                */
                else if(beginShift>0){
                    for(int i=0; i<beginShift && i<planifs.size(); i++){
                        planifs.add(0, Boolean.FALSE);
                    }
                }

                /*==========================================================
                En dernier lieu, on ajuste la taille des listes de
                planifications en ajoutant les planifications manquantes et
                en retirant les planifications qui dépassent la date du plan.
                */

                // S'il n'y a pas assez d'éléments, il faut en rajouter
                while(planifs.size()<duration){
                    planifs.add(Boolean.FALSE);
                }

                // S'il y en a trop il faut en enlever
                while(planifs.size()>duration){
                    planifs.remove(duration);
                }
//            }
        }

        // Une fois toutes les planifications mises à jour, on
        // sauvegarde les parcelles du plan.
        parcelleRepo.executeBulk(parcelles);
    }

    /**
     * Parametrization of the traitement of a vegetation zone.
     * 
     * @param <T>
     * @param zoneType
     * @param peuplement
     * @param typeVegetationId
     */
    public static <T extends ZoneVegetation> void paramTraitement(final Class<T> zoneType, final T peuplement, final String typeVegetationId){

        // 1- Récupération de la parcelle :
        final AbstractSIRSRepository<ParcelleVegetation> parcelleRepo = Injector.getSession().getRepositoryForClass(ParcelleVegetation.class);
        if(parcelleRepo!=null){
            final ParcelleVegetation parcelle = parcelleRepo.get(peuplement.getParcelleId());
            if(parcelle!=null && parcelle.getPlanId()!=null){

                // 2- Récupération du plan
                final AbstractSIRSRepository<PlanVegetation> planRepo = Injector.getSession().getRepositoryForClass(PlanVegetation.class);
                if(planRepo!=null){
                    final PlanVegetation plan = planRepo.get(parcelle.getPlanId());

                    if(plan!=null){

                        // 3- Récupération des paramétrages de fréquences
                        final ObservableList<ParamFrequenceTraitementVegetation> params = plan.getParamFrequence();
                        boolean ponctuelSet=false, nonPonctuelSet=false;
                        for(final ParamFrequenceTraitementVegetation param : params){

                            // On ne s'intéresse qu'aux paramètres relatifs au type de zone concerné.
                            if(param.getType().equals(zoneType) && typeVegetationId.equals(param.getTypeVegetationId())){

                                if(param.getPonctuel()){
                                    peuplement.getTraitement().setTraitementPonctuelId(param.getTraitementId());
                                    peuplement.getTraitement().setSousTraitementPonctuelId(param.getSousTraitementId());
                                    ponctuelSet=true;
                                }
                                else{
                                    peuplement.getTraitement().setTraitementId(param.getTraitementId());
                                    peuplement.getTraitement().setSousTraitementId(param.getSousTraitementId());
                                    peuplement.getTraitement().setFrequenceId(param.getFrequenceId());
                                    nonPonctuelSet=true;
                                }
                            }

                            // Lorsque les deux traitements ponctuel et non ponctuel ont été initialisés, on peut arrêter l'examen des paramètres.
                            if(ponctuelSet && nonPonctuelSet) break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Détermine le côté du tronçon où se trouve la zone de végétation.
     *
     * @param troncon
     * @param zone
     * @return
     */
    public static double computeRatio(final TronconDigue troncon, final ZoneVegetation zone){

        final String typeRiveId = troncon.getTypeRiveId();
        final String typeCoteId = zone.getTypeCoteId();
        
        double ratio = 1.0;

        if("RefRive:1".equals(typeRiveId)){
            //rive gauche
            ratio *= -1.0;
        }

        switch (typeCoteId==null ? "" : typeCoteId) {
            //coté eau
            case "RefCote:1": //riviere
            case "RefCote:3": //etang
            case "RefCote:4": //mer
                ratio *= 1.0;
                break;
            //coté terre
            case "RefCote:2": //terre
            case "RefCote:6": //crete
            case "RefCote:99"://indéfini
            default :
                //Terre, Crete
                ratio *= -1.0;
                break;
            case "RefCote:5": //2 coté
                ratio = 0.0;
                break;
        }
        return ratio;
    }

    /**
     * Builds an area from a linear.
     *
     *        ||
     *        ||
     *        ||
     *        ||----------->   --------------(startFar)
     *        ||---->          --------------(startNear)
     *        ||     *******
     *        ||     *\\\\\*
     *        ||     *\\\\\*
     *        \\    *\\\\\\\*
     *         \\    *\\\\\\\*
     *          \\    *\\\\\\\*     (SURFACE)
     *           \\   *\\\\\\\\\*
     *            ||   *\\\\\\\\\*
     *            ||   *\\\\\\\\\*
     *            ||  *\\\\\\\\\\\*
     *            ||  *\\\\\\\\\\\*
     *            ||  *************
     *            ||->                  --------(endNear)
     *            ||-------------->     --------(endFar)
     *            ||
     *            ||
     *
     *
     *
     *
     *
     * Utilisé dans le plugin vegetation.
     *
     * @param linear
     * @param startNear
     * @param startFar
     * @param endNear
     * @param endFar
     * @return
     */
    public static Polygon toPolygon(final LineString linear, final double startNear,
            final double startFar, final double endNear, final double endFar) {

        final PathIterator ite = new JTSLineIterator(linear, null);
        final DoublePathWalker walker = new DoublePathWalker(ite);
        final Point2D.Double pt = new Point2D.Double();
        final double totalLength = linear.getLength();
        final Coordinate c0 = new Coordinate(0,0);
        final Coordinate c1 = new Coordinate(0,0);
        final List<Coordinate> coords = new ArrayList<>();

        double distance = 0;
        double distNear = startNear;
        double distFar = startFar;

        //premiers points
        walker.walk(0);
        walker.getPosition(pt);
        double angle = Math.PI/2 + walker.getRotation();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        c0.x = pt.x + cos*distNear;
        c0.y = pt.y + sin*distNear;
        c1.x = pt.x + cos*distFar;
        c1.y = pt.y + sin*distFar;
        coords.add(0,new Coordinate(c0));
        coords.add(new Coordinate(c1));


        while(!walker.isFinished()){
            final double d = walker.getSegmentLengthRemaining();
            distance += d;
            walker.walk(d+0.0001f);
            walker.getPosition(pt);
            angle = Math.PI/2 + walker.getRotation();
            cos = Math.cos(angle);
            sin = Math.sin(angle);

            distNear = startNear + (endNear-startNear)*(distance/totalLength);
            distFar = startFar + (endFar-startFar)*(distance/totalLength);

            c0.x = pt.x + cos*distNear;
            c0.y = pt.y + sin*distNear;
            c1.x = pt.x + cos*distFar;
            c1.y = pt.y + sin*distFar;
            coords.add(0,new Coordinate(c0));
            coords.add(new Coordinate(c1));
        }

        //on ferme le polygone
        coords.add(new Coordinate(coords.get(0)));
        while(coords.size()<4){
            coords.add(new Coordinate(coords.get(0)));
        }

        final Polygon polygon = GO2Utilities.JTS_FACTORY.createPolygon(coords.toArray(new Coordinate[0]));
        polygon.setSRID(linear.getSRID());
        polygon.setUserData(linear.getUserData());
        return polygon;
    }

    /**
     * Calcule les coordonnées d'un point à partir d'un segment de référence,
     * d'une distance au point de départ du segment et d'une distance au segment
     * lui-même.
     *
     *
     *                         X Point computed
     *                         |
     *                         | perpendicular distance
     *        linear distance  |
     *     x---------------------------------------------x
     *     start             linear (segment)           end
     *
     * @param segment Te reference segment
     * @param perpendicularDistance
     * @param linearDistance
     * @return
     */
    public static Point toPoint(final LineString segment, final double perpendicularDistance, final double linearDistance) {

        final double segmentLength = segment.getLength();
        /*
        La suite calcule les coordonnées du point définitif à la distance
        "perpendicularDistance" du segment.
         */
        final PathIterator ite = new JTSLineIterator(segment, null);
        final DoublePathWalker walker = new DoublePathWalker(ite);
        final Point2D.Double pt = new Point2D.Double();
        final Coordinate coord = new Coordinate(0,0);

        /*
        On va jusqu'au point intermédiaire, on récupère la position et la
        direction perpendiculaire.
        */
//        final float d = walker.getSegmentLengthRemaining();
        final double distanceToWalk;
        // On sécurise la distance à parcourir pour que celle-ci soit bien incluse dans le segment.
        if(linearDistance>segmentLength) distanceToWalk=segmentLength;
        else if(linearDistance<0.) distanceToWalk=0.;
        else distanceToWalk = linearDistance;

        walker.walk(distanceToWalk);
        walker.getPosition(pt);
        double angle = Math.PI/2 + walker.getRotation();

        coord.x = pt.x + Math.cos(angle)*perpendicularDistance;
        coord.y = pt.y + Math.sin(angle)*perpendicularDistance;

        /**
         * Les quatre points retournés par "toPolygon()" sont identiques. On prend le premier.
         */
        final Point polygon = GO2Utilities.JTS_FACTORY.createPoint(coord);
        polygon.setSRID(segment.getSRID());
        polygon.setUserData(segment.getUserData());
        return polygon;
    }
}
