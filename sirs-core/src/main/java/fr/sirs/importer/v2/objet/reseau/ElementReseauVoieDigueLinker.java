package fr.sirs.importer.v2.objet.reseau;

import fr.sirs.importer.v2.*;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SessionCore;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Autowired;

import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_VOIE_SUR_DIGUE;
import org.springframework.stereotype.Component;

/**
 *
 * Create links between object using an MS-Access join table.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ElementReseauVoieDigueLinker implements Linker<VoieDigue, ObjetReseau> {

    @Override
    public Class<VoieDigue> getTargetClass() {
        return VoieDigue.class;
    }

    @Override
    public Class<ObjetReseau> getHolderClass() {
        return ObjetReseau.class;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_ELEMENT_RESEAU_VOIE_SUR_DIGUE
    }

    @Autowired
    protected ImportContext context;

    @Autowired
    protected SessionCore session;

    public void link() throws AccessDbImporterException, IOException {
        Iterator<Row> iterator = context.inputDb.getTable(ELEMENT_RESEAU_VOIE_SUR_DIGUE.name()).iterator();

        final AbstractImporter<VoieDigue> voieDigueImporter = context.importers.get(VoieDigue.class);
        if (voieDigueImporter == null) {
            throw new AccessDbImporterException("No importer found for type " + VoieDigue.class.getCanonicalName());
        }

        final AbstractImporter<ObjetReseau> reseauImporter = context.importers.get(ObjetReseau.class);
        if (reseauImporter == null) {
            throw new AccessDbImporterException("No importer found for type " + ObjetReseau.class.getCanonicalName());
        }

        final AbstractSIRSRepository<VoieDigue> voieDigueRepo = session.getRepositoryForClass(VoieDigue.class);
        if (voieDigueRepo == null) {
            throw new AccessDbImporterException("No repository available to get/update objects of type " + VoieDigue.class.getCanonicalName());
        }

        final HashSet<Element> toUpdate = new HashSet<>();

        String voieDigueId, reseauId;
        VoieDigue voieDigue;
        Class elementType;
        Element objetReseau;
        AbstractSIRSRepository<ObjetReseau> reseauRepo;
        Row current;
        while (iterator.hasNext()) {

            // Split execution in bulks
            while (iterator.hasNext() && toUpdate.size() < context.bulkLimit) {
                current = iterator.next();

                // Those fields should be SQL join table keys, so they should never be null.
                voieDigueId = voieDigueImporter.getImportedId(current.getInt(Columns.ID_ELEMENT_RESEAU_VOIE_SUR_DIGUE.name()));
                reseauId = reseauImporter.getImportedId(current.getInt(Columns.ID_ELEMENT_RESEAU.name()));
                if (voieDigueId == null) {
                    context.reportError(new ErrorReport(null, current, ELEMENT_RESEAU_VOIE_SUR_DIGUE.name(), Columns.ID_ELEMENT_RESEAU_VOIE_SUR_DIGUE.name(), null, null, "No imported object found for input Id.", CorruptionLevel.ROW));
                    continue;
                } else if (reseauId == null) {
                    context.reportError(new ErrorReport(null, current, ELEMENT_RESEAU_VOIE_SUR_DIGUE.name(), Columns.ID_ELEMENT_RESEAU.name(), null, null, "No imported object found for input Id.", CorruptionLevel.ROW));
                    continue;
                }

                voieDigue = voieDigueRepo.get(voieDigueId);
                objetReseau = session.getElement(reseauId).orElse(null);

                link(voieDigue, objetReseau, toUpdate);
            }

            context.executeBulk(toUpdate);
            toUpdate.clear();
            context.linkCount.incrementAndGet();
        }
    }

    private void link(final VoieDigue voie, final Element elementReseau, final HashSet<Element> updates) {
        if (elementReseau instanceof ReseauHydrauliqueFerme) {
            final ReseauHydrauliqueFerme reseauFerme = (ReseauHydrauliqueFerme) elementReseau;
            reseauFerme.getReseauHydrauliqueCielOuvertIds().add(voie.getId());
            updates.add(reseauFerme);
        } else if (elementReseau instanceof OuvrageFranchissement) {
            final OuvrageFranchissement ouvrageVoirie = (OuvrageFranchissement) elementReseau;
            ouvrageVoirie.getOuvrageVoirieIds().add(voie.getId());
            voie.getOuvrageFranchissementIds().add(ouvrageVoirie.getId());
            updates.add(voie);
            updates.add(ouvrageVoirie);
        }
    }
}
