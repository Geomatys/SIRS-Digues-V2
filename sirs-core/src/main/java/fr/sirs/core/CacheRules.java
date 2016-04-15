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
package fr.sirs.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class CacheRules {

    public static final AtomicBoolean cacheAllDocs = new AtomicBoolean(false);

    public static final ConcurrentHashMap<Class, Boolean> typesToCache = new ConcurrentHashMap<>();

    /**
     * A method which tells if repositories working with a specific data type should cache them as long as possible, or just ensure unique instance of loaded objects.
     * @param toTest The data type to test.
     * @return True if given data type should be cached, false if we should just ensure unicity.
     */
    public static boolean cacheElementsOfType(final Class toTest) {
        if (cacheAllDocs.get()) {
            return true;
        }
        final Boolean typeRule = typesToCache.get(toTest);
        if (typeRule != null) {
            return typeRule;
        }

        for (final Map.Entry<Class, Boolean> entry : typesToCache.entrySet()) {
            if (entry.getKey().isAssignableFrom(toTest)) {
                return entry.getValue();
            }
        }
        return false;
    }
}
