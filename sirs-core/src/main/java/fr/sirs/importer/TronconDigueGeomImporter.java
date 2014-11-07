package fr.sirs.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Geometry;
import fr.sirs.core.model.TronconDigue;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.storage.DataStoreException;
import org.ektorp.CouchDbConnector;
import org.geotoolkit.data.shapefile.shp.ShapeHandler;
import org.geotoolkit.data.shapefile.shp.ShapeType;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TronconDigueGeomImporter extends GenericImporter {
    
    private Map<Integer, Geometry> tronconDigueGeom = null;

    TronconDigueGeomImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (CartoTronconGestionDigueColumns c : CartoTronconGestionDigueColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
    
    private enum CartoTronconGestionDigueColumns {
        ID_TRONCON_GESTION, 
        SHAPE,
//        OBJECTID,
//        SHAPE_Length,
//        LONGUEUR
    };

    /**
     * 
     * @return A map containing all the geometries referenced by their internal 
     * troncon ID.
     * @throws IOException 
     */
    public Map<Integer, Geometry> getTronconDigueGeoms() throws IOException {
        if(tronconDigueGeom==null) compute();
        return tronconDigueGeom;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.CARTO_TRONCON_GESTION_DIGUE.toString();
    }

    @Override
    protected void compute() throws IOException {
        tronconDigueGeom = new HashMap<>();
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            try {
                final Row row = it.next();
                final TronconDigue tronconDigue = new TronconDigue();

                final byte[] bytes = row.getBytes(CartoTronconGestionDigueColumns.SHAPE.toString());
                final ByteBuffer bb = ByteBuffer.wrap(bytes);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                final int id = bb.getInt();
                final ShapeType shapeType = ShapeType.forID(id);
                final ShapeHandler handler = shapeType.getShapeHandler(false);
                Geometry geom = (Geometry) handler.read(bb, shapeType);

                final MathTransform lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"),true);
                geom = JTS.transform(geom, lambertToRGF);

                tronconDigueGeom.put(row.getInt(String.valueOf(CartoTronconGestionDigueColumns.ID_TRONCON_GESTION.toString())),
                        geom);
            } catch (FactoryException | DataStoreException | MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(DbImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
