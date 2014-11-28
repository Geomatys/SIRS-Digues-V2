package fr.sirs.importer.theme.document.related.profilLong;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.LeveePoints;
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
class ProfilLongPointXYZImporter extends GenericImporter {

    private Map<Integer, LeveePoints> points = null;
    private Map<Integer, List<LeveePoints>> pointsByProfil = null;
    
    ProfilLongPointXYZImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public Map<Integer, LeveePoints> getLeveePoints() throws IOException, AccessDbImporterException{
        if(points==null) compute();
        return points;
    }
    
    public Map<Integer, List<LeveePoints>> getLeveePointByProfilId() throws IOException, AccessDbImporterException{
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
        return DbImporter.TableName.PROFIL_EN_LONG_XYZ.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        points = new HashMap<>();
        pointsByProfil = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final LeveePoints leveePoint = new LeveePoints();
            
            if (row.getDouble(Columns.X.toString()) != null) {
                leveePoint.setX(row.getDouble(Columns.X.toString()).doubleValue());
            }
            
            if (row.getDouble(Columns.Y.toString()) != null) {
                leveePoint.setY(row.getDouble(Columns.Y.toString()).doubleValue());
            }
            
            if (row.getDouble(Columns.Z.toString()) != null) {
                leveePoint.setZ(row.getDouble(Columns.Z.toString()).doubleValue());
            }
            
            leveePoint.setD(Double.NaN);
            
            points.put(row.getInt(Columns.ID_POINT.toString()), leveePoint);
            
            List<LeveePoints> listByProfil = pointsByProfil.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()));
            if (listByProfil == null) {
                listByProfil = new ArrayList<>();
                pointsByProfil.put(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()), listByProfil);
            }
            listByProfil.add(leveePoint);
        }
    }
}
