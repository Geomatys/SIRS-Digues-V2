package fr.sirs.map;

import fr.sirs.Injector;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.TronconDigue;
import java.awt.Dimension;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXMapAction;
import org.geotoolkit.internal.GeotkFX;

/**
 * Map action to import {@link BorneDigue} into a selected {@link TronconDigue}.
 * 
 * @author Alexis Manin (Geomatys)
 */
public class FXImportBornesAction extends FXMapAction {
    // For unknown reason, making icon 16*16 as other buttons on toolbar makes it one pixel less tall on screen.
    public static final Image ICON = SwingFXUtils.toFXImage(GeotkFX.getBufferedImage("add-vector", new Dimension(16, 17)), null);

        
    public FXImportBornesAction(FXMap map) {
        super(map, "Importer des bornes",
                "Import de bornes depuis un fichier Shapefile", ICON);
        disabledProperty().bind(Injector.getSession().geometryEditionProperty().not());
    }
    
    @Override
    public void accept(ActionEvent t) {
        FXImportBornesPane.showImportDialog("tronçon", TronconDigue.class);
    }

}
