package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefOuvrageVoirie;
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
class TypeOuvrageVoirieImporter extends GenericTypeReferenceImporter<RefOuvrageVoirie> {
    
    TypeOuvrageVoirieImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_OUVRAGE_VOIRIE,
        LIBELLE_TYPE_OUVRAGE_VOIRIE,
        ABREGE_TYPE_OUVRAGE_VOIRIE,
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
        return TYPE_OUVRAGE_VOIRIE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefOuvrageVoirie typeOuvrage = createAnonymValidElement(RefOuvrageVoirie.class);
            
            typeOuvrage.setId(typeOuvrage.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_OUVRAGE_VOIRIE.toString())));
            typeOuvrage.setLibelle(row.getString(Columns.LIBELLE_TYPE_OUVRAGE_VOIRIE.toString()));
            typeOuvrage.setAbrege(row.getString(Columns.ABREGE_TYPE_OUVRAGE_VOIRIE.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeOuvrage.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeOuvrage.setDesignation(String.valueOf(row.getInt(String.valueOf(Columns.ID_TYPE_OUVRAGE_VOIRIE.toString()))));
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_OUVRAGE_VOIRIE.toString())), typeOuvrage);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
