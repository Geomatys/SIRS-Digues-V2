/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
            throw new IllegalStateException("Input has no valid ID in " + getDocumentIdField());
        }

        try {
            final String docId = masterImporter.getImportedId(accessDocId);
            return masterRepository.get(docId);
        } catch (Exception ex) {
            throw new IllegalStateException("No imported object found for row " + input.getInt(getRowIdFieldName()) + " from table " + getTableName(), ex);
        }
    }
}
