package fr.sirs.importer.v2.objet.reseau;

import fr.sirs.importer.v2.*;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SessionCore;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Autowired;

import static fr.sirs.importer.DbImporter.TableName.DESORDRE_ELEMENT_RESEAU;

/**
 *
 * Create links between object using an MS-Access join table.
 *
 * @author Alexis Manin (Geomatys)
 */
public abstract class ElementReseauDesordreLinker {

    private enum Columns {
        ID_DESORDRE,
        ID_ELEMENT_RESEAU
    }

    @Autowired
    protected ImportContext context;

    @Autowired
    protected SessionCore session;

    @Autowired
    ReseauRegistry registry;

    public void compute() throws AccessDbImporterException, IOException {
        Iterator<Row> iterator = context.inputDb.getTable(DESORDRE_ELEMENT_RESEAU.name()).iterator();

        final AbstractImporter<ObjetReseau> reseauImporter = context.importers.get(ObjetReseau.class);
        if (reseauImporter == null) {
            throw new AccessDbImporterException("No importer found for type " + ObjetReseau.class.getCanonicalName());
        }

        final AbstractImporter<Desordre> desordreImporter = context.importers.get(Desordre.class);
        if (desordreImporter == null) {
            throw new AccessDbImporterException("No importer found for type " + Desordre.class.getCanonicalName());
        }

        final AbstractSIRSRepository<Desordre> desordreRepo = session.getRepositoryForClass(Desordre.class);
        if (desordreRepo == null) {
            throw new AccessDbImporterException("No repository available to get/update objects of type " + Desordre.class.getCanonicalName());
        }

        final HashSet<Element> toUpdate = new HashSet<>();

        String reseauId, desordreId;
        Class elementType;
        ObjetReseau objetReseau;
        Desordre desordre;
        Row current;
        AbstractSIRSRepository<ObjetReseau> reseauRepo;
        while (iterator.hasNext()) {

            // Split execution in bulks
            while (iterator.hasNext() && toUpdate.size() < context.bulkLimit) {
                current = iterator.next();

                // Those fields should be SQL join table keys, so they should never be null.
                reseauId = reseauImporter.getImportedId(current.getInt(Columns.ID_ELEMENT_RESEAU.name()));
                elementType = registry.getElementType(current);
                desordreId = desordreImporter.getImportedId(current.getInt(Columns.ID_DESORDRE.name()));
                if (reseauId == null) {
                    context.reportError(new ErrorReport(null, current, DESORDRE_ELEMENT_RESEAU.name(), Columns.ID_ELEMENT_RESEAU.name(), null, null, "No imported object found for input Id.", CorruptionLevel.ROW));
                    continue;
                } else if (desordreId == null) {
                    context.reportError(new ErrorReport(null, current, DESORDRE_ELEMENT_RESEAU.name(), Columns.ID_DESORDRE.name(), null, null, "No imported object found for input Id.", CorruptionLevel.ROW));
                    continue;
                }

                reseauRepo = session.getRepositoryForClass(elementType);
                objetReseau = reseauRepo.get(reseauId);
                desordre = desordreRepo.get(desordreId);
                if (link(objetReseau, desordre)) {
                    toUpdate.add(desordre);
                }
            }

            context.executeBulk(toUpdate);
            toUpdate.clear();
        }
    }

    private boolean link(final ObjetReseau elementReseau, final Desordre desordre) {
        if (elementReseau instanceof VoieDigue) {
            desordre.getVoieDigueIds().add(elementReseau.getId());
        } else if (elementReseau instanceof OuvrageParticulier) {
            desordre.getOuvrageParticulierIds().add(elementReseau.getId());
        } else if (elementReseau instanceof OuvrageHydrauliqueAssocie) {
            desordre.getOuvrageHydrauliqueAssocieIds().add(elementReseau.getId());
        } else if (elementReseau instanceof OuvrageTelecomEnergie) {
            desordre.getOuvrageTelecomEnergieIds().add(elementReseau.getId());
        } else if (elementReseau instanceof OuvrageVoirie) {
            desordre.getOuvrageVoirieIds().add(elementReseau.getId());
        } else if (elementReseau instanceof ReseauTelecomEnergie) {
            desordre.getReseauTelecomEnergieIds().add(elementReseau.getId());
        } else if (elementReseau instanceof ReseauHydrauliqueCielOuvert) {
            desordre.getReseauHydrauliqueCielOuvertIds().add(elementReseau.getId());
        } else if (elementReseau instanceof ReseauHydrauliqueFerme) {
            desordre.getReseauHydrauliqueFermeIds().add(elementReseau.getId());
        } else {
            return false;
        }
        return true;
    }
}
