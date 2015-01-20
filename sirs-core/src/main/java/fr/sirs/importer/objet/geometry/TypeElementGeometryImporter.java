package fr.sirs.importer.objet.geometry;

import fr.sirs.core.SirsCore;
import java.util.logging.Level;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.DbImporter;
import fr.sirs.core.model.LargeurFrancBord;
import fr.sirs.core.model.ProfilFrontFrancBord;
import fr.sirs.importer.GenericTypeImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeElementGeometryImporter extends GenericTypeImporter<Class> {

    TypeElementGeometryImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_ELEMENT_GEOMETRIE,
        LIBELLE_TYPE_ELEMENT_GEOMETRIE,
//        NOM_TABLE_EVT,
//        ID_TYPE_OBJET_CARTO,
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
        return DbImporter.TableName.TYPE_ELEMENT_GEOMETRIE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            try {
                final Class classe;
                // La colonne NOM_TABLE_EVT étant vide dans la table de l'Isère, 
                // on gère la correspondance en dur en espérant que toutes les 
                //bases font le même lien !
//                final DbImporter.TableName table = DbImporter.TableName.valueOf(row.getString(TypeElementGeometryColumns.NOM_TABLE_EVT.toString()));
                final int table = (int) row.getInt(Columns.ID_TYPE_ELEMENT_GEOMETRIE.toString());
                switch (table) {
                    case 1:
                        classe = LargeurFrancBord.class;
                        break;
                    case 2:
                        classe = ProfilFrontFrancBord.class;
                        break;
//                    case 3:
//                        classe = Distance.class;
//                        break;
                    default:
                        classe = null;
                }
                types.put(row.getInt(String.valueOf(Columns.ID_TYPE_ELEMENT_GEOMETRIE.toString())), classe);
            } catch (IllegalArgumentException e) {
                SirsCore.LOGGER.log(Level.FINE, e.getMessage());
            }
        }
    }
}
