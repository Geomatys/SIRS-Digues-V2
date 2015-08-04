
package fr.sirs.plugin.vegetation.map;

import fr.sirs.util.ResourceInternationalString;
import javafx.scene.Node;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionTool;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionToolSpi;
import org.geotoolkit.gui.javafx.render2d.edition.EditionTool;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Johann Sorel
 */
public class CreateParcelleTool extends AbstractEditionTool{

    public static final Spi SPI = new Spi();
    public static final class Spi extends AbstractEditionToolSpi{

        public Spi() {
            super("CreateParcelle",
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle", 
                        "fr.sirs.plugin.vegetation.map.CreateParcelleTool.title",CreateParcelleTool.class.getClassLoader()),
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle", 
                        "fr.sirs.plugin.vegetation.map.CreateParcelleTool.abstract",CreateParcelleTool.class.getClassLoader()),
                GeotkFX.ICON_ADD);
        }

        @Override
        public boolean canHandle(Object candidate) {
            return true;
        }

        @Override
        public EditionTool create(FXMap map, Object layer) {
            return new CreateParcelleTool(map);
        }
    };

    public CreateParcelleTool(FXMap map) {
        super(SPI);
    }

    @Override
    public Node getConfigurationPane() {
        return null;
    }

    @Override
    public Node getHelpPane() {
        return null;
    }

}
