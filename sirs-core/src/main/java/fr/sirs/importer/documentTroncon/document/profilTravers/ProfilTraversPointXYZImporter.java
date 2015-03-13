package fr.sirs.importer.documentTroncon.document.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.PointLeve;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class ProfilTraversPointXYZImporter extends GenericImporter {

    private Map<Integer, PointLeve> points = null;
    private Map<Integer, List<PointLeve>> pointsByLeve = null;
    
    ProfilTraversPointXYZImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public Map<Integer, PointLeve> getLeveePoints() throws IOException, AccessDbImporterException{
        if(points==null) compute();
        return points;
    }
    
    public Map<Integer, List<PointLeve>> getLeveePointByLeveId() throws IOException, AccessDbImporterException{
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
            final PointLeve levePoint = new PointLeve();
            
            if (row.getDouble(Columns.X.toString()) != null) {
                levePoint.setX(row.getDouble(Columns.X.toString()).doubleValue());
            }
            
            if (row.getDouble(Columns.Y.toString()) != null) {
                levePoint.setY(row.getDouble(Columns.Y.toString()).doubleValue());
            }
            
            if (row.getDouble(Columns.Z.toString()) != null) {
                levePoint.setZ(row.getDouble(Columns.Z.toString()).doubleValue());
            }
            
            levePoint.setD(Double.NaN);
            levePoint.setPseudoId(String.valueOf(row.getInt(Columns.ID_POINT.toString())));
            levePoint.setValid(true);
            
            points.put(row.getInt(Columns.ID_POINT.toString()), levePoint);
            
            List<PointLeve> listByLeve = pointsByLeve.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()));
            if (listByLeve == null) {
                listByLeve = new ArrayList<>();
                pointsByLeve.put(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()), listByLeve);
            }
            listByLeve.add(levePoint);
        }
    }
}
