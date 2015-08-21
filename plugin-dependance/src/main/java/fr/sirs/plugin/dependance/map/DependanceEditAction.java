package fr.sirs.plugin.dependance.map;

import fr.sirs.Injector;
import javafx.event.ActionEvent;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXMapAction;
import org.geotoolkit.internal.GeotkFX;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class DependanceEditAction extends FXMapAction {
    public DependanceEditAction(FXMap map) {
        super(map,"Dépendance","Création / Modification de dépendance", GeotkFX.ICON_EDIT);

        this.disabledProperty().bind(Injector.getSession().geometryEditionProperty().not());

        map.getHandlerProperty().addListener((observable, oldValue, newValue) -> {
            selectedProperty().set(newValue instanceof DependanceEditHandler);
        });
    }

    @Override
    public void accept(ActionEvent event) {
        if (map != null) {
            map.setHandler(new DependanceEditHandler());
        }
    }
}
