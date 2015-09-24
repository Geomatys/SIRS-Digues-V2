
package fr.sirs.plugin.berge.map;

import fr.sirs.Injector;
import fr.sirs.Session;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import org.geotoolkit.gui.javafx.render2d.FXCanvasHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXMapAction;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ConvertGeomToTraitAction extends FXMapAction {
        
    public ConvertGeomToTraitAction(FXMap map) {
        super(map,"Géometrie vers Trait de berge","Convertir une géométrie en trait de berge",new Image("/fr/sirs/plugin/berge/geometrietraitberge.png"));
        
        final Session session = Injector.getSession();
        this.disabledProperty().bind(session.geometryEditionProperty().not());
        
        map.getHandlerProperty().addListener(new ChangeListener<FXCanvasHandler>() {
            @Override
            public void changed(ObservableValue<? extends FXCanvasHandler> observable, FXCanvasHandler oldValue, FXCanvasHandler newValue) {
                selectedProperty().set(newValue instanceof ConvertGeomToTraitHandler);
            }
        });
    }

    @Override
    public void accept(ActionEvent event) {
        if (map != null) {
            map.setHandler(new ConvertGeomToTraitHandler(map));
        }
    }
    
}
