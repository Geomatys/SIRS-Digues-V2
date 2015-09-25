
package fr.sirs.plugin.berge.map;

import fr.sirs.Injector;
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
public class TraitBergeEditAction extends FXMapAction {
        
    public TraitBergeEditAction(FXMap map) {
        super(map,"Trait de berge","Edition/Création de trait de berge",new Image("/fr/sirs/plugin/berge/traideberge.png"));
        
        this.disabledProperty().bind(Injector.getSession().geometryEditionProperty().not());
        
        map.getHandlerProperty().addListener(new ChangeListener<FXCanvasHandler>() {
            @Override
            public void changed(ObservableValue<? extends FXCanvasHandler> observable, FXCanvasHandler oldValue, FXCanvasHandler newValue) {
                selectedProperty().set(newValue instanceof TraitBergeEditHandler);
            }
        });
    }

    @Override
    public void accept(ActionEvent event) {
        if (map != null) {
            map.setHandler(new TraitBergeEditHandler(map));
        }
    }
    
}

