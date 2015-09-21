/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.document;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.valueOf;
import fr.sirs.importer.v2.ImportContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * Bind managed document class to their ID in access document type table.
 * @author Alexis Manin (Geomatys)
 */
@Component
public class DocTypeRegistry {

    private final String tableName = "TYPE_DOCUMENT";

    private final HashMap<Integer, Class> docTypes = new HashMap<>(4);

    private enum Columns {
        ID_TYPE_DOCUMENT,
        NOM_TABLE_EVT
    }

    @Autowired
    private DocTypeRegistry(ImportContext context) throws IOException {
        Iterator<Row> iterator = context.inputDb.getTable(tableName).iterator();

        while (iterator.hasNext()) {
            final Row row = iterator.next();
                        try {
                final Class clazz;
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
                    clazz = ArticleJournal.class; break;
//                case SYS_EVT_MARCHE:
//                    classe = .class; break;
//                case SYS_EVT_PLAN_TOPO:
//                    classe = .class; break;
                    case SYS_EVT_PROFIL_EN_LONG:
                        clazz = ProfilLong.class;
                        break;
                    case SYS_EVT_PROFIL_EN_TRAVERS:
                        clazz = ProfilTravers.class;
                        break;
                    case SYS_EVT_RAPPORT_ETUDES:
                        clazz = RapportEtude.class;
                        break;
////                case SYS_EVT_SONDAGE: // N'existe pas
                    default:
                        clazz = null;
                }

                docTypes.put(row.getInt(Columns.ID_TYPE_DOCUMENT.name()), clazz);
            } catch (IllegalArgumentException e) {
                SirsCore.LOGGER.log(Level.FINE, e.getMessage());
            }
        }
    }

    /**
     * @param typeId An id found in {@link Columns#ID_TYPE_DOCUMENT} column.
     * @return document class associated to given document type ID, or null, if
     * given Id is unknown.
     */
    public Class getDocType(final Integer typeId) {
        return docTypes.get(typeId);
    }

    public Class getDocType(final Row input) {
        Integer docTypeId = input.getInt(Columns.ID_TYPE_DOCUMENT.toString());
        if (docTypeId != null) {
            return getDocType(docTypeId);
        }
        return null;
    }
}
