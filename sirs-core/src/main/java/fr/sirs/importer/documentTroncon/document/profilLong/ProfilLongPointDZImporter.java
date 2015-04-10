package fr.sirs.importer.documentTroncon.document.profilLong;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.PointLeve;
import fr.sirs.core.model.PointLeveDZ;
import fr.sirs.core.model.PointLeveXYZ;
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
class ProfilLongPointDZImporter extends GenericImporter {

    private Map<Integer, PointLeveDZ> points = null;
    private Map<Integer, List<PointLeveDZ>> pointsByProfil = null;
    
    ProfilLongPointDZImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public Map<Integer, PointLeveDZ> getLeveePoints() throws IOException, AccessDbImporterException{
        if(points==null) compute();
        return points;
    }
    
    public Map<Integer, List<PointLeveDZ>> getLeveePointByProfilId() throws IOException, AccessDbImporterException{
        if(pointsByProfil==null) compute();
        return pointsByProfil;
    }
    
    private enum Columns {
        ID_PROFIL_EN_LONG,
        ID_POINT,
        PR_SAISI,
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
        return DbImporter.TableName.PROFIL_EN_LONG_DZ.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        points = new HashMap<>();
        pointsByProfil = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final PointLeveDZ levePoint = new PointLeveDZ();
            
            if (row.getDouble(Columns.PR_CALCULE.toString()) != null) {
                levePoint.setD(row.getDouble(Columns.PR_CALCULE.toString()).doubleValue());
            }
            
            if (row.getDouble(Columns.Z.toString()) != null) {
                levePoint.setZ(row.getDouble(Columns.Z.toString()).doubleValue());
            }
            
            levePoint.setDesignation(String.valueOf(row.getInt(Columns.ID_POINT.toString())));
            levePoint.setValid(true);
            points.put(row.getInt(Columns.ID_POINT.toString()), levePoint);
            
            List<PointLeveDZ> listByProfil = pointsByProfil.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()));
            if (listByProfil == null) {
                listByProfil = new ArrayList<>();
                pointsByProfil.put(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()), listByProfil);
            }
            listByProfil.add(levePoint);
        }
    }
}
