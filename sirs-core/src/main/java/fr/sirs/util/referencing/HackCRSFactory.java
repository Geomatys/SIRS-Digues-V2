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
package fr.sirs.util.referencing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.factory.GeodeticObjectFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

/**
 * HACK : We override default CRS factory behaviour to attempt to replace CRS
 * read from WKT when they're not well defined.
 * @author Alexis Manin (Geomatys)
 */
public class HackCRSFactory extends GeodeticObjectFactory {

    static final Map<String,String> CRS_ALIASES;
    static {
        final HashMap<String,String> tmpMap = new HashMap<>(13);
        //old lambert
        tmpMap.put("NTF_Lambert_Zone_I",      "EPSG:27561");
        tmpMap.put("NTF_Lambert_Zone_II",     "EPSG:27562");
        tmpMap.put("NTF_Lambert_Zone_III",    "EPSG:27563");
        tmpMap.put("NTF_Lambert_Zone_IV",     "EPSG:27564");
        //rgf-93 CC-42 <> CC-50
        tmpMap.put("RGF_1993_Lambert_Zone_1", "EPSG:3942");
        tmpMap.put("RGF_1993_Lambert_Zone_2", "EPSG:3943");
        tmpMap.put("RGF_1993_Lambert_Zone_3", "EPSG:3944");
        tmpMap.put("RGF_1993_Lambert_Zone_4", "EPSG:3945");
        tmpMap.put("RGF_1993_Lambert_Zone_5", "EPSG:3946");
        tmpMap.put("RGF_1993_Lambert_Zone_6", "EPSG:3947");
        tmpMap.put("RGF_1993_Lambert_Zone_7", "EPSG:3948");
        tmpMap.put("RGF_1993_Lambert_Zone_8", "EPSG:3949");
        tmpMap.put("RGF_1993_Lambert_Zone_9", "EPSG:3950");
        CRS_ALIASES = Collections.unmodifiableMap(tmpMap);
    }

    @Override
    public CoordinateReferenceSystem createFromWKT(String text) throws FactoryException {
        final CoordinateReferenceSystem crs = super.createFromWKT(text);
        final String code = CRS_ALIASES.get(crs.getName().getCode());
        if (code != null) {
            return CRS.forCode(code);
        }

        return crs;
    }
}
