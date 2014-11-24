
package fr.sirs.map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import org.geotoolkit.gui.javafx.render2d.FXCanvasHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXMapAction;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class BorneEditAction extends FXMapAction {
        
    public BorneEditAction(FXMap map) {
        super(map,"Système de repèrage","Edition/Création de borne",GeotkFX.ICON_EDIT);
        
        map.getHandlerProperty().addListener(new ChangeListener<FXCanvasHandler>() {
            @Override
            public void changed(ObservableValue<? extends FXCanvasHandler> observable, FXCanvasHandler oldValue, FXCanvasHandler newValue) {
                selectedProperty().set(newValue instanceof BorneEditHandler);
            }
        });
    }

    @Override
    public void accept(ActionEvent event) {
        if (map != null) {
            map.setHandler(new BorneEditHandler(map));
        }
    }
    
}
