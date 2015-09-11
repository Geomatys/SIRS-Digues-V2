package fr.sirs.importer.documentTroncon.document.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.DZLeveProfilTravers;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
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
class ProfilTraversPointDZImporter extends DocumentImporter {

    private Map<Integer, DZLeveProfilTravers> points = null;
    private Map<Integer, List<DZLeveProfilTravers>> pointsByProfil = null;
    
    ProfilTraversPointDZImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public Map<Integer, DZLeveProfilTravers> getLeveePoints() throws IOException, AccessDbImporterException{
        if(points==null) compute();
        return points;
    }
    
    public Map<Integer, List<DZLeveProfilTravers>> getLeveePointByLeveId() throws IOException, AccessDbImporterException{
        if(pointsByProfil==null) compute();
        return pointsByProfil;
    }
    
    private enum Columns {
        ID_PROFIL_EN_TRAVERS_LEVE,
        ID_POINT,
        DISTANCE,
        Z,
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
        return PROFIL_EN_TRAVERS_DZ.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        points = new HashMap<>();
        pointsByProfil = new HashMap<>();
        
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final DZLeveProfilTravers levePoint = createAnonymValidElement(DZLeveProfilTravers.class);
            
            if (row.getDouble(Columns.DISTANCE.toString()) != null) {
                levePoint.setD(row.getDouble(Columns.DISTANCE.toString()));
            }
            
            if (row.getDouble(Columns.Z.toString()) != null) {
                levePoint.setZ(row.getDouble(Columns.Z.toString()));
            }
            
            levePoint.setDesignation(String.valueOf(row.getInt(Columns.ID_POINT.toString())));
            
            points.put(row.getInt(Columns.ID_POINT.toString()), levePoint);
            
            List<DZLeveProfilTravers> listByProfil = pointsByProfil.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()));
            if (listByProfil == null) {
                listByProfil = new ArrayList<>();
                pointsByProfil.put(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()), listByProfil);
            }
            listByProfil.add(levePoint);
        }
    }
}
