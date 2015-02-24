package fr.sirs.map;

import java.awt.Dimension;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXMapAction;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXImportBornesAction extends FXMapAction {
    public static final Image ICON = SwingFXUtils.toFXImage(GeotkFX.getBufferedImage("add-vector", new Dimension(16, 16)), null);

    public FXImportBornesAction(FXMap map) {
        super(map, "Importer des bornes",
                "Import de bornes depuis un fichier Shapefile.", ICON);
    }
    
    @Override
    public void accept(ActionEvent t) {
        FXImportBornesPane.showImportDialog();
    }

}
