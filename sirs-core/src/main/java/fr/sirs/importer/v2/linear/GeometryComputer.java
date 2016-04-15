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
package fr.sirs.importer.v2.linear;

import com.vividsolutions.jts.geom.LineString;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.model.Positionable;
import fr.sirs.importer.v2.ElementModifier;
import java.util.AbstractMap;
import java.util.Map;
import org.apache.sis.util.collection.Cache;
import org.geotoolkit.referencing.LinearReferencing.SegmentInfo;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class GeometryComputer implements ElementModifier<Positionable> {

    private final Cache<String, Map.Entry<LineString, SegmentInfo[]>> linearGeometries = new Cache<>(12, 12, true);

    @Override
    public Class<Positionable> getDocumentClass() {
        return Positionable.class;
    }

    @Override
    public void modify(Positionable outputData) {
        TronconUtils.PosInfo posInfo = new TronconUtils.PosInfo(outputData);
        getOrCacheGeometries(posInfo);
        posInfo.getGeometry();
    }

    /**
     * Try to retrieve reference linear information needed by given position info from the cache.
     * If such an information cannot be found, we will let given {@link TronconUtils.PosInfo} compute it
     * and cache it.
     * @param info The position object we want to compute a geometry for.
     */
    private void getOrCacheGeometries(final TronconUtils.PosInfo info) {
        final String tdId = info.getTronconId();
        Map.Entry<LineString, SegmentInfo[]> value = linearGeometries.peek(tdId);
        if (value == null) {
            // No value found in cache, we compute it from scratch.
            final Cache.Handler<Map.Entry<LineString, SegmentInfo[]>> handler = linearGeometries.lock(tdId);
            try {
                value = handler.peek();
                if (value == null) {
                    value = new AbstractMap.SimpleImmutableEntry<>(info.getTronconLinear(), info.getTronconSegments(false));
                }
            } finally {
                handler.putAndUnlock(value);
            }
        } else {
            info.setTronconLinear(value.getKey());
            info.setTronconSegments(value.getValue());
        }
    }
}
