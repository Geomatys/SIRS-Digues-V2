package fr.sirs.importer.documentTroncon.document.profilLong;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.XYZProfilLong;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ektorp.CouchDbConnector;
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
class ProfilLongPointXYZImporter extends GenericImporter {

    private Map<Integer, XYZProfilLong> points = null;
    private Map<Integer, List<XYZProfilLong>> pointsByProfil = null;
    
    ProfilLongPointXYZImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public Map<Integer, XYZProfilLong> getLeveePoints() throws IOException, AccessDbImporterException{
        if(points==null) compute();
        return points;
    }
    
    public Map<Integer, List<XYZProfilLong>> getLeveePointByProfilId() throws IOException, AccessDbImporterException{
        if(pointsByProfil==null) compute();
        return pointsByProfil;
    }
    
    private enum Columns {
        ID_PROFIL_EN_LONG,
        ID_POINT,
        X,
        Y,
        Z,
        PR_CALCULE,
        DATE_DERNIERE_MAJ
    }
    
    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return PROFIL_EN_LONG_XYZ.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        points = new HashMap<>();
        pointsByProfil = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final XYZProfilLong levePoint = createAnonymValidElement(XYZProfilLong.class);
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

                try {

                    if (row.getDouble(Columns.X.toString()) != null && row.getDouble(Columns.Y.toString()) != null) {
                        Point point = (Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X.toString()),
                                row.getDouble(Columns.Y.toString()))), lambertToRGF);
                        levePoint.setX(point.getX());
                        levePoint.setY(point.getY());
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, null, ex);
            }
            
            if (row.getDouble(Columns.Z.toString()) != null) {
                levePoint.setZ(row.getDouble(Columns.Z.toString()));
            }
            
            levePoint.setDesignation(String.valueOf(row.getInt(Columns.ID_POINT.toString())));
            
            points.put(row.getInt(Columns.ID_POINT.toString()), levePoint);
            
            List<XYZProfilLong> listByProfil = pointsByProfil.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()));
            if (listByProfil == null) {
                listByProfil = new ArrayList<>();
                pointsByProfil.put(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()), listByProfil);
            }
            listByProfil.add(levePoint);
        }
    }
}
