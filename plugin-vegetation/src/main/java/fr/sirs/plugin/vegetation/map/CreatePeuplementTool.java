
package fr.sirs.plugin.vegetation.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import fr.sirs.Injector;
import static fr.sirs.SIRS.CSS_PATH;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.ArbreVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import fr.sirs.util.ResourceInternationalString;
import fr.sirs.util.SirsStringConverter;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import javafx.scene.layout.VBox;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display.VisitFilter;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionTool;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionToolSpi;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.edition.EditionTool;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.internal.Loggers;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.opengis.util.FactoryException;

/**
 *
 * @author Johann Sorel
 */
public class CreatePeuplementTool extends AbstractEditionTool{

    public static final Spi SPI = new Spi();
    public static final class Spi extends AbstractEditionToolSpi{

        public Spi() {
            super("CreatePeuplement",
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle",
                        "fr.sirs.plugin.vegetation.map.CreatePeuplementTool.title",CreatePeuplementTool.class.getClassLoader()),
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle",
                        "fr.sirs.plugin.vegetation.map.CreatePeuplementTool.abstract",CreatePeuplementTool.class.getClassLoader()),
                new Image("fr/sirs/plugin/vegetation/peuplement.png"));
        }

        @Override
        public boolean canHandle(Object candidate) {
            return true;
        }

        @Override
        public EditionTool create(FXMap map, Object layer) {
            return new CreatePeuplementTool(map);
        }
    };


    //session and repo
    private final Session session;
    private final AbstractSIRSRepository<ParcelleVegetation> parcelleRepo;

    private final MouseListen mouseInputListener = new MouseListen();
    private final BorderPane wizard = new BorderPane();

    private ArbreVegetation arbre = new ArbreVegetation();
    private ParcelleVegetation parcelle = null;
    private final Label lblParcelle = new Label();
    private final Label lblGeom = new Label();

    private FeatureMapLayer parcelleLayer = null;

    //geometry en cours
    private EditionHelper helper;
    private final FXGeometryLayer geomLayer = new FXGeometryLayer();
    private Polygon geometry = null;
    private final List<Coordinate> coords = new ArrayList<Coordinate>();
    private boolean justCreated = false;
    

    public CreatePeuplementTool(FXMap map) {
        super(SPI);
        wizard.getStylesheets().add(CSS_PATH);

        session = Injector.getSession();
        parcelleRepo = session.getRepositoryForClass(ParcelleVegetation.class);

        final Label lbl1 = new Label("Parcelle :");
        final Label lbl2 = new Label("Géométrie :");
        lbl1.getStyleClass().add("label-header");
        lbl2.getStyleClass().add("label-header");
        wizard.getStyleClass().add("blue-light");
        lblParcelle.getStyleClass().add("label-text");
        lblGeom.getStyleClass().add("label-text");

        final VBox vbox = new VBox(15,
                lbl1,
                lblParcelle,
                lbl2,
                lblGeom);
        vbox.setMaxSize(USE_PREF_SIZE,USE_PREF_SIZE);
        wizard.setCenter(vbox);
    }

    private void reset(){
        arbre = new ArbreVegetation();
        parcelle = null;
        lblParcelle.setText("Sélectionner une parcelle sur la carte");
        lblGeom.setText("");
        if(parcelleLayer!=null) parcelleLayer.setSelectionFilter(null);

        geometry = null;
        coords.clear();
        justCreated = false;
        geomLayer.getGeometries().clear();
    }

    @Override
    public Node getConfigurationPane() {
        return wizard;
    }

    @Override
    public Node getHelpPane() {
        return null;
    }

    @Override
    public void install(FXMap component) {
        reset();
        super.install(component);
        component.addEventHandler(MouseEvent.ANY, mouseInputListener);
        component.addEventHandler(ScrollEvent.ANY, mouseInputListener);

        //on rend les couches troncon et borne selectionnables
        final MapContext context = component.getContainer().getContext();
        for(MapLayer layer : context.layers()){
            layer.setSelectable(false);
            if(layer.getName().equalsIgnoreCase(PluginVegetation.PARCELLE_LAYER_NAME)){
                parcelleLayer = (FeatureMapLayer) layer;
            }
        }

        helper = new EditionHelper(map, parcelleLayer);
        component.setCursor(Cursor.CROSSHAIR);
        component.addDecoration(geomLayer);
    }

    @Override
    public boolean uninstall(FXMap component) {
        super.uninstall(component);
        component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
        component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
        component.setCursor(Cursor.DEFAULT);
        component.removeDecoration(geomLayer);
        reset();
        return true;
    }

    private class MouseListen extends FXPanMouseListen {

        private final SirsStringConverter cvt = new SirsStringConverter();

        public MouseListen() {
            super(CreatePeuplementTool.this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {

            final Rectangle2D clickArea = new Rectangle2D.Double(e.getX()-2, e.getY()-2, 4, 4);

            if(parcelle==null){
                parcelleLayer.setSelectable(true);
                //recherche une parcelle sous la souris
                map.getCanvas().getGraphicsIn(clickArea, new AbstractGraphicVisitor() {
                    @Override
                    public void visit(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D area) {
                        final Object bean = graphic.getCandidate().getUserData().get(BeanFeature.KEY_BEAN);
                        if(bean instanceof ParcelleVegetation){
                            //on recupere l'object complet
                            parcelle = (ParcelleVegetation) bean;
                            //on recupere l'object complet
                            parcelle = parcelleRepo.get(parcelle.getDocumentId());
                            lblParcelle.setText(cvt.toString(parcelle));
                            lblGeom.setText("Cliquer sur la carte pour créer la géométrie");
                            parcelleLayer.setSelectionFilter(GO2Utilities.FILTER_FACTORY.id(Collections.singleton(graphic.getCandidate().getIdentifier())));

                            final Geometry constraint = parcelle.getGeometry().buffer(1000000,10,BufferParameters.CAP_FLAT);
                            try {
                                JTS.setCRS(constraint, JTS.findCoordinateReferenceSystem(parcelle.getGeometry()));
                            } catch (FactoryException ex) {
                                Loggers.JAVAFX.log(Level.WARNING, ex.getMessage(), ex);
                            }

                            helper.setConstraint(constraint);
                        }
                    }
                    @Override
                    public boolean isStopRequested() {
                        return parcelle!=null;
                    }
                    @Override
                    public void visit(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D area) {}
                }, VisitFilter.INTERSECTS);
            }else if(parcelle!=null){

                final double x = getMouseX(e);
                final double y = getMouseY(e);
                mousebutton = e.getButton();

                if(mousebutton == MouseButton.PRIMARY){

                    if(justCreated){
                        justCreated = false;
                        //we must modify the second point since two point where added at the start
                        coords.remove(2);
                        coords.remove(1);
                        coords.add(helper.toCoord(x,y));
                        coords.add(helper.toCoord(x,y));

                    }else if(coords.isEmpty()){
                        justCreated = true;
                        //this is the first point of the geometry we create
                        //add 3 points that will be used when moving the mouse around
                        coords.add(helper.toCoord(x,y));
                        coords.add(helper.toCoord(x,y));
                        coords.add(helper.toCoord(x,y));
                    }else{
                        justCreated = false;
                        coords.add(helper.toCoord(x,y));
                    }

                    geometry = EditionHelper.createPolygon(coords);
                    JTS.setCRS(geometry, map.getCanvas().getObjectiveCRS2D());
                    geomLayer.getGeometries().setAll(geometry);

                }else if(mousebutton == MouseButton.SECONDARY){

                    justCreated = false;
                    helper.sourceAddGeometry(geometry);
                    reset();
                    geomLayer.getGeometries().clear();
                    coords.clear();
                }

            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if(coords.size() > 2){
                final double x = getMouseX(e);
                final double y = getMouseY(e);
                if(justCreated){
                    coords.remove(coords.size()-1);
                    coords.remove(coords.size()-1);
                    coords.add(helper.toCoord(x,y));
                    coords.add(helper.toCoord(x,y));
                }else{
                    coords.remove(coords.size()-1);
                    coords.add(helper.toCoord(x,y));
                }
                geometry = EditionHelper.createPolygon(coords);
                JTS.setCRS(geometry, map.getCanvas().getObjectiveCRS2D());
                geomLayer.getGeometries().setAll(geometry);
                return;
            }
            super.mouseMoved(e);
        }

    }

}
