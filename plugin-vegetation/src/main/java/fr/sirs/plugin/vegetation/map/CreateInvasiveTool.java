
package fr.sirs.plugin.vegetation.map;

import fr.sirs.core.model.InvasiveVegetation;
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
public class CreateInvasiveTool extends CreateVegetationPolygonTool{

    public static final Spi SPI = new Spi();

    public static final class Spi extends AbstractEditionToolSpi{

        public Spi() {
            super("CreateInvasive",
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle",
                        "fr.sirs.plugin.vegetation.map.CreateInvasiveTool.title",CreateInvasiveTool.class.getClassLoader()),
                new ResourceInternationalString("fr/sirs/plugin/vegetation/bundle",
                        "fr.sirs.plugin.vegetation.map.CreateInvasiveTool.abstract",CreateInvasiveTool.class.getClassLoader()),
                new Image("fr/sirs/plugin/vegetation/invasives.png"));
        }

        @Override
        public boolean canHandle(Object candidate) {
            return true;
        }

        @Override
        public EditionTool create(FXMap map, Object layer) {
            return new CreateInvasiveTool(map);
        }
    };

    public CreateInvasiveTool(FXMap map) {
        super(map,SPI, InvasiveVegetation.class);
    }

    @Override
    protected PositionableVegetation newVegetation() {
        final InvasiveVegetation candidate = new InvasiveVegetation();
        //classement indéfini
        candidate.setTypeInvasive("RefTypeInvasiveVegetation:99");
        return candidate;
    }

}
