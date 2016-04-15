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
package fr.sirs.util.odt;

import fr.sirs.core.SirsCore;
import java.beans.PropertyDescriptor;
import org.opengis.feature.Feature;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class AmontAvalPrinter extends PropertyPrinter {

    private static final String AVAL_MSG = "en aval de la borne";
    private static final String AMONT_MSG = "en amont de la borne";

    public AmontAvalPrinter() {
        super(SirsCore.BORNE_DEBUT_AVAL, SirsCore.BORNE_FIN_AVAL);
    }

    @Override
    protected String printImpl(Object source, PropertyDescriptor property) throws ReflectiveOperationException {
        if (Boolean.TRUE.equals(property.getReadMethod().invoke(source))) {
            return AMONT_MSG;
        } else {
            return AVAL_MSG;
        }
    }

    @Override
    protected String printImpl(Feature source, String propertyToPrint) {
         if (Boolean.TRUE.equals(source.getPropertyValue(propertyToPrint))) {
             return AMONT_MSG;
         } else return AVAL_MSG;
    }
}
