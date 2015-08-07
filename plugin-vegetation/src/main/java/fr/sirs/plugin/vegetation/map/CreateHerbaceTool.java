
package fr.sirs.plugin.vegetation.map;

import fr.sirs.core.model.HerbaceeVegetation;
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
public class CreateHerbaceTool extends CreateVegetationPolygonTool{

    public static final Spi SPI = new Spi();

    public static final class Spi extends AbstractEditionToolSpi{

        public Spi() {
            super("CreateHerbace",
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle",
                        "fr.sirs.plugin.vegetation.map.CreateHerbaceTool.title",CreateHerbaceTool.class.getClassLoader()),
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle",
                        "fr.sirs.plugin.vegetation.map.CreateHerbaceTool.abstract",CreateHerbaceTool.class.getClassLoader()),
                new Image("fr/sirs/plugin/vegetation/herbace.png"));
        }

        @Override
        public boolean canHandle(Object candidate) {
            return true;
        }

        @Override
        public EditionTool create(FXMap map, Object layer) {
            return new CreateHerbaceTool(map);
        }
    };

    public CreateHerbaceTool(FXMap map) {
        super(map,SPI, HerbaceeVegetation.class);
    }

    @Override
    protected PositionableVegetation newVegetation() {
        final HerbaceeVegetation candidate = new HerbaceeVegetation();
        return candidate;
    }

}
