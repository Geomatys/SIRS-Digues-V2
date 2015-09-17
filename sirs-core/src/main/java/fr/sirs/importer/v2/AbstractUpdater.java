/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Identifiable;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An importer designed for document uupdate. The aim is to create and fill an
 * element (of type T) which is not a CouchDB document, but a sub-structure of a
 * document. Once filled, this element will be added to its parent document.
 * Finally, the parent document will be updated.
 *
 * @author Alexis Manin (Geomatys)
 *
 * @param <T> Type of the element which will be created and filled by this importer
 * @param <U> Type of the document which will be updated with computed element.
 */
public abstract class AbstractUpdater<T extends Element, U extends Element> extends AbstractImporter<T> {

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
     * Once a row has been computed and its parent retrieved, we ask implementation
     * to make the binding between the two objects.
     * @param container The parent document to update.
     * @param toPut The computed element to bind with its parent.
     */
    public abstract void put(final U container, final T toPut);

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

    protected U getDocument(final Row input) {
        final Integer accessDocId = input.getInt(getDocumentIdField());
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

    @Override
    protected synchronized void compute() throws IOException, AccessDbImporterException {
                if (importedRows != null)
            return;

        preCompute();

        final Table table = context.inputDb.getTable(getTableName());
        mappers.clear();
        mappers.addAll(context.getCompatibleMappers(table, getElementClass()));

        // In case we want to boost import with multi-threading.
        importedRows = new ConcurrentHashMap<>();

        /*
         * Import entire table  content. We split import in packets to avoid memory overload.
         */
        try {
            final Iterator<Row> it = table.iterator();
            while (it.hasNext()) {
                final ConcurrentHashMap<Integer, T> imports = new ConcurrentHashMap<>();
                /*
                 * Contains documents to update. Once it goes over context bulk limit,
                 * we proceed to update, then clear it and restart.
                 */
                final HashSet<U> toUpdate = new HashSet();

                while (it.hasNext() && toUpdate.size() < context.bulkLimit) {
                    final Row row = it.next();
                    final Integer rowId = row.getInt(getRowIdFieldName());
                    if (rowId == null) {
                        context.reportError(new ErrorReport(null, row, getTableName(), getRowIdFieldName(), null, null, "Cannot import row due to bad ID.", CorruptionLevel.ROW));
                        continue;
                    }
                    // TODO : error management and report
                    T output = getOrCreateElement(row);
                    if (output == null) {
                        continue;
                    }
                    output.setDesignation(rowId.toString());
                    output = importRow(row, output);
                    imports.put(rowId, output);

                    // Once a new element is computed, we add it in its parent.
                    final U document = getDocument(row);
                    put(document, output);
                    toUpdate.add(document);
                }

                Map<String, Object> bulkResult = context.executeBulk(imports.values());

                /*
                 * We keep a binding between all original rows and output objects using
                 * their Ids. We check that bulk has succeeded for an object before creating
                 * the binding.
                 */
                for (final Map.Entry<Integer, T> entry : imports.entrySet()) {
                    final Identifiable i = entry.getValue();
                    final String id = i.getId();
                    if (id != null && bulkResult.containsKey(id)) {
                        importedRows.put(entry.getKey(), id);
                    }
                }
            }
        } finally {
            postCompute();
            mappers.clear();
        }
    }
}
