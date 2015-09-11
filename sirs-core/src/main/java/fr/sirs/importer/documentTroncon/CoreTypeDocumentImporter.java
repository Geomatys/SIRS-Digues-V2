package fr.sirs.importer.documentTroncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.SYS_EVT_JOURNAL;
import static fr.sirs.importer.DbImporter.TableName.SYS_EVT_PROFIL_EN_LONG;
import static fr.sirs.importer.DbImporter.TableName.SYS_EVT_PROFIL_EN_TRAVERS;
import static fr.sirs.importer.DbImporter.TableName.SYS_EVT_RAPPORT_ETUDES;
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
public class CoreTypeDocumentImporter extends TypeDocumentImporter {


    public CoreTypeDocumentImporter(final Database accessDatabase,
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
        
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            
            try {
                final Class classe;
                final DbImporter.TableName table = valueOf(row.getString(Columns.NOM_TABLE_EVT.toString()));
                switch (table) {
//                    case SYS_EVT_CONVENTION:
//                        classe = Convention.class;
//                        break;
////                case SYS_EVT_COUPE_OUVRAGE: // N'existe pas
//                case SYS_EVT_DOCUMENT_MARCHE:
//                    classe = .class; break;
//                case SYS_EVT_FICHE_INSPECTION_VISUELLE:
//                    classe = .class; break;
                case SYS_EVT_JOURNAL:
                    classe = ArticleJournal.class; break;
//                case SYS_EVT_MARCHE:
//                    classe = .class; break;
//                case SYS_EVT_PLAN_TOPO:
//                    classe = .class; break;
                    case SYS_EVT_PROFIL_EN_LONG:
                        classe = ProfilLong.class;
                        break;
                    case SYS_EVT_PROFIL_EN_TRAVERS:
                        classe = ProfilTravers.class;
                        break;
                    case SYS_EVT_RAPPORT_ETUDES:
                        classe = RapportEtude.class;
                        break;
////                case SYS_EVT_SONDAGE: // N'existe pas
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
