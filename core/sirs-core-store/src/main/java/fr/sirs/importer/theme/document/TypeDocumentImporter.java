package fr.sirs.importer.theme.document;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Convention;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
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
public class TypeDocumentImporter extends GenericImporter {

    private Map<Integer, Class> typesDocument = null;

    TypeDocumentImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum TypeDocumentColumns {
        ID_TYPE_DOCUMENT,
        LIBELLE_TYPE_DOCUMENT,
        ID_TYPE_GENERAL_DOCUMENT,
        NOM_TABLE_EVT,
        ID_TYPE_OBJET_CARTO,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database types of Document elements (classes) referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, Class> getTypeDocument() throws IOException {
        if(typesDocument == null) compute();
        return typesDocument;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeDocumentColumns c : TypeDocumentColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_DOCUMENT.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesDocument = new HashMap<>();
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            try{
            final Class classe;
            final DbImporter.TableName table = DbImporter.TableName.valueOf(row.getString(TypeDocumentColumns.NOM_TABLE_EVT.toString()));
            switch(table){
                case SYS_EVT_CONVENTION:
                    classe = Convention.class; break;
////                case SYS_EVT_COUPE_OUVRAGE:
////                    classe = TalusDigue.class; break;
//                case SYS_EVT_DOCUMENT_MARCHE:
//                    classe = .class; break;
//                case SYS_EVT_FICHE_INSPECTION_VISUELLE:
//                    classe = .class; break;
//                case SYS_EVT_JOURNAL:
//                    classe = .class; break;
//                case SYS_EVT_MARCHE:
//                    classe = .class; break;
//                case SYS_EVT_PLAN_TOPO:
//                    classe = .class; break;
//                case SYS_EVT_PROFIL_EN_LONG:
//                    classe = .class; break;
//                case SYS_EVT_PROFIL_EN_TRAVERS:
//                    classe = .class; break;
//                case SYS_EVT_RAPPORT_ETUDES:
//                    classe = .class; break;
////                case SYS_EVT_SONDAGE:
////                    classe = OuvrageRevanche.class; break;
                default:
                    classe = null;
            }
            typesDocument.put(row.getInt(String.valueOf(TypeDocumentColumns.ID_TYPE_DOCUMENT.toString())), classe);
            }catch (IllegalArgumentException e){
                System.out.println(e.getMessage());
            }
        }
    }
}
