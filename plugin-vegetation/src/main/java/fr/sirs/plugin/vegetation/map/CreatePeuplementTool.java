
package fr.sirs.plugin.vegetation.map;

import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import static fr.sirs.plugin.vegetation.PluginVegetation.DEFAULT_PEUPLEMENT_VEGETATION_TYPE;
import fr.sirs.util.ResourceInternationalString;
import javafx.scene.image.Image;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionToolSpi;
import org.geotoolkit.gui.javafx.render2d.edition.EditionTool;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CreatePeuplementTool extends CreateVegetationPolygonTool<PeuplementVegetation>{

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
    protected PeuplementVegetation newVegetation() {
        final PeuplementVegetation candidate = super.newVegetation();

        //classement indéfini
        candidate.setTypeVegetationId(DEFAULT_PEUPLEMENT_VEGETATION_TYPE);

        /*
        Si on peut, on paramètre le traitement qui a été associé dans super.newVegetation();
        Il est nécessaire pour cela d'associer un identifiant de parcelle à la zone de végétation.
        */
        if(parcelle!=null && parcelle.getId()!=null){
            candidate.setParcelleId(parcelle.getId());
            PluginVegetation.paramTraitement(PeuplementVegetation.class, candidate, DEFAULT_PEUPLEMENT_VEGETATION_TYPE);
        }

        return candidate;
    }

}
