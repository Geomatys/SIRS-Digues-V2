package fr.sirs.importer.v2.linear;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.AbstractImporter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.shapefile.shp.ShapeHandler;
import org.geotoolkit.data.shapefile.shp.ShapeType;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.LinearReferencing;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class TronconGestionDigueImporter extends AbstractImporter<TronconDigue> {

    private static final String GEOM_TABLE = "CARTO_TRONCON_GESTION_DIGUE";
    private static final String GEOM_COLUMN = "SHAPE";

    private Column idColumn;
    private Cursor geometryCursor;

    enum Columns {
        ID_TRONCON_GESTION,
        ID_SYSTEME_REP_DEFAUT
    };

    @Override
    public Class<TronconDigue> getElementClass() {
        return TronconDigue.class;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_TRONCON_GESTION.name();
    }

    @Override
    public String getTableName() {
        return TRONCON_GESTION_DIGUE.toString();
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();

        try {
            final Table t = context.inputCartoDb.getTable(GEOM_TABLE);
            geometryCursor = t.getDefaultCursor();
            idColumn = t.getColumn(getRowIdFieldName());
        } catch (Exception e) {
            throw new AccessDbImporterException("Cannot get geometry data for TronconDigue.", e);
        }
    }

    @Override
    protected void postCompute() {
        super.postCompute();
        geometryCursor = null;
        idColumn = null;
    }

    @Override
    public TronconDigue importRow(Row row, TronconDigue tronconDigue) throws IOException, AccessDbImporterException {
        tronconDigue = super.importRow(row, tronconDigue);

        final Object tronconId = row.get(getRowIdFieldName());
        if (tronconId == null) {
            throw new AccessDbImporterException("No id set in current row !");
        }

        tronconDigue.setGeometry(computeGeometry(tronconId, null));

        return tronconDigue;
    }

    /**
     * Search for the last valid geometry which has been submitted for a given {@link TronconDigue}
     *
     * @param tronconId Id of the source {@link TronconDigue } we want a geometry for.
     * @return A line string registered for the given object, or null if we cannot find any.
     */
    private LineString computeGeometry(final Object tronconId, final Cursor.Savepoint previousPosition) throws IOException {
        // Only take the last submitted geometry for the object.
        Cursor.Savepoint savepoint = null;
        Row currentRow = null;
        synchronized (geometryCursor) {
            if (previousPosition == null)
                geometryCursor.afterLast();
            else
                geometryCursor.restoreSavepoint(previousPosition);

            while (geometryCursor.moveToPreviousRow()) {
                if (geometryCursor.currentRowMatches(idColumn, tronconId)) {
                    currentRow = geometryCursor.getCurrentRow();
                    savepoint = geometryCursor.getSavepoint();
                    break;
                }
            }
        }

        LineString result = null;
        if (currentRow != null) {
            try {
                result = readGeometry(currentRow.getBytes(GEOM_COLUMN));
            } catch (Exception e) {
                context.reportError(GEOM_TABLE, currentRow, e, "A geometry cannot be read.");
            }

            if (result == null && savepoint != null) {
                result = computeGeometry(tronconId, savepoint);
            }
        }

        return result;
    }

    /**
     * Read input byte array to build a geometry. Input must be a valid WKB geometry.
     * If read geometry is not a {@link LineString}, we try to convert it using {@link  LinearReferencing#asLineString(com.vividsolutions.jts.geom.Geometry) }.
     * @param bytes The array containing WKB geometry.
     * @return A line string, or null if we cannot convert read geometry into line.
     * @throws DataStoreException If an error occurs while reading WKB geometry.
     * @throws MismatchedDimensionException If we cannot project input geometry into output database CRS.
     * @throws TransformException If we cannot project input geometry into output database CRS.
     */
    private LineString readGeometry(final byte[] bytes) throws DataStoreException, MismatchedDimensionException, TransformException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        final ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        final int id = bb.getInt();
        final ShapeType shapeType = ShapeType.forID(id);
        final ShapeHandler handler = shapeType.getShapeHandler(false);
        final Geometry tmpGeometry = JTS.transform((Geometry) handler.read(bb, shapeType), context.geoTransform);
        final LineString geom = LinearReferencing.asLineString(tmpGeometry);
        if (geom != null)
            return geom;

        return null;
    }
}
