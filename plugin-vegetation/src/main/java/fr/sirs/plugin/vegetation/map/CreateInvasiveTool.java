/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.plugin.vegetation.map;

import fr.sirs.core.model.InvasiveVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import static fr.sirs.plugin.vegetation.PluginVegetation.DEFAULT_INVASIVE_VEGETATION_TYPE;
import fr.sirs.util.ResourceInternationalString;
import javafx.scene.image.Image;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionToolSpi;
import org.geotoolkit.gui.javafx.render2d.edition.EditionTool;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CreateInvasiveTool extends CreateVegetationPolygonTool<InvasiveVegetation> {

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
    protected InvasiveVegetation newVegetation() {
        final InvasiveVegetation candidate = super.newVegetation();
        //classement indéfini
        candidate.setTypeVegetationId(DEFAULT_INVASIVE_VEGETATION_TYPE);

        /*
        Si on peut, on paramètre le traitement qui a été associé dans super.newVegetation();
        Il est nécessaire pour cela d'associer un identifiant de parcelle à la zone de végétation.
        */
        if(parcelle!=null && parcelle.getId()!=null){
            candidate.setParcelleId(parcelle.getId());
            PluginVegetation.paramTraitement(InvasiveVegetation.class, candidate, DEFAULT_INVASIVE_VEGETATION_TYPE);
        }
        return candidate;
    }

}
