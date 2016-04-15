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

import fr.sirs.core.model.HerbaceeVegetation;
import fr.sirs.util.ResourceInternationalString;
import javafx.scene.image.Image;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.edition.AbstractEditionToolSpi;
import org.geotoolkit.gui.javafx.render2d.edition.EditionTool;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CreateHerbaceTool extends CreateVegetationPolygonTool<HerbaceeVegetation> {

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
        super(map, SPI, HerbaceeVegetation.class);
    }
}
