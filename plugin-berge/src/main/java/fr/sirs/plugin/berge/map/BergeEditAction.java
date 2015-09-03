
package fr.sirs.plugin.berge.map;

import fr.sirs.Injector;
import fr.sirs.map.TronconEditHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import org.geotoolkit.gui.javafx.render2d.FXCanvasHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXMapAction;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author guilhem
 */
public class BergeEditAction extends FXMapAction {
        
    public BergeEditAction(FXMap map) {
        super(map,"Berge","Edition/Création de berge",GeotkFX.ICON_EDIT);
        
        this.disabledProperty().bind(Injector.getSession().geometryEditionProperty().not());
        
        map.getHandlerProperty().addListener(new ChangeListener<FXCanvasHandler>() {
            @Override
            public void changed(ObservableValue<? extends FXCanvasHandler> observable, FXCanvasHandler oldValue, FXCanvasHandler newValue) {
                selectedProperty().set(newValue instanceof BergeEditHandler);
            }
        });
    }

    @Override
    public void accept(ActionEvent event) {
        if (map != null) {
            map.setHandler(new BergeEditHandler(map));
        }
    }
    
}

