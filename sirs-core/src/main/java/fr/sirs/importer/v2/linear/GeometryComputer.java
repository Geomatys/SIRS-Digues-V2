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
