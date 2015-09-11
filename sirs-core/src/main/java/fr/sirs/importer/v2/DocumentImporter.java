/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Identifiable;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public abstract class DocumentImporter<T extends Element> extends GenericImporter<T> {

    /**
     * Import a single row for current table.
     * @param row The row to import.
     * @param output The object to feed with input row value.
     * @return The object to insert in output database. Should be the same the output parameter.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public T importRow(final Row row, final T output)
            throws IOException, AccessDbImporterException {
        final Integer rowId = row.getInt(getRowIdFieldName());
        if (rowId != null) {
            output.setDesignation(rowId.toString());
        }
        return output;
    }

    /**
     * Compute the maps referencing the retrieved objects.
     *
     * TODO : make final ?
     *
     * @throws java.io.IOException
     * @throws fr.sirs.importer.AccessDbImporterException
     */
    protected synchronized void compute() throws IOException, AccessDbImporterException {
        if (importedRows != null)
            return;

        preCompute();

        // In case we want to boost import with multi-threading.
        importedRows = new ConcurrentHashMap<>();

        /*
         * Import entire table content. We split import in packets to avoid memory overload.
         */
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        final AtomicInteger bulkCount = new AtomicInteger();
        while (it.hasNext()) {
            final ConcurrentHashMap<Integer, T> imports = new ConcurrentHashMap<>();

            bulkCount.set(context.bulkLimit);
            while (it.hasNext() && bulkCount.getAndDecrement() > 0) {
                final Row row = it.next();
                final Integer rowId = row.getInt(getRowIdFieldName());
                if (rowId == null) {
                    context.reportError(new ErrorReport(null, row, getTableName(), getRowIdFieldName(), null, null, "Cannot import row due to bad ID.", CorruptionLevel.ROW));
                    continue;
                }
                // TODO : error management and report
                final T output = importRow(row, getOrCreateElement(row));
                output.setDesignation(rowId.toString());
                // Eventual document modifiers.
                HashSet<DocumentModifier> modifiers = context.modifiers.get(getDocumentClass());
                if (modifiers != null) {
                    for (final DocumentModifier mod : modifiers) {
                        mod.modifyDocument(row, output);
                    }
                }
                imports.put(rowId, output);
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

        postCompute();
    }
}
