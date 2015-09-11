package fr.sirs.importer.objet.reseau;

import fr.sirs.core.SirsCore;
import java.util.logging.Level;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.OuvertureBatardable;
import fr.sirs.core.model.OuvrageFranchissement;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.StationPompage;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericTypeInternalImporter;
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
public class TypeElementReseauImporter extends GenericTypeInternalImporter<Class> {

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
        return TYPE_ELEMENT_RESEAU.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            try {
                final Class classe;
                final DbImporter.TableName table = valueOf(row.getString(Columns.NOM_TABLE_EVT.toString()));
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
                    case SYS_EVT_OUVRAGE_VOIRIE:
                        classe = OuvrageVoirie.class;
                        break;
                    case SYS_EVT_RESEAU_EAU:
                        classe = ReseauHydrauliqueCielOuvert.class;
                        break;
                    case SYS_EVT_OUVRAGE_PARTICULIER:
                        classe = OuvrageParticulier.class;
                        break;
                    case SYS_EVT_OUVERTURE_BATARDABLE:
                        classe = OuvertureBatardable.class;
                        break;
                    default:
                        classe = null;
                }
                types.put(row.getInt(String.valueOf(Columns.ID_TYPE_ELEMENT_RESEAU.toString())), classe);
            } catch (IllegalArgumentException e) {
                SirsCore.LOGGER.log(Level.FINE, e.getMessage());
            }
        }
    }
}
