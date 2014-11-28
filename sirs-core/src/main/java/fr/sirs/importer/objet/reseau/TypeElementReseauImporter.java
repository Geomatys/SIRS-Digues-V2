package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.StationPompage;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
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

    private Map<Integer, Class> types = null;

    TypeElementReseauImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
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
    public Map<Integer, Class> getTypes() throws IOException {
        if (types == null) {
            compute();
        }
        return types;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
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
        types = new HashMap<>();
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            try {
                final Class classe;
                final DbImporter.TableName table = DbImporter.TableName.valueOf(row.getString(Columns.NOM_TABLE_EVT.toString()));
                switch (table) {
                    case SYS_EVT_STATION_DE_POMPAGE:
                        classe = StationPompage.class;
                        break;
                    case SYS_EVT_CONDUITE_FERMEE:
                        classe = ReseauHydrauliqueFerme.class;
                        break;
                    case SYS_EVT_AUTRE_OUVRAGE_HYDRAULIQUE:
                        classe = OuvrageHydrauliqueAssocie.class;
                        break;
                    case SYS_EVT_RESEAU_TELECOMMUNICATION:
                        classe = ReseauTelecomEnergie.class;
                        break;
                    case SYS_EVT_OUVRAGE_TELECOMMUNICATION:
                        classe = OuvrageTelecomEnergie.class;
                        break;
                    case SYS_EVT_CHEMIN_ACCES:
                        classe = VoieAcces.class;
                        break;
                    case SYS_EVT_POINT_ACCES:
                        classe = OuvrageFranchissement.class;
                        break;
                    case SYS_EVT_VOIE_SUR_DIGUE:
                        classe = VoieDigue.class;
                        break;
//                    case 3:
//                        classe = Distance.class;
//                        break;
                    default:
                        classe = null;
                }
                types.put(row.getInt(String.valueOf(Columns.ID_TYPE_ELEMENT_RESEAU.toString())), classe);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
