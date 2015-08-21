
package fr.sirs.map;

import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
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

    private final MouseListen mouseInputListener = new MouseListen();
    private final FXGeometryLayer geomlayer= new FXGeometryLayer();

    private final FXPRPane pane = new FXPRPane();
    private Stage dialog = null;

    public PointCalculatorHandler() {
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void install(final FXMap component) {
        super.install(component);
        component.addEventHandler(MouseEvent.ANY, mouseInputListener);
        component.addEventHandler(ScrollEvent.ANY, mouseInputListener);
        map.addDecoration(0,geomlayer);

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
        dialog.setWidth(350);
        dialog.setHeight(450);
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
        component.removeDecoration(geomlayer);
        dialog.close();
        return true;
    }

    private class MouseListen extends FXPanMouseListen {

        public MouseListen() {
            super(PointCalculatorHandler.this);
        }

    }

}
