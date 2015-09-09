
package fr.sirs.plugin.berge.map;

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
public class ConvertGeomToBergeAction extends FXMapAction {
        
    public ConvertGeomToBergeAction(FXMap map) {
        super(map,"Géometrie vers Berge","Convertir une géométrie en berge",GeotkFX.ICON_DUPLICATE);
        
        final Session session = Injector.getSession();
        this.disabledProperty().bind(session.geometryEditionProperty().not());
        
        map.getHandlerProperty().addListener(new ChangeListener<FXCanvasHandler>() {
            @Override
            public void changed(ObservableValue<? extends FXCanvasHandler> observable, FXCanvasHandler oldValue, FXCanvasHandler newValue) {
                selectedProperty().set(newValue instanceof ConvertGeomToBergeHandler);
            }
        });
    }

    @Override
    public void accept(ActionEvent event) {
        if (map != null) {
            map.setHandler(new ConvertGeomToBergeHandler(map));
        }
    }
    
}
