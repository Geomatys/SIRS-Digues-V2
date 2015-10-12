package fr.sirs.importer.v2.objet.reseau;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.EchelleLimnimetrique;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.OuvertureBatardable;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.StationPompage;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.TYPE_ELEMENT_RESEAU;
import static fr.sirs.importer.DbImporter.TableName.valueOf;
import fr.sirs.importer.v2.ImportContext;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ReseauRegistry {

    private enum Columns {
        ID_TYPE_ELEMENT_RESEAU,
        NOM_TABLE_EVT
    };

    private final HashMap<Object, Class<? extends ObjetReseau>> types = new HashMap<>(4);

    @Autowired
    private ReseauRegistry(ImportContext context) throws IOException {
        Iterator<Row> iterator = context.inputDb.getTable(TYPE_ELEMENT_RESEAU.name()).iterator();

        while (iterator.hasNext()) {
            final Row row = iterator.next();
            try {
                final Class clazz;
                final DbImporter.TableName table = valueOf(row.getString(Columns.NOM_TABLE_EVT.toString()));
                switch (table) {
                    case SYS_EVT_STATION_DE_POMPAGE:
                        clazz = StationPompage.class;
                        break;
                    case SYS_EVT_CONDUITE_FERMEE:
                        clazz = ReseauHydrauliqueFerme.class;
                        break;
                    case SYS_EVT_AUTRE_OUVRAGE_HYDRAULIQUE:
                        clazz = OuvrageHydrauliqueAssocie.class;
                        break;
                    case SYS_EVT_RESEAU_TELECOMMUNICATION:
                        clazz = ReseauTelecomEnergie.class;
                        break;
                    case SYS_EVT_OUVRAGE_TELECOMMUNICATION:
                        clazz = OuvrageTelecomEnergie.class;
                        break;
                    case SYS_EVT_CHEMIN_ACCES:
                        clazz = VoieAcces.class;
                        break;
                    case SYS_EVT_POINT_ACCES:
                        clazz = OuvrageFranchissement.class;
                        break;
                    case SYS_EVT_VOIE_SUR_DIGUE:
                        clazz = VoieDigue.class;
                        break;
                    case SYS_EVT_OUVRAGE_VOIRIE:
                        clazz = OuvrageVoirie.class;
                        break;
                    case SYS_EVT_RESEAU_EAU:
                        clazz = ReseauHydrauliqueCielOuvert.class;
                        break;
                    case SYS_EVT_OUVRAGE_PARTICULIER:
                        clazz = OuvrageParticulier.class;
                        break;
                    case SYS_EVT_OUVERTURE_BATARDABLE:
                        clazz = OuvertureBatardable.class;
                        break;
                    default:
                        clazz = null;
                }

                if (clazz == null) {
                    //context.reportError(new ErrorReport(null, row, TYPE_ELEMENT_RESEAU.name(), Columns.NOM_TABLE_EVT.name(), null, null, "Unrecognized wire type", null));
                } else {
                    types.put(row.get(Columns.ID_TYPE_ELEMENT_RESEAU.name()), clazz);
                }
            } catch (IllegalArgumentException e) {
                //context.reportError(new ErrorReport(null, row, TYPE_ELEMENT_RESEAU.name(), Columns.NOM_TABLE_EVT.name(), null, null, "Unrecognized wire type", null));
            }
        }
    }

    /**
     * Search for the document type to use for import of given row.
     * @param input The row to analyze
     * @return document class associated to given document type ID, or null if
     * given Id is unknown.
     */
    public Class<? extends ObjetReseau> getElementType(final Row input) {
        final Object typeId = input.get(Columns.ID_TYPE_ELEMENT_RESEAU.name());

        Class<? extends ObjetReseau> result = null;
        if (typeId != null) {
            result = types.get(typeId);
        }

        // Hack : If we've got an {@link OuvrageParticulier}, it can be an {@link EchelleLimnimetrique} in some cases.
        if (result != null && OuvrageParticulier.class.isAssignableFrom(result)) {
            final Object typeOuvrage = input.get(ElementReseauImporter.Columns.ID_TYPE_OUVRAGE_PARTICULIER.name());
            if ((typeOuvrage instanceof Number && ((Number)typeOuvrage).intValue() == 5)
                    || (typeOuvrage instanceof String && "5".equals(typeOuvrage))) {
                result = EchelleLimnimetrique.class;
            }
        }
        return result;
    }

    public Collection<Class<? extends ObjetReseau>> allTypes() {
        return types.values();
    }
}
