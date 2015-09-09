
package fr.sirs.plugin.berge.map;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.map.BorneEditHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.render2d.FXCanvasHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXMapAction;

/**
 *
 * @author guilhem
 */
public class BergeBorneEditAction extends FXMapAction {
        
    public static final Image ICON_SR = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_ASTERISK,16,FontAwesomeIcons.DEFAULT_COLOR),null);
    
    public BergeBorneEditAction(FXMap map) {
        super(map,"Système de repèrage","Edition/Création de borne",ICON_SR);
        
        final Session session = Injector.getSession();
        this.disabledProperty().bind(session.geometryEditionProperty().not());
        
        map.getHandlerProperty().addListener(new ChangeListener<FXCanvasHandler>() {
            @Override
            public void changed(ObservableValue<? extends FXCanvasHandler> observable, FXCanvasHandler oldValue, FXCanvasHandler newValue) {
                selectedProperty().set(newValue instanceof BergeBorneEditHandler);
            }
        });
    }

    @Override
    public void accept(ActionEvent event) {
        if (map != null) {
            map.setHandler(new BergeBorneEditHandler(map));
        }
    }
    
}