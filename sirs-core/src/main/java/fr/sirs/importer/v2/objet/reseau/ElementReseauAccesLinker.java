package fr.sirs.importer.v2.objet.reseau;

import fr.sirs.importer.v2.*;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SessionCore;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.OuvertureBatardable;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Autowired;

import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_POINT_ACCES;
import org.springframework.stereotype.Component;

/**
 *
 * Create links between object using an MS-Access join table.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public abstract class ElementReseauAccesLinker {

    private enum Columns {

        ID_ELEMENT_RESEAU,
        ID_ELEMENT_RESEAU_POINT_ACCES
    }

    @Autowired
    protected ImportContext context;

    @Autowired
    protected SessionCore session;

    @Autowired
    ReseauRegistry registry;

    public void compute() throws AccessDbImporterException, IOException {
        Iterator<Row> iterator = context.inputDb.getTable(ELEMENT_RESEAU_POINT_ACCES.name()).iterator();

        final AbstractImporter<ObjetReseau> reseauImporter = context.importers.get(ObjetReseau.class);
        if (reseauImporter == null) {
            throw new AccessDbImporterException("No importer found for type " + ObjetReseau.class.getCanonicalName());
        }

        final AbstractSIRSRepository<OuvrageFranchissement> ouvrageRepo = session.getRepositoryForClass(OuvrageFranchissement.class);
        if (ouvrageRepo == null) {
            throw new AccessDbImporterException("No repository available to get/update objects of type " + OuvrageFranchissement.class.getCanonicalName());
        }

        final HashSet<Element> toUpdate = new HashSet<>();

        String ouvrageId, reseauId;
        OuvrageFranchissement ouvrage;
        Class elementType;
        ObjetReseau objetReseau;
        AbstractSIRSRepository<ObjetReseau> reseauRepo;
        Row current;
        while (iterator.hasNext()) {

            // Split execution in bulks
            while (iterator.hasNext() && toUpdate.size() < context.bulkLimit) {
                current = iterator.next();

                // Those fields should be SQL join table keys, so they should never be null.
                ouvrageId = reseauImporter.getImportedId(current.getInt(Columns.ID_ELEMENT_RESEAU.name()));
                reseauId = reseauImporter.getImportedId(current.getInt(Columns.ID_ELEMENT_RESEAU_POINT_ACCES.name()));
                elementType = registry.getElementType(current);
                if (ouvrageId == null) {
                    context.reportError(new ErrorReport(null, current, ELEMENT_RESEAU_POINT_ACCES.name(), Columns.ID_ELEMENT_RESEAU.name(), null, null, "No imported object found for input Id.", CorruptionLevel.ROW));
                    continue;
                } else if (reseauId == null) {
                    context.reportError(new ErrorReport(null, current, ELEMENT_RESEAU_POINT_ACCES.name(), Columns.ID_ELEMENT_RESEAU_POINT_ACCES.name(), null, null, "No imported object found for input Id.", CorruptionLevel.ROW));
                    continue;
                }

                ouvrage = ouvrageRepo.get(ouvrageId);
                reseauRepo = session.getRepositoryForClass(elementType);
                objetReseau = reseauRepo.get(reseauId);

                if (link(ouvrage, objetReseau)) {
                    toUpdate.add(ouvrage);
                    toUpdate.add(objetReseau);
                }
            }

            context.executeBulk(toUpdate);
            toUpdate.clear();
        }
    }

    private boolean link(final OuvrageFranchissement ouvrage, final ObjetReseau elementReseau) {
        if (elementReseau instanceof VoieDigue) {
            final VoieDigue voieDigue = (VoieDigue) elementReseau;
            voieDigue.getOuvrageFranchissementIds().add(ouvrage.getId());
            ouvrage.getVoieDigueIds().add(voieDigue.getId());
        } else if (elementReseau instanceof OuvertureBatardable) {
            final OuvertureBatardable ouvertureBatardable = (OuvertureBatardable) elementReseau;
            ouvertureBatardable.getOuvrageFranchissementIds().add(ouvrage.getId());
            ouvrage.getOuvertureBatardableIds().add(ouvertureBatardable.getId());
        } else if (elementReseau instanceof OuvrageVoirie) {
            final OuvrageVoirie ouvrageVoirie = (OuvrageVoirie) elementReseau;
            ouvrageVoirie.getOuvrageFranchissementIds().add(ouvrage.getId());
            ouvrage.getOuvrageVoirieIds().add(ouvrageVoirie.getId());
        } else if (elementReseau instanceof VoieAcces) {
            final VoieAcces voieAcces = (VoieAcces) elementReseau;
            voieAcces.getOuvrageFranchissementIds().add(ouvrage.getId());
            ouvrage.getVoieAccesIds().add(voieAcces.getId());
        } else {
            return false;
        }
        return true;
    }
}
