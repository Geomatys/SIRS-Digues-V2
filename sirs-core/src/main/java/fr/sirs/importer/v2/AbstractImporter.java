package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Column;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An importer is suposed to retrive data from one and only one table of the
 * given database.
 *
 * TODO : replace {@link #getRowIdFieldName() } by a complex key object. Current
 * state prevent to import correctly data from join table, because returned key
 * is not unique, and indexing it result in data loss.
 *
 * @author Samuel Andrés (Geomatys)
 * @author Alexis Manin (Geomatys)
 * @param <T> Type of computed output.
 */
public abstract class AbstractImporter<T extends Element> {

    @Autowired
    protected SessionCore session;

    /**
     * Map which binds imported row ids to the ids of their version in output
     * database. If computing has not been performed yet, its value is null.
     */
    protected Map<Integer, String> importedRows;

    protected final HashSet<Mapper<T>> mappers = new HashSet();

    @Autowired
    protected ImportContext context;

    protected AbstractImporter() {}

    /**
     * @return type for the object to create and fill.
     */
    protected abstract Class<T> getElementClass();

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
     */
    protected void preCompute() throws AccessDbImporterException {}

    /**
     * A method which can be overrided to provide a specific treatment after table import.
     */
    protected void postCompute() {
        mappers.clear();
    }

    /**
     * Compute the maps referencing the retrieved objects.
     *
     * @throws java.io.IOException
     * @throws fr.sirs.importer.AccessDbImporterException
     */
    public synchronized void compute() throws IOException, AccessDbImporterException {
        if (importedRows != null)
            return;

        //DEBUG
        SirsCore.LOGGER.info("IMPORT OF " + getTableName());
        SirsCore.LOGGER.info("PRIMARY KEY " + getRowIdFieldName());

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
                final HashSet<Element> dataToPost = new HashSet<>();

                while (it.hasNext() && imports.size() < context.bulkLimit) {
                    final Row row = it.next();

                    // DEBUG
                    final Integer rowId;
                    try {
                        rowId = row.getInt(getRowIdFieldName());
                    } catch (ClassCastException e) {
                        SirsCore.LOGGER.warning("CAST ERROR --> "+getRowIdFieldName());
                        throw e;
                    }
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

                    final Element toPost = prepareToPost(rowId, row, output);
                    if (toPost == null)
                        continue;

                    dataToPost.add(toPost);
                    imports.put(rowId, output);
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
                 * IMPORTANT : NOT PUT IN AFTERPOST METHOD, TO ENSURE IMPLEMENTATIONS WILL DO IT.
                 */
                for (final Map.Entry<Integer, T> entry : imports.entrySet()) {
                    final String id = entry.getValue().getId();
                    if (id != null) {
                        importedRows.put(entry.getKey(), id);
                    }
                }
            }
        } finally {
            postCompute();
        }
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
     */
    public final String getImportedId(final Integer rowId) throws IOException, AccessDbImporterException {
        synchronized (this) {
            if (importedRows == null) {
                compute();
            }
        }
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
    protected T getOrCreateElement(final Row input) {
        return ElementCreator.createAnonymValidElement(getElementClass());
    }

    /**
     * Import a single row for current table.
     *
     * @param row The row to import.
     * @param output The object to feed with input row value.
     * @return The object to insert in output database. Should be the same the
     * output parameter.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public T importRow(final Row row, final T output)
            throws IOException, AccessDbImporterException {
        for (final Mapper m : mappers) {
            m.map(row, output);
        }

        return output;
    }

    /**
     * Once current row has been imported and resulting object has been modified,
     * this method is called to get the real object to send into CouchDB (Ex :
     * imported object was a sub-structure of the document to update).
     * @param rowId Id of the imported row.
     * @param row Row which has been imported
     * @param output The object which has been filled with current row.
     * @return Pojo which must be sent as a complete document into CouchDB.
     */
    protected Element prepareToPost(int rowId, Row row, T output) {
        return output;
    }

    /**
     * Allow an operation after a bulk update has been performed.
     * @param posted The items (keys are their ids) successfully sent into CouchDb.
     * @param imports The items (keys are originating row ids) which have been imported from ms-access for this bulk.
     */
    protected void afterPost(final Map<String, Element> posted, Map<Integer, T> imports) {}

    /*
     * DEBUG UTILITIES
     */

    /**
     *
     * @return the list of Access database table names.
     * @throws IOException
     */
    public List<String> getTableColumns() throws IOException {
        final List<String> names = new ArrayList<>();
        context.inputDb.getTable(getTableName()).getColumns().stream().forEach((column) -> {
            names.add(column.getName());
        });

        return names;
    }

    /**
     * Check all the columns used by the importer exists in the table.
     *
     * @return
     */
    private List<String> getErroneousUsedFields() throws IOException {
        final List<String> erroneousUsedColumn = new ArrayList<>();
        final Table table = context.inputDb.getTable(getTableName());

        // Check all used columns
        this.getUsedColumns().stream().forEach((usedColumnName) -> {
            boolean isPresent = false;
            for (final Column column : table.getColumns()) {

                if (column.getName().equals(usedColumnName)) {
                    isPresent = true;
                    break;
                }
            }
            if (!isPresent) {
                erroneousUsedColumn.add(usedColumnName);
            }

        });
        return erroneousUsedColumn;
    }

    /**
     *
     * @return The list of column names used by the importer which are empty.
     * @throws IOException
     */
    private List<String> getEmptyUsedFields() throws IOException {
        /*
         * Debug purpose. List all columns present in input table, with a flag
         * specifiying if it is usable (needed for the mapping and not empty) for
         * import.
         */
        final HashMap<String, Boolean> columnDataFlags = new HashMap<>();
        final List<String> usedColumns = getUsedColumns();

        // Set the data flags to false for all the columns used by the importer.
        usedColumns.stream().forEach((column) -> {
            columnDataFlags.put(column, Boolean.FALSE);
        });

        final List<String> emptyFields = new ArrayList<>();
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();

        // For each table row
        while (it.hasNext()) {
            final Row row = it.next();

            // For eache table column
            usedColumns.stream().forEach((column) -> {

                // Look for data in the cell if the data flag of the column is
                // false. If there is data, set the flag to true.
                if (!columnDataFlags.get(column) && row.get(column) != null)
                    columnDataFlags.put(column, Boolean.TRUE);
            });

            // If all the columns contains data, do not continue to look for data
            // in the following rows and break the loop.
            if (!columnDataFlags.containsValue(Boolean.FALSE))
                break;
        }

        // List the column names detected to not contain data.
        usedColumns.stream().forEach((column) -> {
            if (!columnDataFlags.get(column))
                emptyFields.add(column);
        });
        return emptyFields;
    }

    /**
     *
     * @return The list of table columns names ignored by the importer but
     * containing data.
     * @throws IOException
     */
    private List<String> getForgottenFields() throws IOException {
        final List<String> forgottenFields = new ArrayList<>();
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        final List<String> potentialForgottenFields = this.getTableColumns();
        potentialForgottenFields.removeAll(this.getUsedColumns());

        // For each table row
        while (it.hasNext()) {
            final Row row = it.next();

            // For eache table column
            this.getTableColumns().stream().forEach((column) -> {

                if (potentialForgottenFields.contains(column) && row.get(column) != null) {
                    forgottenFields.add(column);
                    potentialForgottenFields.remove(column);
                }
            });

            if (potentialForgottenFields.isEmpty())
                break;
        }

        return forgottenFields;
    }

    private void checkTable() {
        SirsCore.LOGGER.log(Level.FINE, "=================================================");
        SirsCore.LOGGER.log(Level.FINE, "======== IMPORTER CHECK for table : " + getTableName() + "====");
        try {
            // Detect the empty fields.
            final List<String> emptyFields = this.getEmptyUsedFields();
            if (!emptyFields.isEmpty()) {
                SirsCore.LOGGER.log(Level.FINE, "Empty used fields for table " + getTableName() + " : ");
                emptyFields.stream().forEach((field) -> {
                    SirsCore.LOGGER.log(Level.FINE, field);
                });
            }

            // Detect the coluns used by the importer that do not exist in the table.
            final List<String> erroneousFields = this.getErroneousUsedFields();
            if (!erroneousFields.isEmpty()) {
                SirsCore.LOGGER.log(Level.FINE, "Erroneous fields for table " + getTableName() + " : ");
                erroneousFields.stream().forEach((field) -> {
                    SirsCore.LOGGER.log(Level.FINE, field);
                });
            }

            // Detect the coluns forgotten by the importer but containing data;
            final List<String> forgottenFields = this.getForgottenFields();
            if (!forgottenFields.isEmpty()) {
                SirsCore.LOGGER.log(Level.FINE, "Forgotten fields for table " + getTableName() + " : ");
                forgottenFields.stream().forEach((field) -> {
                    SirsCore.LOGGER.log(Level.FINE, field);
                });
            }

        } catch (IOException ex) {
            SirsCore.LOGGER.log(Level.FINE, "An error occurred while analyzing table "+getTableName(), ex);
        }
        SirsCore.LOGGER.log(Level.FINE, "*************************************************\n");
    }
}
