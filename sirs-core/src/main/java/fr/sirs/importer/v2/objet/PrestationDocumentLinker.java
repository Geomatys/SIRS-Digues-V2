/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.importer.v2.objet;

import fr.sirs.importer.v2.*;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SessionCore;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.AbstractPositionDocumentAssociable;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Autowired;

import static fr.sirs.importer.DbImporter.TableName.PRESTATION_DOCUMENT;
import java.util.HashMap;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.springframework.stereotype.Component;

/**
 *
 * Create links between object using an MS-Access join table.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class PrestationDocumentLinker implements Linker<AbstractPositionDocument, Prestation>, WorkMeasurable {

    private enum Columns {

        ID_PRESTATION,
        ID_DOC
    }

    @Autowired
    protected ImportContext context;

    @Autowired
    protected SessionCore session;

    private final SimpleIntegerProperty count = new SimpleIntegerProperty(0);

    @Override
    public Class<Prestation> getHolderClass() {
        return Prestation.class;
    }

    @Override
    public Class<AbstractPositionDocument> getTargetClass() {
        return AbstractPositionDocument.class;
    }

    public void link() throws AccessDbImporterException, IOException {
        Iterator<Row> iterator = context.inputDb.getTable(PRESTATION_DOCUMENT.name()).iterator();

        final AbstractImporter<Prestation> prestationImporter = context.importers.get(Prestation.class);
        if (prestationImporter == null) {
            throw new AccessDbImporterException("No importer found for type " + Prestation.class.getCanonicalName());
        }

        final AbstractImporter<AbstractPositionDocument> docImporter = context.importers.get(AbstractPositionDocument.class);
        if (docImporter == null) {
            throw new AccessDbImporterException("No importer found for type " + AbstractPositionDocumentAssociable.class.getCanonicalName());
        }

        final AbstractSIRSRepository<Prestation> prestationRepo = session.getRepositoryForClass(Prestation.class);
        if (prestationRepo == null) {
            throw new AccessDbImporterException("No repository available to get/update objects of type " + Prestation.class.getCanonicalName());
        }

        final HashMap<String, Element> toUpdate = new HashMap<>();

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
                posDoc = toUpdate.getOrDefault(docId, session.getElement(docId).orElse(null));

                if (posDoc instanceof AbstractPositionDocumentAssociable) {
                    linkDocument(prestation, (AbstractPositionDocumentAssociable) posDoc, toUpdate);
                }
            }

            context.executeBulk(toUpdate.values());
            toUpdate.clear();
        }
        count.set(1);
    }

    @Override
    public int getTotalWork() {
        return 1;
    }

    @Override
    public IntegerProperty getWorkDone() {
        return count;
    }

    private void linkDocument(final Prestation p, final AbstractPositionDocumentAssociable doc, final HashMap<String, Element> updates) {
        final String realDocId = doc.getSirsdocument();
        if (realDocId == null) {
            return;
        }

        final Element realDoc = session.getElement(realDocId).orElse(null);
        if (realDoc instanceof DocumentGrandeEchelle) {
            p.getDocumentGrandeEchelleIds().add(realDocId);
            updates.put(p.getId(), p);
        } else if (realDoc instanceof RapportEtude) {
            final RapportEtude rapport = (RapportEtude) realDoc;
            rapport.getPrestationIds().add(p.getId());
            p.getRapportEtudeIds().add(rapport.getId());
            updates.put(p.getId(), p);
            updates.put(rapport.getId(), rapport);
        }
    }
}
