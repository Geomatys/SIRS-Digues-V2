package fr.sirs.importer.theme.document;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefDocumentGrandeEchelle;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericTypeImporter;
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
class TypeDocumentAGrandeEchelleImporter extends GenericTypeImporter<RefDocumentGrandeEchelle> {

    TypeDocumentAGrandeEchelleImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE,
        LIBELLE_TYPE_DOCUMENT_A_GRANDE_ECHELLE,
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
        return DbImporter.TableName.TYPE_DOCUMENT_A_GRANDE_ECHELLE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            final RefDocumentGrandeEchelle typeDocument = new RefDocumentGrandeEchelle();
            
            typeDocument.setLibelle(row.getString(Columns.LIBELLE_TYPE_DOCUMENT_A_GRANDE_ECHELLE.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeDocument.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE.toString())), typeDocument);
            
        }
        couchDbConnector.executeBulk(types.values());
    }
}
