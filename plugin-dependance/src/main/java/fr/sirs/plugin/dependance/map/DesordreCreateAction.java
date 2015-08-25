package fr.sirs.plugin.dependance.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import javafx.event.ActionEvent;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXMapAction;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class DesordreCreateAction extends FXMapAction {
    public DesordreCreateAction(FXMap map) {
        super(map,"Dépendance","Création / Modification de désordre", SIRS.ICON_WARNING);

        this.disabledProperty().bind(Injector.getSession().geometryEditionProperty().not());

        map.getHandlerProperty().addListener((observable, oldValue, newValue) -> {
            selectedProperty().set(newValue instanceof DesordreCreateHandler);
        });
    }

    @Override
    public void accept(ActionEvent event) {
        if (map != null && !(map.getHandler() instanceof DesordreCreateHandler)) {
            map.setHandler(new DesordreCreateHandler());
        }
    }
}
