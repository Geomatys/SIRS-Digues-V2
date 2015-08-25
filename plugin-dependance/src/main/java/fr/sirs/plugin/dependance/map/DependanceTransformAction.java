package fr.sirs.plugin.dependance.map;

import fr.sirs.Injector;
import javafx.event.ActionEvent;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXMapAction;
import org.geotoolkit.internal.GeotkFX;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class DependanceTransformAction extends FXMapAction {
    public DependanceTransformAction(FXMap map) {
        super(map,"Dépendance","Transformer une géométrie en dépendance", GeotkFX.ICON_DUPLICATE);

        this.disabledProperty().bind(Injector.getSession().geometryEditionProperty().not());

        map.getHandlerProperty().addListener((observable, oldValue, newValue) -> {
            selectedProperty().set(newValue instanceof DependanceTransformHandler);
        });
    }

    @Override
    public void accept(ActionEvent event) {
        if (map != null && !(map.getHandler() instanceof DependanceTransformHandler)) {
            map.setHandler(new DependanceTransformHandler());
        }
    }
}
