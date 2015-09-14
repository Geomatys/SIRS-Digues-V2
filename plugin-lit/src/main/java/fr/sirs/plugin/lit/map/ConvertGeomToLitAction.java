
package fr.sirs.plugin.lit.map;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.map.ConvertGeomToTronconHandler;
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
public class ConvertGeomToLitAction extends FXMapAction {
        
    public ConvertGeomToLitAction(FXMap map) {
        super(map,"Géometrie vers tronçon de lit","Convertir une géométrie en tronçon de lit",GeotkFX.ICON_DUPLICATE);
        
        final Session session = Injector.getSession();
        this.disabledProperty().bind(session.geometryEditionProperty().not());
        
        map.getHandlerProperty().addListener(new ChangeListener<FXCanvasHandler>() {
            @Override
            public void changed(ObservableValue<? extends FXCanvasHandler> observable, FXCanvasHandler oldValue, FXCanvasHandler newValue) {
                selectedProperty().set(newValue instanceof ConvertGeomToLitHandler);
            }
        });
    }

    @Override
    public void accept(ActionEvent event) {
        if (map != null) {
            map.setHandler(new ConvertGeomToLitHandler(map));
        }
    }
    
}
