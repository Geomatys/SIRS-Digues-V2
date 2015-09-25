package fr.sirs.importer.documentTroncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Convention;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.SYS_EVT_CONVENTION;
import static fr.sirs.importer.DbImporter.TableName.TYPE_DOCUMENT;
import static fr.sirs.importer.DbImporter.TableName.valueOf;
import fr.sirs.importer.documentTroncon.document.TypeDocumentImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class AotCotTypeDocumentImporter extends TypeDocumentImporter {

    public AotCotTypeDocumentImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_TYPE_DOCUMENT,
        LIBELLE_TYPE_DOCUMENT,
//        ID_TYPE_GENERAL_DOCUMENT, // Ignoré dans le nouveau modèle
        NOM_TABLE_EVT,
//        ID_TYPE_OBJET_CARTO,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database types of Document elements 
     * (classes) referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, Class> getClasseDocument() throws IOException {
        if(classesDocument == null) compute();
        return classesDocument;
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
        return TYPE_DOCUMENT.toString();
    }

    @Override
    protected void compute() throws IOException {
        classesDocument = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            try {
                final Class classe;
                final DbImporter.TableName table = valueOf(row.getString(Columns.NOM_TABLE_EVT.toString()));
                switch (table) {
                    case SYS_EVT_CONVENTION:
                        classe = Convention.class;
                        break;
                    default:
                        classe = null;
                }
                
                classesDocument.put(row.getInt(String.valueOf(Columns.ID_TYPE_DOCUMENT.toString())), classe);
            } catch (IllegalArgumentException e) {
                SirsCore.LOGGER.log(Level.FINE, e.getMessage());
            }
        }
    }
}
