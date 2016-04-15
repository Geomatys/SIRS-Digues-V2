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

import com.vividsolutions.jts.geom.Geometry;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Positionable;
import java.util.logging.Level;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.CRS;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A listener which computes a positionable envelope (in long/lat : CRS:84)
 * when its geometry changes.
 *
 * @author Alexis Manin (Geomatys)
 */
public class ComputeEnvelopeOnGeometryChange implements ChangeListener<Geometry> {

    private final Positionable target;

    public ComputeEnvelopeOnGeometryChange(final Positionable toListenOn) {
        ArgumentChecks.ensureNonNull("Positionable to listen on", toListenOn);
        target = toListenOn;
    }

    @Override
    public void changed(ObservableValue<? extends Geometry> observable, Geometry oldValue, Geometry newValue) {
        if (newValue != null) {
            try {
                CoordinateReferenceSystem geometryCRS = JTS.findCoordinateReferenceSystem(newValue);
                if (geometryCRS == null) {
                    geometryCRS = InjectorCore.getBean(SessionCore.class).getProjection();
                }
                Envelope envelope2D = JTS.getEnvelope2D(newValue.getEnvelopeInternal(), geometryCRS);
                if (!CRS.equalsApproximatively(geometryCRS, CommonCRS.WGS84.normalizedGeographic())) {
                    envelope2D = CRS.transform(envelope2D, CommonCRS.WGS84.normalizedGeographic());
                }
                target.setLongitudeMin(envelope2D.getMinimum(0));
                target.setLongitudeMax(envelope2D.getMaximum(0));
                target.setLatitudeMin(envelope2D.getMinimum(1));
                target.setLatitudeMax(envelope2D.getMaximum(1));
            } catch (Exception e) {
                SirsCore.LOGGER.log(Level.WARNING, "Cannot compute CRS:84 envelope.", e);
            }
        }
    }
}
