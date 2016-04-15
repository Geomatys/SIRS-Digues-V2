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
package fr.sirs.maj;

import fr.sirs.CorePlugin;
import fr.sirs.PluginInfo;
import java.util.Comparator;

/**
 *
 * Range les plugins par nom puis par version. Version la plus récente d'abord, 
 * la plus vieille ensuite.
 * Note : Il y a une exception dans le tri. Le module Core sera toujours prioritaire.
 * @author Alexis Manin (Geomatys)
 */
public class PluginInfoComparator implements Comparator<PluginInfo> {

    @Override
    public int compare(PluginInfo t, PluginInfo t1) {
        if (t == null && t1 == null) {
            return 0;
        } else if (t == null) {
            return -1;
        } else if (t1 == null) {
            return 1;
        }
        
        if (t.getName().equalsIgnoreCase(CorePlugin.NAME)) {
            if (!t1.getName().equalsIgnoreCase(CorePlugin.NAME) || t.isOlderOrSame(t1)) {
                return 1;
            } else {
                return -1;
            }
        } else {
            final int nameComparison = t.getName().compareToIgnoreCase(t1.getName());
            if (nameComparison == 0) {
                if (t.isOlderOrSame(t1)) {
                    return 1;
                } else {
                    return -1;
                }
            } else {
                return nameComparison;
            }
        }
    }

}
