package fr.sirs.importer.documentTroncon.document.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.GeometryFactory;
import fr.sirs.core.model.PointLeve;
import fr.sirs.core.model.PointLeveXYZ;
import fr.sirs.core.model.XYZLeveProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
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
import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.util.FactoryException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class ProfilTraversPointXYZImporter extends GenericImporter {

    private Map<Integer, XYZLeveProfilTravers> points = null;
    private Map<Integer, List<XYZLeveProfilTravers>> pointsByLeve = null;
    
    ProfilTraversPointXYZImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public Map<Integer, XYZLeveProfilTravers> getLeveePoints() throws IOException, AccessDbImporterException{
        if(points==null) compute();
        return points;
    }
    
    public Map<Integer, List<XYZLeveProfilTravers>> getLeveePointByLeveId() throws IOException, AccessDbImporterException{
        if(pointsByLeve==null) compute();
        return pointsByLeve;
    }
    
    private enum Columns {
        ID_PROFIL_EN_TRAVERS_LEVE,
        ID_POINT,
        X,
        Y,
        Z,
//        DATE_DERNIERE_MAJ // Pas dans le nouveau modèle
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
        return DbImporter.TableName.PROFIL_EN_TRAVERS_XYZ.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        points = new HashMap<>();
        pointsByLeve = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final XYZLeveProfilTravers levePoint = new XYZLeveProfilTravers();
            
            
            
            
            
//        GeometryFactory geometryFactory = new GeometryFactory();
//        final MathTransform lambertToRGF;
//        try {
//            lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);
//
//            try {
//
//                if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
//                    Point x = (Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
//                            row.getDouble(Columns.X_DEBUT.toString()),
//                            row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
//                }
//            } catch (MismatchedDimensionException | TransformException ex) {
//                Logger.getLogger(SysEvtCreteImporter.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//            try {
//
//                if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
//                    crete.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
//                            row.getDouble(Columns.X_FIN.toString()),
//                            row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
//                }
//            } catch (MismatchedDimensionException | TransformException ex) {
//                Logger.getLogger(SysEvtCreteImporter.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        } catch (FactoryException ex) {
//            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
//        }

            
            
            
            
            if (row.getDouble(Columns.X.toString()) != null) {
                levePoint.setX(row.getDouble(Columns.X.toString()).doubleValue());
            }
            
            if (row.getDouble(Columns.Y.toString()) != null) {
                levePoint.setY(row.getDouble(Columns.Y.toString()).doubleValue());
            }
            
            
            
            
            if (row.getDouble(Columns.Z.toString()) != null) {
                levePoint.setZ(row.getDouble(Columns.Z.toString()).doubleValue());
            }
            
            levePoint.setDesignation(String.valueOf(row.getInt(Columns.ID_POINT.toString())));
            levePoint.setValid(true);
            
            points.put(row.getInt(Columns.ID_POINT.toString()), levePoint);
            
            List<XYZLeveProfilTravers> listByLeve = pointsByLeve.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()));
            if (listByLeve == null) {
                listByLeve = new ArrayList<>();
                pointsByLeve.put(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()), listByLeve);
            }
            listByLeve.add(levePoint);
        }
    }
}
