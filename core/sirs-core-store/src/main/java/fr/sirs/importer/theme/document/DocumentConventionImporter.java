package fr.sirs.importer.theme.document;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.Document;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DocumentConventionImporter extends GenericImporter {

    private Map<Integer, Document> documents = null;
    
    DocumentConventionImporter(Database accessDatabase, CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    @Override
    public List<String> getUsedColumns() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getTableName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
