
package fr.sirs.plugin.vegetation.map;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.Injector;
import static fr.sirs.SIRS.CSS_PATH;
import fr.sirs.Session;
import fr.sirs.core.model.PositionableVegetation;
import fr.sirs.util.ResourceInternationalString;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display.VisitFilter;
import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionTool;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionToolSpi;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.edition.EditionTool;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.map.FeatureMapLayer;

/**
 * Outil d'édition de vegetation existante.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class EditVegetationTool extends AbstractEditionTool{

    public static final Spi SPI = new Spi();
    public static final class Spi extends AbstractEditionToolSpi{

        public Spi() {
            super("EditVegetation",
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle", 
                        "fr.sirs.plugin.vegetation.map.EditVegetationTool.title",EditVegetationTool.class.getClassLoader()),
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle", 
                        "fr.sirs.plugin.vegetation.map.EditVegetationTool.abstract",EditVegetationTool.class.getClassLoader()),
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
    private final MouseListen mouseInputListener = new MouseListen();
    private final BorderPane wizard = new BorderPane();
    private final FXPositionableForm form = new FXPositionableForm();


    //geometry en cours
    private EditionHelper helper;
    private final FXGeometryLayer decoration = new FXGeometryLayer();
    private final EditionHelper.EditionGeometry selection = new EditionHelper.EditionGeometry();
    private boolean modified = false;
    private MouseButton pressed = null;

    public EditVegetationTool(FXMap map) {
        super(SPI);
        wizard.getStylesheets().add(CSS_PATH);
        session = Injector.getSession();
    }

    public void setCurrentFeature(final Feature feature, FeatureMapLayer layer){
        final Object bean = feature.getUserData().get(BeanFeature.KEY_BEAN);
        form.positionableProperty().set((PositionableVegetation)bean);
        helper = new EditionHelper(map, layer);
        if(feature != null){
            this.selection.geometry = helper.toObjectiveCRS(feature);
        }else{
            this.selection.geometry = null;
        }
        refreshDecoration();
    }

    private void reset(){
        selection.reset();
        modified = false;
        decoration.getGeometries().clear();
        decoration.setNodeSelection(null);
    }

    private void refreshDecoration(){
        decoration.getGeometries().setAll(this.selection.geometry);
        decoration.setNodeSelection(this.selection);
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
        component.addEventHandler(KeyEvent.ANY, mouseInputListener);
        component.setCursor(Cursor.CROSSHAIR);
        component.addDecoration(decoration);
    }

    @Override
    public boolean uninstall(FXMap component) {
        super.uninstall(component);
        component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
        component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
        component.removeEventHandler(KeyEvent.ANY, mouseInputListener);
        component.setCursor(Cursor.DEFAULT);
        component.removeDecoration(decoration);
        reset();
        return true;
    }

    private class MouseListen extends FXPanMouseListen {

        public MouseListen() {
            super(EditVegetationTool.this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {

            final MouseButton button = e.getButton();

            if(button == MouseButton.PRIMARY){
                if(selection.geometry == null){
                    final Rectangle2D clickArea = new Rectangle2D.Double(e.getX()-2, e.getY()-2, 4, 4);

                    //recherche d'un object a editer
                    map.getCanvas().getGraphicsIn(clickArea, new AbstractGraphicVisitor() {
                        @Override
                        public void visit(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D area) {
                            final Object bean = graphic.getCandidate().getUserData().get(BeanFeature.KEY_BEAN);
                            if(bean instanceof PositionableVegetation){
                                setCurrentFeature(graphic.getCandidate(),graphic.getLayer());
                            }
                        }
                        @Override
                        public boolean isStopRequested() {
                            return form.positionableProperty().get()!=null;
                        }
                        @Override
                        public void visit(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D area) {}
                    }, VisitFilter.INTERSECTS);

                }else if(e.getClickCount() >= 2){
                    //double click = add a node
                    final Geometry result;
                    if(selection.geometry instanceof LineString){
                        result = helper.insertNode((LineString)selection.geometry, e.getX(), e.getY());
                    }else if(selection.geometry instanceof Polygon){
                        result = helper.insertNode((Polygon)selection.geometry, e.getX(), e.getY());
                    }else if(selection.geometry instanceof GeometryCollection){
                        result = helper.insertNode((GeometryCollection)selection.geometry, e.getX(), e.getY());
                    }else{
                        result = selection.geometry;
                    }
                    modified = modified || result != selection.geometry;
                    selection.geometry = result;
                    decoration.getGeometries().setAll(selection.geometry);
                }else if(e.getClickCount() == 1){
                    //single click with a geometry = select a node
                    helper.grabGeometryNode(e.getX(), e.getY(), selection);
                    decoration.setNodeSelection(selection);
                }
            }else if(button == MouseButton.SECONDARY){
                //TODO on sauvegarde
                reset();
            }

        }

        @Override
        public void mousePressed(final MouseEvent e) {
            pressed = e.getButton();

            if(pressed == MouseButton.PRIMARY){
                if(selection.geometry == null){
                    //setCurrentFeature(helper.grabFeature(e.getX(), e.getY(), false));
                }else if(e.getClickCount() == 1){
                    //single click with a geometry = select a node
                    helper.grabGeometryNode(e.getX(), e.getY(), selection);
                    decoration.setNodeSelection(selection);
                }
            }

            super.mousePressed(e);
        }

        @Override
        public void mouseDragged(final MouseEvent e) {

            if(pressed == MouseButton.PRIMARY && selection != null){
                //dragging node
                selection.moveSelectedNode(helper.toCoord(e.getX(), e.getY()));
                refreshDecoration();
                modified = true;
                return;
            }

            super.mouseDragged(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if(KeyCode.DELETE == e.getCode() && selection != null){
                //delete node
                selection.deleteSelectedNode();
                refreshDecoration();
                modified = true;
            }
        }


    }

}
