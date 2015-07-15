package fr.sirs.importer.troncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefProprietaire;
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
class TypeProprietaireImporter extends GenericTypeReferenceImporter<RefProprietaire> {

    TypeProprietaireImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_TYPE_PROPRIETAIRE,
        LIBELLE_TYPE_PROPRIETAIRE,
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
        return TYPE_PROPRIETAIRE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefProprietaire typeProprietaire = createAnonymValidElement(RefProprietaire.class);
            
            typeProprietaire.setId(typeProprietaire.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_PROPRIETAIRE.toString())));
            typeProprietaire.setLibelle(row.getString(Columns.LIBELLE_TYPE_PROPRIETAIRE.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeProprietaire.setDateMaj(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            typeProprietaire.setDesignation(String.valueOf(row.getInt(String.valueOf(Columns.ID_TYPE_PROPRIETAIRE.toString()))));
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_PROPRIETAIRE.toString())), typeProprietaire);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
