package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefOuvrageParticulier;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeOuvrageParticulierImporter extends GenericTypeReferenceImporter<RefOuvrageParticulier> {
    
    TypeOuvrageParticulierImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_OUVRAGE_PARTICULIER,
        LIBELLE_TYPE_OUVRAGE_PARTICULIER,
        ABREGE_TYPE_OUVRAGE_PARTICULIER,
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
        return TYPE_OUVRAGE_PARTICULIER.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefOuvrageParticulier typeOuvrage = createAnonymValidElement(RefOuvrageParticulier.class);
            
            typeOuvrage.setId(typeOuvrage.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_OUVRAGE_PARTICULIER.toString())));
            typeOuvrage.setLibelle(row.getString(Columns.LIBELLE_TYPE_OUVRAGE_PARTICULIER.toString()));
            typeOuvrage.setAbrege(row.getString(Columns.ABREGE_TYPE_OUVRAGE_PARTICULIER.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeOuvrage.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeOuvrage.setDesignation(String.valueOf(row.getInt(String.valueOf(Columns.ID_TYPE_OUVRAGE_PARTICULIER.toString()))));
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_OUVRAGE_PARTICULIER.toString())), typeOuvrage);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
