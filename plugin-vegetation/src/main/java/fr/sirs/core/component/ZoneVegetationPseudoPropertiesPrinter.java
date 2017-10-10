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
package fr.sirs.core.component;

import fr.sirs.core.SirsCore;
import static fr.sirs.theme.ui.ZoneVegetationPojoTable.SURFACE_COLUMN;
import static fr.sirs.theme.ui.ZoneVegetationPojoTable.VEGETATION_TYPE_COLUMN;
import static fr.sirs.theme.ui.ZoneVegetationPojoTable.ZONE_CLASS_COLUMN;
import fr.sirs.util.odt.PropertyPrinter;
import java.beans.PropertyDescriptor;
import org.opengis.feature.Feature;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys) &lt;samuel.andres at geomatys.com&gt;
 */
@Component
public class ZoneVegetationPseudoPropertiesPrinter extends PropertyPrinter {
    
    public ZoneVegetationPseudoPropertiesPrinter() {
        super(ZONE_CLASS_COLUMN, VEGETATION_TYPE_COLUMN, SURFACE_COLUMN);
        SirsCore.LOGGER.info("instanciation of ZoneVegetation pseudo-properties printer");
    }

    @Override
    protected String printImpl(Feature source, String propertyToPrint) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected String printImpl(Object source, PropertyDescriptor property) throws ReflectiveOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
