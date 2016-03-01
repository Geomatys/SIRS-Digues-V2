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
