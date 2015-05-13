package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefEcoulement;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class EcoulementImporter extends GenericTypeReferenceImporter<RefEcoulement> {
    
    EcoulementImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_ECOULEMENT,
        LIBELLE_ECOULEMENT,
        ABREGE_ECOULEMENT,
        DATE_DERNIERE_MAJ
    };
    
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
        return ECOULEMENT.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefEcoulement typeEcoulement = createAnonymValidElement(RefEcoulement.class);
            
            typeEcoulement.setId(typeEcoulement.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_ECOULEMENT.toString())));
            typeEcoulement.setLibelle(row.getString(Columns.LIBELLE_ECOULEMENT.toString()));
            typeEcoulement.setAbrege(row.getString(Columns.ABREGE_ECOULEMENT.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeEcoulement.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeEcoulement.setDesignation(String.valueOf(row.getInt(String.valueOf(Columns.ID_ECOULEMENT.toString()))));
            
            types.put(row.getInt(String.valueOf(Columns.ID_ECOULEMENT.toString())), typeEcoulement);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
