
package fr.sym.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import fr.sym.Plugin;
import fr.sym.Plugins;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.apache.sis.internal.referencing.j2d.AffineTransform2D;
import org.controlsfx.dialog.Dialogs;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display.VisitFilter;
import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.container.ContextContainer2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.FXAbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.navigation.AbstractMouseHandler;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.internal.Loggers;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class TronconEditHandler extends FXAbstractNavigationHandler {

    
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private final MouseListen mouseInputListener = new MouseListen();
    private final FXGeometryLayer geomlayer= new FXGeometryLayer();
    private final double zoomFactor = 2;
    
    private FeatureMapLayer tronconLayer = null;
    private TronconDigue troncon = null;
    
    private final List<Coordinate> coords = new ArrayList<>();
    
    
    public TronconEditHandler(final FXMap map) {
        super(map);
        
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void install(final FXMap component) {
        super.install(component);
        component.addEventHandler(MouseEvent.ANY, mouseInputListener);
        component.addEventHandler(ScrollEvent.ANY, mouseInputListener);
        map.setCursor(Cursor.CROSSHAIR);
        map.addDecoration(0,geomlayer);
        
        //recuperation du layer de troncon
        tronconLayer = null;
        troncon = null;
        final ContextContainer2D cc = (ContextContainer2D) map.getCanvas().getContainer();
        final MapContext context = cc.getContext();
        for(MapLayer layer : context.layers()){
            layer.setSelectable(false);
            if(layer.getName().equalsIgnoreCase("TronconDigue")){
                tronconLayer = (FeatureMapLayer) layer;
                layer.setSelectable(true);
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void uninstall(final FXMap component) {
        super.uninstall(component);
        component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
        component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
        map.removeDecoration(geomlayer);
        component.setBottom(null);
    }
    
    private void updateGeometry(){
        final List<Geometry> geoms = new ArrayList<>();
        if(coords.size() == 1){
            //single point
            final Geometry geom = GEOMETRY_FACTORY.createPoint(coords.get(0));
            JTS.setCRS(geom, map.getCanvas().getObjectiveCRS2D());
            geoms.add(geom);
        }else if(coords.size() > 1){
            //line
            final Geometry geom = GEOMETRY_FACTORY.createLineString(coords.toArray(new Coordinate[coords.size()]));
            JTS.setCRS(geom, map.getCanvas().getObjectiveCRS2D());
            geoms.add(geom);
        }
        geomlayer.getGeometries().setAll(geoms);
    }
    
    
    private class MouseListen extends AbstractMouseHandler {

        private double startX;
        private double startY;
        private MouseButton mousebutton;

        @Override
        public void mouseClicked(final MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
            mousebutton = e.getButton();
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            if(tronconLayer==null) return;
            
            if(troncon==null){
                
                //recherche du troncon
                final Rectangle2D clickArea = new Rectangle2D.Double(e.getX()-2, e.getY()-2, 4, 4);
                map.getCanvas().getGraphicsIn(clickArea, 
                        new AbstractGraphicVisitor() {

                    @Override
                    public void visit(ProjectedFeature feature, RenderingContext2D context, SearchAreaJ2D area) {
                        
                    }

                    @Override
                    public void visit(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D area) {}

                    @Override
                    public void endVisit() {
                        super.endVisit();
                        if(troncon == null){
                        }
                    }
                    
                    
                    
                },VisitFilter.INTERSECTS);
                
            }
            
            
            startX = e.getX();
            startY = e.getY();

            mousebutton = e.getButton();
            if (mousebutton == MouseButton.PRIMARY) {
                //add a coordinate
                try {
                    final AffineTransform2D dispToObj = map.getCanvas().getDisplayToObjective();
                    final double[] crds = new double[]{e.getX(),e.getY()};
                    dispToObj.transform(crds, 0, crds, 0, 1);
                    coords.add(new Coordinate(crds[0], crds[1]));
                    updateGeometry();
                } catch (NoninvertibleTransformException ex) {
                    Loggers.JAVAFX.log(Level.WARNING, null, ex);
                }

            } else if (mousebutton == MouseButton.SECONDARY) {
                //erase coordiantes
                coords.clear();
                updateGeometry();
            }
        }

        @Override
        public void mouseExited(final MouseEvent e) {
            decorationPane.setFill(false);
            decorationPane.setCoord(-10, -10,-10, -10, true);
        }

        @Override
        public void mouseMoved(final MouseEvent e){
            startX = e.getX();
            startY = e.getY();
        }
        
        @Override
        public void mouseWheelMoved(final ScrollEvent e) {
            final double rotate = -e.getDeltaY();

            if(rotate<0){
                scale(new Point2D.Double(startX, startY),zoomFactor);
            }else if(rotate>0){
                scale(new Point2D.Double(startX, startY),1d/zoomFactor);
            }

        }
    }
    
    
}
