package fr.sirs.importer.documentTroncon.document.documentAGrandeEchelle;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefDocumentGrandeEchelle;
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
class TypeDocumentAGrandeEchelleImporter extends GenericTypeReferenceImporter<RefDocumentGrandeEchelle> {

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
        return TYPE_DOCUMENT_A_GRANDE_ECHELLE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            final RefDocumentGrandeEchelle typeDocument = createAnonymValidElement(RefDocumentGrandeEchelle.class);
            
            typeDocument.setId(typeDocument.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE.toString())));
            typeDocument.setLibelle(row.getString(Columns.LIBELLE_TYPE_DOCUMENT_A_GRANDE_ECHELLE.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeDocument.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            typeDocument.setDesignation(String.valueOf(row.getInt(Columns.ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE.toString())));
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE.toString())), typeDocument);
            
        }
        couchDbConnector.executeBulk(types.values());
    }
}
