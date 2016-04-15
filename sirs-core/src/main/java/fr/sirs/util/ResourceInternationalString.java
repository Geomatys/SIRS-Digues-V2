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
package fr.sirs.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Extends ResourceInternationalString to provide a specific class loader.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class ResourceInternationalString extends org.apache.sis.util.iso.ResourceInternationalString {

    private final ClassLoader cl;

    public ResourceInternationalString(String resources, String key, ClassLoader cl) {
        super(resources, key);
        this.cl = cl;
    }

    @Override
    protected ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(resources, locale, cl);
    }
    
}
