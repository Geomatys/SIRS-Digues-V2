package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import fr.sirs.core.model.Positionable;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.esrigeodb.GeoDBStore;
import org.geotoolkit.feature.type.GeometryDescriptor;
import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengis.util.GenericName;

/**
 * Contains main properties and data needed for an import from an access database
 * to a couchDB database.
 *
 * @author Alexis Manin (Geomatys)
 */
public class ImportContext {

    public String startXName = "X_DEBUT";
    public String startYName = "Y_DEBUT";
    public String endXName = "X_FIN";
    public String endYName = "Y_FIN";

    public final Database inputDb;
    public final Database inputCartoDb;
    public final CoordinateReferenceSystem inputCRS;

    public final CouchDbConnector outputDb;
    public final CoordinateReferenceSystem outputCRS;

    public final MathTransform geoTransform;

    public ImportContext(final Database inputDb, final Database inputCartoDb, final CouchDbConnector outputDb, final CoordinateReferenceSystem outputCRS) throws FactoryException, MalformedURLException, DataStoreException {
        ArgumentChecks.ensureNonNull("Input database which contains properties", inputDb);
        ArgumentChecks.ensureNonNull("Input database which contains geometries", inputCartoDb);
        ArgumentChecks.ensureNonNull("Output database connection", outputDb);
        ArgumentChecks.ensureNonNull("Output projection", outputCRS);

        this.inputDb = inputDb;
        this.inputCartoDb = inputCartoDb;
        this.outputDb = outputDb;
        this.outputCRS = outputCRS;

        CoordinateReferenceSystem crs = null;
        try (final FeatureStore store = new GeoDBStore("test", inputCartoDb.getFile().toURI().toURL())) {
            for (final GenericName name : store.getNames()) {
                GeometryDescriptor geomDesc = store.getFeatureType(name).getGeometryDescriptor();
                if (geomDesc != null) {
                    crs = geomDesc.getType().getCoordinateReferenceSystem();
                    if (crs != null)
                        break;
                }
            }
        }

        if(crs==null) {
            inputCRS = CRS.decode("EPSG:27593", true);
        } else {
            inputCRS = crs;
        }

        geoTransform = CRS.findMathTransform(inputCRS, outputCRS, true);
    }

    public void setPositions(final Row input, final Positionable toSet) throws TransformException {
        final Double startX = input.getDouble(startXName);
        final Double startY = input.getDouble(startYName);
        final Double endX = input.getDouble(endXName);
        final Double endY = input.getDouble(endYName);

        if (startX  == null || startY == null) {
            final double[] points = new double[] {endX, endY};
            geoTransform.transform(points, 0, points, 0, 1);
            toSet.setPositionFin(GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(points[0], points[1])));

        } else if (endX == null || endY == null) {
            final double[] points = new double[] {endX, endY};
            geoTransform.transform(points, 0, points, 0, 1);
            toSet.setPositionDebut(GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(points[0], points[1])));

        } else {
            final double[] points = new double[] {startX, startY, endX, endY};
            geoTransform.transform(points, 0, points, 0, 2);
            toSet.setPositionDebut(GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(points[0], points[1])));
            toSet.setPositionFin(GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(points[2], points[3])));
        }
    }

    /*
     * Utilities
     */

    /**
     * Convert a {@link Date} to a {@link LocalDate}.
     * @param date Date to convert. If null, a null value is returned.
     * @return The converted date, or null if no input was given.
     */
    public static LocalDate toLocalDate(final Date date) {
        return date == null? null : LocalDate.from(date.toInstant());
    }


    /**
     * Convert a {@link Date} to a {@link LocalDateTime}.
     * @param date Date to convert. If null, a null value is returned.
     * @return The converted date and time, or null if no input was given.
     */
    public static LocalDateTime toLocalDateTime(final Date date) {
        return date == null? null : LocalDateTime.from(date.toInstant());
    }
}
