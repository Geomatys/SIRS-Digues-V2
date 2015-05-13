package fr.sirs.importer.objet.structure;

import fr.sirs.core.SirsCore;
import java.util.logging.Level;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Epi;
import fr.sirs.core.model.Fondation;
import fr.sirs.core.model.FrontFrancBord;
import fr.sirs.core.model.OuvrageRevanche;
import fr.sirs.core.model.PiedDigue;
import fr.sirs.core.model.PiedFrontFrancBord;
import fr.sirs.core.model.SommetRisberme;
import fr.sirs.core.model.TalusDigue;
import fr.sirs.core.model.TalusRisberme;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericTypeInternalImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeElementStructureImporter extends GenericTypeInternalImporter<Class> {

    TypeElementStructureImporter(final Database accessDatabase) {
        super(accessDatabase, null);
    }

    private enum Columns {
        ID_TYPE_ELEMENT_STRUCTURE,
        //        LIBELLE_TYPE_ELEMENT_STRUCTURE,
        NOM_TABLE_EVT,
        //        ID_TYPE_OBJET_CARTO,
        DATE_DERNIERE_MAJ
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
        return TYPE_ELEMENT_STRUCTURE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            try {
                final Class classe;
                final DbImporter.TableName table = valueOf(row.getString(Columns.NOM_TABLE_EVT.toString()));
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
                    case SYS_EVT_TALUS_FRANC_BORD:
                        classe = FrontFrancBord.class; 
                        break;
                    case SYS_EVT_PIED_FRONT_FRANC_BORD:
                        classe = PiedFrontFrancBord.class; 
                        break;
                    case SYS_EVT_FONDATION:
                        classe = Fondation.class;
                        break;
//                case SYS_EVT_OUVRAGE_PARTICULIER: // Les ouvrages particuliers sont en fait des éléments de réseau
//                    classe = OuvrageParticulier.class; break;
//                    case SYS_EVT_BRISE_LAME: // Les brise-lames n'existent pas dans le modèle car ils feront partie d'un module "ouvrages à la mer" en 2015
//                        classe = BriseLame.class; break;
                    case SYS_EVT_EPIS:
                        classe = Epi.class;
                        break;
//                    case SYS_EVT_VEGETATION: // La végétation est un module à part.
//                        classe = Vegetation.class; break;
                    case SYS_EVT_OUVRAGE_REVANCHE:
                        classe = OuvrageRevanche.class;
                        break;
                    default:
                        // Dans la table de l'Isère, on trouve SYS_EVT_FRONT_FRANC_BORD qui n'existe pas et qui semble correspondre à SYS_EVT_TALUS_FRANC_BORD
                        if("SYS_EVT_FRONT_FRANC_BORD".equals(row.getString(Columns.NOM_TABLE_EVT.toString()))){
                            classe = FrontFrancBord.class;
                        }
                        else {
                            System.out.println(row.getString(Columns.NOM_TABLE_EVT.toString()));
                            classe = null;
                        }
                }
                types.put(row.getInt(String.valueOf(Columns.ID_TYPE_ELEMENT_STRUCTURE.toString())), classe);
            } catch (IllegalArgumentException e) {
                SirsCore.LOGGER.log(Level.FINE, e.getMessage());
            }
        }
    }
}
