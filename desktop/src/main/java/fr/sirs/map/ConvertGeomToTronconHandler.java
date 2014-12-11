
package fr.sirs.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.TronconDigue;
import static fr.sirs.map.TronconEditHandler.showTronconDialog;
import static fr.sirs.map.TronconEditHandler.updateSRElementaire;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.logging.Level;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.geotoolkit.display.VisitFilter;
import org.geotoolkit.display2d.GraphicVisitor;
import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.FXAbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.navigation.AbstractMouseHandler;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ConvertGeomToTronconHandler extends FXAbstractNavigationHandler {

    private static final int CROSS_SIZE = 5;
    
    private final MouseListen mouseInputListener = new MouseListen();
    private final FXGeometryLayer geomlayer= new FXGeometryLayer(){
        @Override
        protected Node createVerticeNode(Coordinate c){
            final Line h = new Line(c.x-CROSS_SIZE, c.y, c.x+CROSS_SIZE, c.y);
            final Line v = new Line(c.x, c.y-CROSS_SIZE, c.x, c.y+CROSS_SIZE);
            h.setStroke(Color.RED);
            v.setStroke(Color.RED);
            return new Group(h,v);
        }
    };
    private final double zoomFactor = 2;
    
        
    
    public ConvertGeomToTronconHandler(final FXMap map) {
        super(map);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void install(final FXMap map) {
        super.install(map);
        map.addEventHandler(MouseEvent.ANY, mouseInputListener);
        map.addEventHandler(ScrollEvent.ANY, mouseInputListener);
        map.setCursor(Cursor.CROSSHAIR);
        map.addDecoration(0,geomlayer);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean uninstall(final FXMap component) {
        super.uninstall(component);
        component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
        component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
        map.removeDecoration(geomlayer);
        return true;
    }
            
    private class MouseListen extends AbstractMouseHandler {

        private final ContextMenu popup = new ContextMenu();
        private double startX;
        private double startY;

        public MouseListen() {
            popup.setAutoHide(true);
        }
        
        private double getMouseX(MouseEvent event){
            final javafx.geometry.Point2D pt = map.localToScreen(0, 0);
            return event.getScreenX()- pt.getX();
        }
        
        private double getMouseY(MouseEvent event){
            final javafx.geometry.Point2D pt = map.localToScreen(0, 0);
            return event.getScreenY() - pt.getY();
        }
        
        @Override
        public void mouseClicked(final MouseEvent e) {   
            startX = getMouseX(e);
            startY = getMouseY(e);
        }

        @Override
        public void mousePressed(final MouseEvent e) {            
            final GraphicVisitor visitor = new AbstractGraphicVisitor() {

                @Override
                public void visit(ProjectedFeature feature, RenderingContext2D context, SearchAreaJ2D area) {
                    final Feature f = feature.getCandidate();
                    final Geometry geom = (Geometry) f.getDefaultGeometryProperty().getValue();
                    LineString line = LinearReferencingUtilities.asLineString(geom);
                    
                    if(line !=null){
                        final Map.Entry<String, Digue> entry = showTronconDialog();
                        final Session session = Injector.getBean(Session.class);
                        final TronconDigue troncon = new TronconDigue();
                        troncon.setLibelle(entry.getKey());
                        if(entry.getValue()!=null) troncon.setDigueId(entry.getValue().getId());

                        try{
                            //convertion from data crs to base crs
                            line = (LineString) JTS.transform(line, CRS.findMathTransform(
                                    f.getDefaultGeometryProperty().getType().getCoordinateReferenceSystem(), 
                                    SirsCore.getEpsgCode(), true));
                            JTS.setCRS(geom, SirsCore.getEpsgCode());
                            troncon.setGeometry(line);

                            //save troncon
                            session.getTronconDigueRepository().add(troncon);
                            updateSRElementaire(troncon);
                            
                            map.getCanvas().repaint();
                        }catch(TransformException | FactoryException ex){
                            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
                        }
                    }
                }

                @Override
                public void visit(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D area) {}
            };
            
            startX = getMouseX(e);
            startY = getMouseY(e);
            final Rectangle2D rect = new Rectangle2D.Double(startX-3, startY-3, 6, 6);
            map.getCanvas().getGraphicsIn(rect, visitor, VisitFilter.INTERSECTS);
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            //do not use getX/getY to calculate difference
            //JavaFX Bug : https://javafx-jira.kenai.com/browse/RT-34608
            
            //calcul du deplacement
            startX = getMouseX(me);
            startY = getMouseY(me);
        }
        
        @Override
        public void mouseExited(final MouseEvent e) {
            decorationPane.setFill(false);
            decorationPane.setCoord(-10, -10,-10, -10, true);
        }

        @Override
        public void mouseMoved(final MouseEvent e){
            startX = getMouseX(e);
            startY = getMouseY(e);
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
