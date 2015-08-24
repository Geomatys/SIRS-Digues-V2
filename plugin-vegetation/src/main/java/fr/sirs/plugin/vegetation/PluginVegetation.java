package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.StructBeanSupplier;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.AbstractZoneVegetationRepository;
import fr.sirs.core.model.ArbreVegetation;
import fr.sirs.core.model.HerbaceeVegetation;
import fr.sirs.core.model.InvasiveVegetation;
import fr.sirs.core.model.ParcelleTraitementVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefFrequenceTraitementVegetation;
import fr.sirs.core.model.RefSousTraitementVegetation;
import fr.sirs.core.model.RefTypeInvasiveVegetation;
import fr.sirs.core.model.RefTypePeuplementVegetation;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.core.model.sql.SQLHelper;
import fr.sirs.core.model.sql.VegetationSqlHelper;
import fr.sirs.map.FXMapPane;
import fr.sirs.plugin.vegetation.map.CreateParcelleTool;
import java.awt.Color;
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

    public static final String PARCELLE_LAYER_NAME = "Parcelles";
    public static final String VEGETATION_GROUP_NAME = "Végétation";

    private static final String NAME = "plugin-vegetation";
    private static final String TITLE = "Module végétation";
    private static final MutableStyleFactory SF = GO2Utilities.STYLE_FACTORY;
    private static final FilterFactory2 FF = GO2Utilities.FILTER_FACTORY;

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
        final Session session = Injector.getSession();

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
            herbeLayer.setName("Strates herbacée");
            herbeLayer.setStyle(createPolygonStyle(Color.ORANGE));
            herbeLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            vegetationGroup.items().add(0,herbeLayer);

            //arbres
            final StructBeanSupplier arbreSupplier = new StructBeanSupplier(ArbreVegetation.class,
                    () -> vs.getArbreRepo().getByParcelleIds(getParcelleIds(plan.getId())));
            final BeanStore arbreStore = new BeanStore(arbreSupplier);
            final MapLayer arbreLayer = MapBuilder.createFeatureLayer(arbreStore.createSession(true)
                    .getFeatureCollection(QueryBuilder.all(arbreStore.getNames().iterator().next())));
            arbreLayer.setName("Arbres exceptionnels");
            arbreLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            arbreLayer.setStyle(createArbreStyle());
            vegetationGroup.items().add(0,arbreLayer);

            //peuplements
            final StructBeanSupplier peuplementSupplier = new StructBeanSupplier(PeuplementVegetation.class,
                    () -> vs.getPeuplementRepo().getByParcelleIds(getParcelleIds(plan.getId())));
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
                final Filter filter = FF.equals(FF.property("typePeuplementId"),FF.literal(id));
                final FeatureCollection col = peuplementSession.getFeatureCollection(
                        QueryBuilder.all(peuplementStore.getNames().iterator().next()));
                final FeatureMapLayer layer = MapBuilder.createFeatureLayer(col);
                layer.setQuery(QueryBuilder.filtered(col.getFeatureType().getName(), filter));
                layer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
                final Color color;
                switch(id){
                    case "RefTypePeuplementVegetation:1" : color = new Color(  0, 200,   0); break;
                    case "RefTypePeuplementVegetation:2" : color = new Color(200, 200,   0); break;
                    case "RefTypePeuplementVegetation:3" : color = new Color(  0, 200, 200); break;
                    case "RefTypePeuplementVegetation:4" : color = new Color(200, 200, 100); break;
                    case "RefTypePeuplementVegetation:5" : color = new Color(  0, 150,   0); break;
                    case "RefTypePeuplementVegetation:6" : color = new Color(150, 200,   0); break;
                    case "RefTypePeuplementVegetation:7" : color = new Color(100, 250, 100); break;
                    case "RefTypePeuplementVegetation:99": color = new Color(200, 200, 200); break;
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
            invasivesGroup.setName("Invasives");
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
                    case "RefTypeInvasiveVegetation:1" : color = new Color(  0, 200,   0); break;
                    case "RefTypeInvasiveVegetation:2" : color = new Color(200, 200,   0); break;
                    case "RefTypeInvasiveVegetation:3" : color = new Color(  0, 200, 200); break;
                    case "RefTypeInvasiveVegetation:4" : color = new Color(200, 200, 100); break;
                    case "RefTypeInvasiveVegetation:5" : color = new Color(  0, 150,   0); break;
                    case "RefTypeInvasiveVegetation:6" : color = new Color(150, 200,   0); break;
                    case "RefTypeInvasiveVegetation:7" : color = new Color(100, 250, 100); break;
                    case "RefTypeInvasiveVegetation:8" : color = new Color( 50, 250, 100); break;
                    case "RefTypeInvasiveVegetation:9" : color = new Color(100, 250,  50); break;
                    case "RefTypeInvasiveVegetation:99": color = new Color(200, 200, 200); break;
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
    public static int frequenceTraitement(final ParcelleVegetation parcelle){

        // On initialise la plus courte fréquence à la durée du plan
        int plusCourteFrequence = 0;

        // On récupère toutes les fréquences de traitement de la parcelle
        final List<String> frequenceIds = new ArrayList<>();
        final ObservableList<? extends ZoneVegetation> zones = AbstractZoneVegetationRepository.getAllZoneVegetationByParcelleId(parcelle.getId(), Injector.getSession());
        for(final ZoneVegetation zone : zones){
            if(zone.getTraitement()!=null && !zone.getTraitement().getHorsGestion()){
                final String frequenceId = zone.getTraitement().getFrequenceId();
                if(frequenceId!=null) frequenceIds.add(frequenceId);
            }
        }

        // Si on a récupéré des identifiants de fréquences, il faut obtenir les fréquences elles-mêmes !
        if(!frequenceIds.isEmpty()){
            final List<RefFrequenceTraitementVegetation> frequences = Injector.getSession().getRepositoryForClass(RefFrequenceTraitementVegetation.class).get(frequenceIds);
            for(final RefFrequenceTraitementVegetation frequence : frequences){
                final int f = frequence.getFrequence();
                if(f>0 && (f<plusCourteFrequence || plusCourteFrequence==0)) plusCourteFrequence=f;
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
     * @param parcelle must not be null.
     * @param planDuration
     * @throws NullPointerException if parcelle is null
     */
    public static void resetAutoPlanif(final ParcelleVegetation parcelle, final int planDuration){
        final List<Boolean> planifs = parcelle.getPlanifications();

        // 1- on récupère la plus petite fréquence
        final int frequenceTraitement = PluginVegetation.frequenceTraitement(parcelle);
        // 2- on retire toutes les anciennes planifications
        while(planifs.size()>0){
            planifs.remove(0);
        }
        // 3- on réinitialise les planifications
        // a- Si on n'a pas de traitement sur zone de végétation, inclus dans la gestion, on ne planifie rien.
        if(frequenceTraitement==0){
            for(int i=0; i<planDuration; i++){
                planifs.add(i, Boolean.FALSE);
            }
        }
        // b- Sinon, on initialise les planifications à la fréquence de traitement de la parcelle.
        else {
            for(int i=0; i<planDuration; i++){
                planifs.add(i, i%frequenceTraitement==0);
            }
        }
    }

    /**
     * Use setAutoPlanifs(ParcelleVegetation parcelle, int planDuration) if you
     * already know planDuration.
     *
     * @param parcelle
     * @throws NullPointerException if parcelle is null
     * @throws IllegalStateException if:
     * 1) the planId of the parcelle is null,
     * 2) no repository is found for PlanVegetation class,
     * 3) no plan was found for the planId of the parcelle,
     * 4) plan duration is strictly negative.
     */
    public static void resetAutoPlanif(final ParcelleVegetation parcelle){
        final String planId = parcelle.getPlanId();

        if(planId==null) throw new IllegalStateException("planId must not be null");

        final AbstractSIRSRepository<PlanVegetation> planRepo = Injector.getSession().getRepositoryForClass(PlanVegetation.class);

        if(planRepo==null) throw new IllegalStateException("No repository found for "+PlanVegetation.class);

        final PlanVegetation plan = planRepo.get(planId);

        if(plan==null) throw new IllegalStateException("No plan found for id "+planId);

        final int planDuration = plan.getAnneeFin()-plan.getAnneeDebut();

        if(planDuration<0) throw new IllegalStateException("Plan duration must be positive.");
        
        resetAutoPlanif(parcelle, planDuration);
    }


    /**
     * Gives the information if the parcelle is coherent.
     *
     * @param parcelle
     * @param plusCourteFrequence
     * @return
     */
    public static boolean isCoherent(final ParcelleVegetation parcelle, final int plusCourteFrequence){

        /*
        La fréquence de traitement de la parcelle doit être positive.
        Si elle ne l'est pas (pour une raison inconnue), on enregistre l'erreur dans le log et on signale
        la parcelle incohérente.
        */
        if(plusCourteFrequence<0){
            SIRS.LOGGER.log(Level.WARNING, "La fréquence de la parcelle {0} est indiquée négative ("+plusCourteFrequence+"). Une fréquence de traitement doit être positive (ou nulle).", parcelle);
            return false;
        }

        /*
        D'autre part, si la fréquence est nulle, c'est qu'il n'y a pas de zone
        de végétation dans la parcelle ou qu'aucune d'elle n'a de traitement
        associé. On ne peut donc pas être incohérent dans ce cas et on renvoie
        tout de suite "vrai".
        */
        if(plusCourteFrequence==0) return true;

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
        for(final ParcelleTraitementVegetation traitement : parcelle.getTraitements()){
            if(traitement.getDate()!=null){
                final int anneeTraitement = traitement.getDate().getYear();
                if(anneeCourante-anneeTraitement<plusCourteFrequence){
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
     */
    public static boolean isCoherent(final ParcelleVegetation parcelle){
        return isCoherent(parcelle, frequenceTraitement(parcelle));
    }

    /**
     *
     * @param parcelle the parcelle. Must not be null.
     * @return the date of the last traitement. Null if no traitement was done yet.
     */
    public static LocalDate dernierTraitement(final ParcelleVegetation parcelle){
        LocalDate result = null;
        for(final ParcelleTraitementVegetation traitement : parcelle.getTraitements()){
            if(traitement.getDate()!=null){
                if(result==null || traitement.getDate().isAfter(result)){
                    result = traitement.getDate();
                }
            }
        }
        return result;
    }
    
}
