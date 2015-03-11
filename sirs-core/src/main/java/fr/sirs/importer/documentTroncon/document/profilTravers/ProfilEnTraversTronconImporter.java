package fr.sirs.importer.documentTroncon.document.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.DocumentTroncon;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.documentTroncon.DocumentImporter;
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
public class ProfilEnTraversTronconImporter extends GenericImporter {

    private Map<Integer, DocumentTroncon[]> documentTronconsByLeve = null;
    private DocumentImporter documentImporter;
    
    private ProfilEnTraversTronconImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    ProfilEnTraversTronconImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final DocumentImporter documentImporter) {
        this(accessDatabase, couchDbConnector);
        this.documentImporter = documentImporter;
    }
    
    public Map<Integer, DocumentTroncon[]> getDocumentTronconsByLeveId() throws IOException, AccessDbImporterException{
        if(documentTronconsByLeve==null) compute();
        return documentTronconsByLeve;
    }
    
    private enum Columns {
        ID_PROFIL_EN_TRAVERS_LEVE,
        ID_DOC,
//        COTE_RIVIERE_Z_NGF_PIED_DE_DIGUE,
//        COTE_RIVIERE_Z_NGF_SOMMET_RISBERME,
//        CRETE_Z_NGF,
//        COTE_TERRE_Z_NGF_SOMMET_RISBERME,
//        COTE_TERRE_Z_NGF_PIED_DE_DIGUE,
//        DATE_DERNIERE_MAJ,
//        CRETE_LARGEUR
    }
    
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
        return DbImporter.TableName.PROFIL_EN_TRAVERS_TRONCON.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        documentTronconsByLeve = new HashMap<>();
        
        final Map<Integer, DocumentTroncon> documents = documentImporter.getPrecomputedDocuments();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            DocumentTroncon[] docTroncons = documentTronconsByLeve.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()));
            if(docTroncons==null){
                docTroncons = new DocumentTroncon[2];
                docTroncons[0]=documents.get(row.getInt(Columns.ID_DOC.toString()));//row.getInt(Columns.ID_DOC.toString());
                documentTronconsByLeve.put(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()), docTroncons);
            }
            else{
                docTroncons[1]=documents.get(row.getInt(Columns.ID_DOC.toString()));//row.getInt(Columns.ID_DOC.toString());
            }
        }
    }
}
