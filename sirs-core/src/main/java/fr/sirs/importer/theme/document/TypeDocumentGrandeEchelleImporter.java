package fr.sirs.importer.theme.document;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.core.model.RefDocumentGrandeEchelle;
import fr.sirs.core.model.RefTypeDocument;
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
public class TypeDocumentGrandeEchelleImporter extends GenericImporter {

    private Map<Integer, RefDocumentGrandeEchelle> typesDocument = null;

    TypeDocumentGrandeEchelleImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum TypeDocumentGrandeEchelleColumns {
        ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE,
        LIBELLE_TYPE_DOCUMENT_A_GRANDE_ECHELLE,
        DATE_DERNIERE_MAJ
    };
    
    /**
     * 
     * @return A map containing all the database types of Document elements 
     * (RefTypeDocument) referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefDocumentGrandeEchelle> getTypeDocumentGrandeEchelle() throws IOException {
        if(typesDocument == null) compute();
        return typesDocument;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeDocumentGrandeEchelleColumns c : TypeDocumentGrandeEchelleColumns.values()) {
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
        typesDocument = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            final RefDocumentGrandeEchelle typeDocument = new RefDocumentGrandeEchelle();
            
            typeDocument.setLibelle(row.getString(TypeDocumentGrandeEchelleColumns.LIBELLE_TYPE_DOCUMENT_A_GRANDE_ECHELLE.toString()));
            
            if (row.getDate(TypeDocumentGrandeEchelleColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeDocument.setDateMaj(LocalDateTime.parse(row.getDate(TypeDocumentGrandeEchelleColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            typesDocument.put(row.getInt(String.valueOf(TypeDocumentGrandeEchelleColumns.ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE.toString())), typeDocument);
            
        }
        couchDbConnector.executeBulk(typesDocument.values());
    }
}
