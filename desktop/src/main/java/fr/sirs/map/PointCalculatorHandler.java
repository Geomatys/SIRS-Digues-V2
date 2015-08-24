
package fr.sirs.map;

import com.vividsolutions.jts.geom.Coordinate;
import fr.sirs.CorePlugin;
import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.model.TronconDigue;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display2d.container.ContextContainer2D;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.gui.javafx.render2d.AbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.navigation.FXPanHandler;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;

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
    private int pickType = 0;
    private FeatureMapLayer tronconLayer = null;
    private EditionHelper helperTroncon;

    public PointCalculatorHandler() {
    }

    public FXGeometryLayer getDecoration() {
        return decoration;
    }

    /**
     * Activer le mode de selection.
     * 
     * @param type  0 : -
     *              1 : troncon
     *              2 : coordonnée
     */
    void setPickType(int type){
        this.pickType = type;
        if(type==0){
            map.setCursor(Cursor.DEFAULT);
        }else{
            map.setCursor(Cursor.CROSSHAIR);
        }
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


        final ContextContainer2D cc = (ContextContainer2D) map.getCanvas().getContainer();
        final MapContext context = cc.getContext();
        for(MapLayer layer : context.layers()){
            layer.setSelectable(false);
            if(layer.getName().equalsIgnoreCase(CorePlugin.TRONCON_LAYER_NAME)){
                tronconLayer = (FeatureMapLayer) layer;
                layer.setSelectable(true);
            }
        }
        helperTroncon = new EditionHelper(map, tronconLayer);
        helperTroncon.setMousePointerSize(6);

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

        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);

            if(e.getButton()==MouseButton.PRIMARY){
                if(pickType==1){
                    //on recherche un troncon
                    final Feature feature = helperTroncon.grabFeature(e.getX(), e.getY(), false);
                    if(feature !=null){
                        Object bean = feature.getUserData().get(BeanFeature.KEY_BEAN);
                        if(bean instanceof TronconDigue){
                            final Session session = Injector.getSession();
                            bean = session.getRepositoryForClass(TronconDigue.class).get(((TronconDigue)bean).getDocumentId());
                            pane.uiSourceTroncon.setValue((TronconDigue)bean);
                            setPickType(0);
                            pane.uiPickTroncon.setSelected(false);
                            pane.uiPickCoord.setSelected(false);
                        }
                    }

                }else if(pickType==2){
                    //on prend la coordonnée du click
                    final Coordinate coord = helperTroncon.toCoord(e.getX(), e.getY());
                    pane.uiSourceX.getValueFactory().setValue(coord.x);
                    pane.uiSourceY.getValueFactory().setValue(coord.y);
                    setPickType(0);
                    pane.uiPickTroncon.setSelected(false);
                    pane.uiPickCoord.setSelected(false);
                }
            }

        }

    }

}
