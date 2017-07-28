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
package fr.sirs.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCore;
import java.util.logging.Level;


import org.geotoolkit.geometry.jts.JTS;

public class GeometryDeserializer extends JsonDeserializer<Geometry> {

    @Override
    public Geometry deserialize(JsonParser parser, DeserializationContext arg1) throws IOException, JsonProcessingException {
        if (parser.getText() == null || parser.getText().isEmpty())
            return null;
        try {
            Geometry value = new WKTReader().read(parser.getText());
            try {
                final SessionCore session = InjectorCore.getBean(SessionCore.class);
                if (session != null) {
                    JTS.setCRS(value, session.getProjection());
                }
            } catch (Exception e) {
                SirsCore.LOGGER.log(Level.FINE, "Cannot set CRS on geometry", e);
            }
            return value;
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}
