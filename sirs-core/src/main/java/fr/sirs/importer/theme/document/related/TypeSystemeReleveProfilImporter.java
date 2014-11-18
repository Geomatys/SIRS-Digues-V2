package fr.sirs.importer.theme.document.related;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefSystemeReleveProfilRepository;
import fr.sirs.core.model.RefSystemeReleveProfil;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
import java.time.LocalDateTime;
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
public class TypeSystemeReleveProfilImporter extends GenericImporter {

    private Map<Integer, RefSystemeReleveProfil> typesSystemesReleveProfilsTravers = null;
    private final RefSystemeReleveProfilRepository refSystemeReleveProfilRepository;

    public TypeSystemeReleveProfilImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final RefSystemeReleveProfilRepository refSystemeReleveProfilRepository) {
        super(accessDatabase, couchDbConnector);
        this.refSystemeReleveProfilRepository = refSystemeReleveProfilRepository;
    }
    
    private enum TypeSystemeReleveProfilTraversColumns {
        ID_TYPE_SYSTEME_RELEVE_PROFIL,
        LIBELLE_TYPE_SYSTEME_RELEVE_PROFIL,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefSystemeReleveProfil referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefSystemeReleveProfil> getTypeSystemeReleve() throws IOException {
        if(typesSystemesReleveProfilsTravers == null) compute();
        return typesSystemesReleveProfilsTravers;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeSystemeReleveProfilTraversColumns c : TypeSystemeReleveProfilTraversColumns.values()) {
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
        typesSystemesReleveProfilsTravers = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefSystemeReleveProfil typeSystemeReleve = new RefSystemeReleveProfil();
            
            typeSystemeReleve.setLibelle(row.getString(TypeSystemeReleveProfilTraversColumns.LIBELLE_TYPE_SYSTEME_RELEVE_PROFIL.toString()));
            
            if (row.getDate(TypeSystemeReleveProfilTraversColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeSystemeReleve.setDateMaj(LocalDateTime.parse(row.getDate(TypeSystemeReleveProfilTraversColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesSystemesReleveProfilsTravers.put(row.getInt(String.valueOf(TypeSystemeReleveProfilTraversColumns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString())), typeSystemeReleve);
            refSystemeReleveProfilRepository.add(typeSystemeReleve);
        }
    }
}
