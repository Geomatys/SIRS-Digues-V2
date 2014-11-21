package fr.sirs.importer.objet.structure;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Epi;
import fr.sirs.core.model.Fondation;
import fr.sirs.core.model.OuvrageRevanche;
import fr.sirs.core.model.PiedDigue;
import fr.sirs.core.model.SommetRisberme;
import fr.sirs.core.model.TalusDigue;
import fr.sirs.core.model.TalusRisberme;
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
class TypeElementStructureImporter extends GenericImporter {

    private Map<Integer, Class> typesElementStructure = null;

    TypeElementStructureImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum TypeElementStructureColumns {

        ID_TYPE_ELEMENT_STRUCTURE,
        //        LIBELLE_TYPE_ELEMENT_STRUCTURE,
        NOM_TABLE_EVT,
        //        ID_TYPE_OBJET_CARTO,
        DATE_DERNIERE_MAJ
    };

    /**
     *
     * @return A map containing all the database types of Structure elements
     * (classes) referenced by their internal ID.
     * @throws IOException
     */
    public Map<Integer, Class> getTypeElementStructure() throws IOException {
        if (typesElementStructure == null) {
            compute();
        }
        return typesElementStructure;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeElementStructureColumns c : TypeElementStructureColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_ELEMENT_STRUCTURE.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesElementStructure = new HashMap<>();
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            try {
                final Class classe;
                final DbImporter.TableName table = DbImporter.TableName.valueOf(row.getString(TypeElementStructureColumns.NOM_TABLE_EVT.toString()));
                switch (table) {
                    case SYS_EVT_CRETE:
                        classe = Crete.class;
                        break;
                    case SYS_EVT_TALUS_DIGUE:
                        classe = TalusDigue.class;
                        break;
                    case SYS_EVT_SOMMET_RISBERME:
                        classe = SommetRisberme.class;
                        break;
                    case SYS_EVT_TALUS_RISBERME:
                        classe = TalusRisberme.class;
                        break;
                    case SYS_EVT_PIED_DE_DIGUE:
                        classe = PiedDigue.class;
                        break;
//                    case 6:
//                        classe = LargeurFrancBord.class; break;
//                    case 7:
//                        classe = LargeurFrancBord.class; break;
                    case SYS_EVT_FONDATION:
                        classe = Fondation.class;
                        break;
//                case SYS_EVT_OUVRAGE_PARTICULIER:
//                    classe = OuvrageParticulier.class; break;
//                    case 10:
//                        classe = BriseLame.class; break;
                    case SYS_EVT_EPIS:
                        classe = Epi.class;
                        break;
//                    case 12:
//                        classe = Vegetation.class; break;
                    case SYS_EVT_OUVRAGE_REVANCHE:
                        classe = OuvrageRevanche.class;
                        break;
                    default:
                        classe = null;
                }
                typesElementStructure.put(row.getInt(String.valueOf(TypeElementStructureColumns.ID_TYPE_ELEMENT_STRUCTURE.toString())), classe);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
