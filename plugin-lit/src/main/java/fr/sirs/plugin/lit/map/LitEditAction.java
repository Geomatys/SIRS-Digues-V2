
package fr.sirs.plugin.lit.map;

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
public class LitEditAction extends FXMapAction {
        
    public LitEditAction(FXMap map) {
        super(map,"Lit","Edition/Création de tronçon de lit",GeotkFX.ICON_EDIT);
        
        this.disabledProperty().bind(Injector.getSession().geometryEditionProperty().not());
        
        map.getHandlerProperty().addListener(new ChangeListener<FXCanvasHandler>() {
            @Override
            public void changed(ObservableValue<? extends FXCanvasHandler> observable, FXCanvasHandler oldValue, FXCanvasHandler newValue) {
                selectedProperty().set(newValue instanceof LitEditHandler);
            }
        });
    }

    @Override
    public void accept(ActionEvent event) {
        if (map != null) {
            map.setHandler(new LitEditHandler(map));
        }
    }
    
}

