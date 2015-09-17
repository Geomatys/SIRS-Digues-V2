package fr.sirs.importer.documentTroncon.document.profilLong;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.PrZProfilLong;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.AbstractImporter;
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
class ProfilLongPointDZImporter extends DocumentImporter {

    private Map<Integer, PrZProfilLong> points = null;
    private Map<Integer, List<PrZProfilLong>> pointsByProfil = null;
    
    ProfilLongPointDZImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public Map<Integer, PrZProfilLong> getLeveePoints() throws IOException, AccessDbImporterException{
        if(points==null) compute();
        return points;
    }
    
    public Map<Integer, List<PrZProfilLong>> getLeveePointByProfilId() throws IOException, AccessDbImporterException{
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
        return PROFIL_EN_LONG_DZ.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        points = new HashMap<>();
        pointsByProfil = new HashMap<>();
        
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final PrZProfilLong levePoint = createAnonymValidElement(PrZProfilLong.class);
            
            if (row.getDouble(Columns.PR_CALCULE.toString()) != null) {
                levePoint.setD(row.getDouble(Columns.PR_CALCULE.toString()).doubleValue());
            }
            
            if (row.getDouble(Columns.Z.toString()) != null) {
                levePoint.setZ(row.getDouble(Columns.Z.toString()).doubleValue());
            }
            
            levePoint.setDesignation(String.valueOf(row.getInt(Columns.ID_POINT.toString())));
            
            points.put(row.getInt(Columns.ID_POINT.toString()), levePoint);
            
            List<PrZProfilLong> listByProfil = pointsByProfil.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()));
            if (listByProfil == null) {
                listByProfil = new ArrayList<>();
                pointsByProfil.put(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()), listByProfil);
            }
            listByProfil.add(levePoint);
        }
    }
}
