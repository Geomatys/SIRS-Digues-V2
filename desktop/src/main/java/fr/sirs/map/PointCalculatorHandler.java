
package fr.sirs.map;

import com.vividsolutions.jts.geom.Coordinate;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.geotoolkit.gui.javafx.render2d.AbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.navigation.FXPanHandler;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class PointCalculatorHandler extends AbstractNavigationHandler {

    private static final int CROSS_SIZE = 20;

    private final MouseListen mouseInputListener = new MouseListen();
    private final FXGeometryLayer decoration= new FXGeometryLayer(){
        @Override
        protected Node createVerticeNode(Coordinate c, boolean selected){
            final Line h = new Line(c.x-CROSS_SIZE, c.y, c.x+CROSS_SIZE, c.y);
            final Line v = new Line(c.x, c.y-CROSS_SIZE, c.x, c.y+CROSS_SIZE);
            h.setStroke(Color.RED);
            h.setStrokeWidth(2);
            v.setStroke(Color.RED);
            v.setStrokeWidth(2);
            return new Group(h,v);
        }
    };

    private final FXPRPane pane = new FXPRPane(this);
    private Stage dialog = null;

    public PointCalculatorHandler() {
    }

    public FXGeometryLayer getDecoration() {
        return decoration;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void install(final FXMap component) {
        super.install(component);
        component.addEventHandler(MouseEvent.ANY, mouseInputListener);
        component.addEventHandler(ScrollEvent.ANY, mouseInputListener);
        map.addDecoration(0,decoration);

        dialog = new Stage();
        dialog.setAlwaysOnTop(true);
        dialog.initModality(Modality.NONE);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Outil de repérage SR");

        final BorderPane bpane = new BorderPane(pane);
        final Scene scene = new Scene(bpane);

        dialog.setOnCloseRequest((WindowEvent evt) -> component.setHandler(new FXPanHandler(true)));
        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.show();

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean uninstall(final FXMap component) {
        super.uninstall(component);
        component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
        component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
        component.removeDecoration(decoration);
        dialog.close();
        return true;
    }

    private class MouseListen extends FXPanMouseListen {

        public MouseListen() {
            super(PointCalculatorHandler.this);
        }

    }

}
