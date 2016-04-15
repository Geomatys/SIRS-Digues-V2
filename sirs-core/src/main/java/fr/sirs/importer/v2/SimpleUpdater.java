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
package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Element;
import fr.sirs.importer.AccessDbImporterException;

/**
 * An importer designed for document update. The aim is to create and fill an
 * element (of type T) which is not a CouchDB document, but a sub-structure of a
 * document. Once filled, this element will be added to its parent document.
 * Finally, the parent document will be updated.
 *
 * @author Alexis Manin (Geomatys)
 *
 * @param <T> Type of the element which will be created and filled by this importer
 * @param <U> Type of the document which will be updated with computed element.
 */
public abstract class SimpleUpdater<T extends Element, U extends Element> extends AbstractUpdater<T, U> {

    /**
     * The importer used for computing documents we want to update here.
     */
    protected AbstractImporter<U> masterImporter;

    /**
     * Repository used for getting documents we want to update.
     */
    protected AbstractSIRSRepository<U> masterRepository;

    /**
     *
     * @return Name of the column which contains Ids of the documents to update.
     */
    public abstract String getDocumentIdField();

    /**
     *
     * @return Class representing the type of document to update.
     */
    public abstract Class<U> getDocumentClass();

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        final Class<U> docClass = getDocumentClass();
        masterImporter = context.importers.get(docClass);
        if (masterImporter == null) {
            throw new IllegalStateException("Cannot find any importer for type : " + docClass);
        }

        masterRepository = session.getRepositoryForClass(docClass);
        if (masterRepository == null) {
            throw new IllegalStateException("No repository found to read elements of type : " + docClass);
        }
    }

    @Override
    protected U getDocument(final Object rowId, final Row input, T output) {
        final Object accessDocId = input.get(getDocumentIdField());
        if (accessDocId == null) {
            context.reportError(new ErrorReport(null, input, getTableName(), getDocumentIdField(), output, null, "Cannot import a document due to bad foreign key.", CorruptionLevel.ROW));
            return null;
        }

        try {
            final String docId = masterImporter.getImportedId(accessDocId);
            return masterRepository.get(docId);
        } catch (Exception ex) {
            context.reportError(new ErrorReport(null, input, masterImporter.getTableName(), getDocumentIdField(), output, null, "No imported object found for row " + accessDocId + " from table " + masterImporter.getTableName(), CorruptionLevel.ROW));
            return null;
        }
    }
}
