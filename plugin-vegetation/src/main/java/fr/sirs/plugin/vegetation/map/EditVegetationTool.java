
package fr.sirs.plugin.vegetation.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.CSS_PATH;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.ArbreVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import fr.sirs.util.ResourceInternationalString;
import fr.sirs.util.SirsStringConverter;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
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
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionTool;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionToolSpi;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.edition.EditionTool;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Johann Sorel
 */
public class EditVegetationTool extends AbstractEditionTool{

    public static final Spi SPI = new Spi();
    public static final class Spi extends AbstractEditionToolSpi{

        public Spi() {
            super("CreatePeuplement",
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle", 
                        "fr.sirs.plugin.vegetation.map.CreateArbreTool.title",EditVegetationTool.class.getClassLoader()),
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle", 
                        "fr.sirs.plugin.vegetation.map.CreateArbreTool.abstract",EditVegetationTool.class.getClassLoader()),
                SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_PENCIL,24,new Color(15,112,183)),null)
            );
        }

        @Override
        public boolean canHandle(Object candidate) {
            return true;
        }

        @Override
        public EditionTool create(FXMap map, Object layer) {
            return new EditVegetationTool(map);
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
    private final Label lblPoint = new Label();

    private MapLayer parcelleLayer = null;

    public EditVegetationTool(FXMap map) {
        super(SPI);
        wizard.getStylesheets().add(CSS_PATH);

        session = Injector.getSession();
        parcelleRepo = session.getRepositoryForClass(ParcelleVegetation.class);

        final VBox vbox = new VBox(10,
                new Label("Parcelle :"),
                lblParcelle,
                new Label("Position :"),
                lblPoint);
        vbox.setMaxSize(USE_PREF_SIZE,USE_PREF_SIZE);
        wizard.setCenter(vbox);
    }

    private void reset(){
        arbre = new ArbreVegetation();
        parcelle = null;
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

        //on rend les couches troncon et borne selectionnables
        final MapContext context = component.getContainer().getContext();
        for(MapLayer layer : context.layers()){
            layer.setSelectable(false);
            if(layer.getName().equalsIgnoreCase(PluginVegetation.PARCELLE_LAYER_NAME)){
                parcelleLayer = layer;
            }
        }
        component.setCursor(Cursor.CROSSHAIR);
    }

    @Override
    public boolean uninstall(FXMap component) {
        super.uninstall(component);
        component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
        component.setCursor(Cursor.DEFAULT);
        reset();
        return true;
    }

    private class MouseListen extends FXPanMouseListen {

        private final SirsStringConverter cvt = new SirsStringConverter();

        public MouseListen() {
            super(EditVegetationTool.this);
        }

        @Override
        public void mouseClicked(MouseEvent event) {

            final Rectangle2D clickArea = new Rectangle2D.Double(event.getX()-2, event.getY()-2, 4, 4);

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
                parcelleLayer.setSelectable(false);

                //on crée l'arbre
                final EditionHelper helper = new EditionHelper(map, null);
                final Coordinate coord = helper.toCoord(event.getX(), event.getY());
                Geometry pt = GO2Utilities.JTS_FACTORY.createPoint(coord);
                JTS.setCRS(pt, map.getCanvas().getObjectiveCRS2D());
                try {
                    //on s'assure d'etre dans le crs de la base
                    pt = JTS.transform(pt, CRS.findMathTransform(JTS.findCoordinateReferenceSystem(pt),session.getProjection()));
                } catch (FactoryException | TransformException ex) {
                    SIRS.LOGGER.log(Level.WARNING,ex.getMessage(),ex);
                }
                arbre.setExplicitGeometry(pt);
                arbre.setGeometry(pt);
                arbre.setValid(true);
                arbre.setForeignParentId(parcelle.getDocumentId());


                final AbstractSIRSRepository<ArbreVegetation> arbreRepo = session.getRepositoryForClass(ArbreVegetation.class);
                arbreRepo.add(arbre);
                final ParcelleVegetation p = parcelle;
                reset();
                parcelle = p;
                
            }

        }

    }

}
