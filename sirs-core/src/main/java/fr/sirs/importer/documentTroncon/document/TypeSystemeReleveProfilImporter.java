package fr.sirs.importer.documentTroncon.document;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefSystemeReleveProfil;
import fr.sirs.importer.DbImporter;
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
public class TypeSystemeReleveProfilImporter extends GenericTypeReferenceImporter<RefSystemeReleveProfil> {

    public TypeSystemeReleveProfilImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_TYPE_SYSTEME_RELEVE_PROFIL,
        LIBELLE_TYPE_SYSTEME_RELEVE_PROFIL,
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
        return DbImporter.TableName.TYPE_SYSTEME_RELEVE_PROFIL.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefSystemeReleveProfil typeSystemeReleve = new RefSystemeReleveProfil();
            
            typeSystemeReleve.setId(typeSystemeReleve.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString())));
            typeSystemeReleve.setLibelle(row.getString(Columns.LIBELLE_TYPE_SYSTEME_RELEVE_PROFIL.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeSystemeReleve.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeSystemeReleve.setDesignation(String.valueOf(row.getInt(Columns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString())));
            typeSystemeReleve.setValid(true);
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString())), typeSystemeReleve);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
