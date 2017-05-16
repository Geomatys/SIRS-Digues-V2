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

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.component.Previews;
import fr.sirs.util.property.Reference;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collections;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.DocumentNotFoundException;
import org.opengis.feature.Feature;

/**
 * Printer used by default to convert object properties into string for ODT templates.
 *
 * @author Alexis Manin (Geomatys)
 */
class DefaultPrinter extends PropertyPrinter {

    private final Previews previews;

    public DefaultPrinter() {
        super(Collections.EMPTY_LIST);
        previews = InjectorCore.getBean(SessionCore.class).getPreviews();
    }

    @Override
    public String print(Object source, PropertyDescriptor property) throws ReflectiveOperationException {
        ArgumentChecks.ensureNonNull("Object holding property to print", source);
        ArgumentChecks.ensureNonNull("Descriptor for the property to print", property);
        return printImpl(source, property);
    }


    @Override
    protected String printImpl(Object source, PropertyDescriptor property) throws ReflectiveOperationException {
        final Method readMethod = property.getReadMethod();
            if (readMethod == null) {
                throw new IllegalArgumentException("Given property descriptor has no accessor defined.");
            } else {
                readMethod.setAccessible(true);
            }

            // Check if we've got a real data or a link.
            final Reference ref = readMethod.getAnnotation(Reference.class);
            final Class<?> refClass;
            if (ref != null) {
                refClass = ref.ref();
            } else {
                refClass = null;
            }

            Object value = readMethod.invoke(source);
            if (value == null) {
              return "N/A";
            } else if (refClass != null && (value instanceof String)) {
                try {
                    return previews.get((String) value).getLibelle();
                } catch (DocumentNotFoundException ex) {
                    // La preview n'a pas pu être trouvée, ce qui indique que l'objet pointé a été supprimé.
                    return "Objet supprimé !";
                }
            } else if (refClass == null) {
                return value.toString(); // TODO : better string conversion ?
            } else {
                throw new SirsCoreRuntimeException("A reference attribute must be a string ! Found : "+value + " from object "+source);
            }
    }

    @Override
    protected String printImpl(Feature source, String propertyToPrint) {
        final Object propertyValue = source.getPropertyValue(propertyToPrint);
        return propertyValue == null? "N/A" : propertyValue.toString();
    }
}
