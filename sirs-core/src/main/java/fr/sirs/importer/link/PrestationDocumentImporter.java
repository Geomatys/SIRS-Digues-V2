package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Document;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.Prestation;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.objet.prestation.PrestationImporter;
import fr.sirs.importer.theme.document.DocumentImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class PrestationDocumentImporter extends GenericEntityLinker {

    private final PrestationImporter prestationImporter;
    private final DocumentImporter documentImporter;
    
    public PrestationDocumentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final PrestationImporter prestationImporter,
            final DocumentImporter documentImporter) {
        super(accessDatabase, couchDbConnector);
        this.prestationImporter = prestationImporter;
        this.documentImporter = documentImporter;
    }

    private enum Columns {
        ID_PRESTATION,
        ID_DOC,
//        DATE_DERNIERE_MAJ
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
        return DbImporter.TableName.PRESTATION_DOCUMENT.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Prestation> prestations = prestationImporter.getById();
        final Map<Integer, Document> documents = documentImporter.getDocuments();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final Prestation prestation = prestations.get(row.getInt(Columns.ID_PRESTATION.toString()));
            final Document document = documents.get(row.getInt(Columns.ID_DOC.toString()));
            
            if(prestation!=null && document!=null){
                prestation.getDocument().add(document.getId());
                document.getPrestation().add(prestation.getId());
            }
        }
    }
}
