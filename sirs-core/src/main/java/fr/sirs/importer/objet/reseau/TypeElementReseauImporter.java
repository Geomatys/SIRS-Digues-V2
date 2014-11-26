package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.core.model.LargeurFrancBord;
import fr.sirs.core.model.ProfilFrontFrancBord;
import fr.sirs.core.model.StationPompage;
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
class TypeElementReseauImporter extends GenericImporter {

    private Map<Integer, Class> typesElementReseau = null;

    TypeElementReseauImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum TypeElementReseauColumns {
        ID_TYPE_ELEMENT_RESEAU,
//        LIBELLE_TYPE_ELEMENT_RESEAU,
        NOM_TABLE_EVT,
//        ID_TYPE_OBJET_CARTO,
//        DATE_DERNIERE_MAJ
    };

    /**
     *
     * @return A map containing all the database types of Geometry elements
     * (classes) referenced by their internal ID.
     * @throws IOException
     */
    public Map<Integer, Class> getTypeElementStructure() throws IOException {
        if (typesElementReseau == null) {
            compute();
        }
        return typesElementReseau;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeElementReseauColumns c : TypeElementReseauColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_ELEMENT_RESEAU.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesElementReseau = new HashMap<>();
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            try {
                final Class classe;
                final DbImporter.TableName table = DbImporter.TableName.valueOf(row.getString(TypeElementReseauColumns.NOM_TABLE_EVT.toString()));
                switch (table) {
                    case SYS_EVT_STATION_DE_POMPAGE:
                        classe = StationPompage.class;
                        break;
//                    case 2:
//                        classe = ProfilFrontFrancBord.class;
//                        break;
//                    case 3:
//                        classe = Distance.class;
//                        break;
                    default:
                        classe = null;
                }
                typesElementReseau.put(row.getInt(String.valueOf(TypeElementReseauColumns.ID_TYPE_ELEMENT_RESEAU.toString())), classe);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
