package fr.sirs.importer.v2.objet;

import fr.sirs.importer.v2.*;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SessionCore;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AbstractPositionDocumentAssociable;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Autowired;

import static fr.sirs.importer.DbImporter.TableName.PRESTATION_DOCUMENT;
import org.springframework.stereotype.Component;

/**
 *
 * Create links between object using an MS-Access join table.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class PrestationDocumentLinker {

    private enum Columns {

        ID_PRESTATION,
        ID_DOC
    }

    @Autowired
    protected ImportContext context;

    @Autowired
    protected SessionCore session;

    public void compute() throws AccessDbImporterException, IOException {
        Iterator<Row> iterator = context.inputDb.getTable(PRESTATION_DOCUMENT.name()).iterator();

        final AbstractImporter<Prestation> prestationImporter = context.importers.get(Prestation.class);
        if (prestationImporter == null) {
            throw new AccessDbImporterException("No importer found for type " + Prestation.class.getCanonicalName());
        }

        final AbstractImporter<AbstractPositionDocumentAssociable> docImporter = context.importers.get(AbstractPositionDocumentAssociable.class);
        if (docImporter == null) {
            throw new AccessDbImporterException("No importer found for type " + AbstractPositionDocumentAssociable.class.getCanonicalName());
        }

        final AbstractSIRSRepository<Prestation> prestationRepo = session.getRepositoryForClass(Prestation.class);
        if (prestationRepo == null) {
            throw new AccessDbImporterException("No repository available to get/update objects of type " + Prestation.class.getCanonicalName());
        }

        final HashSet<Element> toUpdate = new HashSet<>();

        String prestationId, docId;
        Prestation prestation;
        Element posDoc;
        Row current;
        while (iterator.hasNext()) {

            // Split execution in bulks
            while (iterator.hasNext() && toUpdate.size() < context.bulkLimit) {
                current = iterator.next();

                // Those fields should be SQL join table keys, so they should never be null.
                prestationId = prestationImporter.getImportedId(current.getInt(Columns.ID_PRESTATION.name()));
                docId = docImporter.getImportedId(current.getInt(Columns.ID_DOC.name()));
                if (prestationId == null) {
                    context.reportError(new ErrorReport(null, current, PRESTATION_DOCUMENT.name(), Columns.ID_PRESTATION.name(), null, null, "No imported object found for input Id.", CorruptionLevel.ROW));
                    continue;
                } else if (docId == null) {
                    context.reportError(new ErrorReport(null, current, PRESTATION_DOCUMENT.name(), Columns.ID_DOC.name(), null, null, "No imported object found for input Id.", CorruptionLevel.ROW));
                    continue;
                }

                prestation = prestationRepo.get(prestationId);
                posDoc = session.getElement(docId).orElse(null);

                if (posDoc instanceof AbstractPositionDocumentAssociable) {
                    link(prestation, (AbstractPositionDocumentAssociable) posDoc, toUpdate);
                }
            }

            context.executeBulk(toUpdate);
            toUpdate.clear();
        }
    }

    private void link(final Prestation p, final AbstractPositionDocumentAssociable doc, final HashSet<Element> updates) {
        final String realDocId = doc.getSirsdocument();
        if (realDocId == null) {
            return;
        }

        final Element realDoc = session.getElement(realDocId).orElse(null);
        if (realDoc instanceof DocumentGrandeEchelle) {
            final DocumentGrandeEchelle documentGrandeEchelle = (DocumentGrandeEchelle) realDoc;
            p.getDocumentGrandeEchelleIds().add(realDocId);
            updates.add(p);
        } else if (realDoc instanceof RapportEtude) {
            final RapportEtude rapport = (RapportEtude) realDoc;
            rapport.getPrestationIds().add(p.getId());
            p.getRapportEtudeIds().add(rapport.getId());
            updates.add(p);
            updates.add(rapport);
        }
    }
}
