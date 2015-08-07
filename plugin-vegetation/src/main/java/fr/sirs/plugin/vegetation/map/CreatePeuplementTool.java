
package fr.sirs.plugin.vegetation.map;

import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.PositionableVegetation;
import fr.sirs.util.ResourceInternationalString;
import javafx.scene.image.Image;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionToolSpi;
import org.geotoolkit.gui.javafx.render2d.edition.EditionTool;

/**
 *
 * @author Johann Sorel
 */
public class CreatePeuplementTool extends CreateVegetationPolygonTool{

    public static final Spi SPI = new Spi();

    public static final class Spi extends AbstractEditionToolSpi{

        public Spi() {
            super("CreatePeuplement",
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle",
                        "fr.sirs.plugin.vegetation.map.CreatePeuplementTool.title",CreatePeuplementTool.class.getClassLoader()),
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle",
                        "fr.sirs.plugin.vegetation.map.CreatePeuplementTool.abstract",CreatePeuplementTool.class.getClassLoader()),
                new Image("fr/sirs/plugin/vegetation/peuplement.png"));
        }

        @Override
        public boolean canHandle(Object candidate) {
            return true;
        }

        @Override
        public EditionTool create(FXMap map, Object layer) {
            return new CreatePeuplementTool(map);
        }
    };

    public CreatePeuplementTool(FXMap map) {
        super(map,SPI, PeuplementVegetation.class);
    }

    @Override
    protected PositionableVegetation newVegetation() {
        final PeuplementVegetation candidate = new PeuplementVegetation();
        //classement indéfini
        candidate.setTypePeuplementId("RefTypePeuplementVegetation:99");
        return candidate;
    }

}
