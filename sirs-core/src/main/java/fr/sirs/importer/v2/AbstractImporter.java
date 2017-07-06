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
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.mapper.Mapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An importer is suposed to retrieve data from one and only one table of the
 * given database. Note : Type of computed output is not necessarily a CouchDB
 * document. To allow user to post its data in this case, implementations of
 * this class can wrap their output in a postable document by overriding method {@link #prepareToPost(java.lang.Object, com.healthmarketscience.jackcess.Row, fr.sirs.core.model.Element)
 * }.
 *
 * @author Samuel Andrés (Geomatys)
 * @author Alexis Manin (Geomatys)
 * @param <T> Type of computed output.
 */
public abstract class AbstractImporter<T extends Element> implements WorkMeasurable {

    @Autowired
    protected SessionCore session;

    @Autowired
    protected ImportContext context;

    /**
     * Map which binds imported row ids to the ids of their version in output
     * database. If computing has not been performed yet, its value is null.
     */
    private Map<Object, String> importedRows;

    private Set<Mapper<T>> mappers;
    private Set<ElementModifier<T>> modifiers;

    private HashMap<Class, Collection<Mapper>> additionalMappers;
    private HashMap<Class, Collection<ElementModifier>> additionalModifiers;

    protected final SimpleIntegerProperty count = new SimpleIntegerProperty(0);

    protected Table table;

    protected AbstractImporter() {}

    /**
     * @return type for the object to create and fill.
     */
    public abstract Class<T> getElementClass();

    /**
     *
     * @return the list of the column names used by the importer. This method
     * must not return the whole columns from the table, but only those used by
     * the importer.
     */
    protected List<String> getUsedColumns() {
        return Collections.EMPTY_LIST;
    }

    /**
     *
     * @return The table name used by the importer.
     */
    public abstract String getTableName();

    /**
     * @return name of the field which contains id for input rows. Shouldd never
     * be null.
     */
    public abstract String getRowIdFieldName();

    /**
     * A method which can be overrided to provide a specific treatment before
     * table import.
     *
     * @throws fr.sirs.importer.AccessDbImporterException If an unrecoverable
     * error occurs.
     */
    protected void preCompute() throws AccessDbImporterException {}

    /**
     * A method which can be overrided to provide a specific treatment after
     * table import.
     */
    protected void postCompute() {}

    /**
     * Compute the maps referencing the retrieved objects.
     *
     * @throws java.io.IOException If an error occurs while connecting to database.
     * @throws fr.sirs.importer.AccessDbImporterException If an error occurs while processing import.
     */
    public synchronized void compute() throws IOException, AccessDbImporterException {
        if (importedRows != null)
            return;

        //DEBUG
        SirsCore.LOGGER.info("\nIMPORT OF " + getTableName() + " by " + getClass().getCanonicalName() + ". PRIMARY KEY : " + getRowIdFieldName());

        table = context.inputDb.getTable(getTableName());
        mappers = new HashSet(context.getCompatibleMappers(table, getElementClass()));
        modifiers = new HashSet(context.getCompatibleModifiers(table, getElementClass()));
        additionalMappers = new HashMap<>();
        additionalModifiers = new HashMap<>();
        // In case we want to boost import with multi-threading.
        importedRows = new ConcurrentHashMap<>();

        preCompute();

        /*
         * Import entire table  content. We split import in packets to avoid memory overload.
         */
        try {
            final Iterator<Row> it = table.newCursor().toCursor().iterator();
            int rowCount = table.getRowCount();
            while (rowCount > 0) {
                int bulkCount = Math.min(context.bulkLimit, rowCount);
                rowCount -= bulkCount;
                final HashMap<Object, T> imports = new HashMap<>(bulkCount);
                final HashSet<Element> dataToPost = new HashSet<>(bulkCount);

                while (it.hasNext() && imports.size() < bulkCount) {
                    final Row row = it.next();

                    final Object rowId = row.get(getRowIdFieldName());

                    if (rowId == null) {
                        context.reportError(new ErrorReport(null, row, getTableName(), getRowIdFieldName(), null, null, "Imported row is not linkable due to null ID.", CorruptionLevel.RELATION));
                    }

                    T output = createElement(row);
                    if (output == null) {
                        continue;
                    }

                    if (rowId != null) {
                        output.setDesignation(rowId.toString());
                    }
                    output = importRow(row, output);

                    final Element toPost = prepareToPost(rowId, row, output);
                    if (toPost == null) {
                        continue;
                    }

                    // Once we prepared our element for posting, we can apply final modifications which might need all object context.
                    for (final ElementModifier mod : modifiers) {
                        mod.modify(output);
                    }

                    // Needed for importers implementing MultipleSubTypes interface.
                    final Class clazz = output.getClass();
                    if (!getElementClass().equals(clazz)) {
                        Collection<ElementModifier> tmpModifiers = additionalModifiers.get(clazz);
                        if (tmpModifiers == null) {
                            tmpModifiers = context.getCompatibleModifiers(table, (Class) clazz);
                            tmpModifiers.removeAll(modifiers);
                            additionalModifiers.put(clazz, tmpModifiers);
                        }

                        for (final ElementModifier mod : tmpModifiers) {
                            mod.modify(output);
                        }
                    }

                    dataToPost.add(toPost);

                    if (rowId != null) {
                        imports.put(rowId, output);
                    }
                }

                afterPost(
                        context.executeBulk(dataToPost),
                        imports
                );

                /*
                 * We keep a binding between all original rows and output objects using
                 * their Ids. We check that bulk has succeeded for an object before creating
                 * the binding.
                 *
                 * IMPORTANT : DO NOT PUT IN AFTERPOST METHOD, TO ENSURE IMPLEMENTATIONS WILL DO IT.
                 */
                for (final Map.Entry<Object, T> entry : imports.entrySet()) {
                    final String id = entry.getValue().getId();
                    if (id != null) {
                        importedRows.put(entry.getKey(), id);
                    }
                }
            }
        } finally {
            table = null;
            mappers = null;
            modifiers = null;
            additionalMappers = null;
            additionalModifiers = null;
            postCompute();
            count.set(1);
        }
    }

    @Override
    public int getTotalWork() {
        return 1;
    }

    @Override
    public IntegerProperty getWorkDone() {
        return count;
    }


    /**
     * Retrieve ID of the object in output database which corresponds to the
     * given ms-access row Id. Note : If this importer has not imported yet its
     * affected table, it will do it before returning a result, making this
     * method possibly time/cpu consuming.
     *
     * @param rowId Id of the object to return.
     * @return Id of the wanted object in output database, or null if we cannot
     * find it.
     * @throws java.io.IOException If an error occurs while importing data.
     * @throws fr.sirs.importer.AccessDbImporterException If an error occurs
     * while importing data.
     * @throws IllegalStateException If no document has been imported for given
     * row id.
     */
    public final String getImportedId(final Object rowId) throws IOException, AccessDbImporterException {
        compute();

        final String result = importedRows.get(rowId);
        if (result == null) {
            throw new IllegalStateException("No imported object found for row " + rowId + " from table " + getTableName());
        }
        return result;
    }

    /**
     * Create an empty {@link Element} to put data from input row into it.
     *
     * @param input The row to import.
     * @return The object to import row into. If null, we skip row import.
     */
    protected T createElement(final Row input) {
        return ElementCreator.createAnonymValidElement(getElementClass());
    }

    /**
     * Import a single row for current table.
     *
     * @param row The row to import.
     * @param output The object to feed with input row value.
     * @return The object to insert in output database. Should be the same the
     * output parameter.
     * @throws IOException If an error occurs during attribute mapping.
     * @throws AccessDbImporterException If an error happens during mapping.
     */
    protected T importRow(final Row row, final T output)
            throws IOException, AccessDbImporterException {
        for (final Mapper m : mappers) {
            m.map(row, output);
        }

        // Needed for importers implementing MultipleSubTypes interface.
        final Class clazz = output.getClass();
        if (!getElementClass().equals(clazz)) {
            Collection<Mapper> tmpMappers = additionalMappers.get(clazz);
            if (tmpMappers == null) {
                tmpMappers = context.getCompatibleMappers(table, (Class) clazz);
                tmpMappers.removeAll(mappers);
                additionalMappers.put(clazz, tmpMappers);
            }

            for (final Mapper m : tmpMappers) {
                m.map(row, output);
            }
        }

        return output;
    }

    /**
     * Once current row has been imported and resulting object has been
     * modified, this method is called to get the real object to send into
     * CouchDB (Ex : imported object was a sub-structure of the document to
     * update).
     *
     * @param rowId Id of the imported row.
     * @param row Row which has been imported
     * @param output The object which has been filled with current row.
     * @return Pojo which must be sent as a complete document into CouchDB.
     */
    protected Element prepareToPost(Object rowId, Row row, T output) {
        return output;
    }

    /**
     * Allow an operation after a bulk update has been performed.
     *
     * @param posted The items (keys are their ids) successfully sent into
     * CouchDb.
     * @param imports The items (keys are originating row ids) which have been
     * imported from ms-access for this bulk.
     */
    protected void afterPost(final Map<String, Element> posted, Map<Object, T> imports) {
    }

    /*
     * DEBUG UTILITIES
     */
    /**
     *
     * @return the list of Access database table names.
     * @throws IOException if an error happens while connecting to database.
     */
    public List<String> getTableColumns() throws IOException {
        final List<String> names = new ArrayList<>();
        context.inputDb.getTable(getTableName()).getColumns().stream().forEach((column) -> {
            names.add(column.getName());
        });

        return names;
    }
}
