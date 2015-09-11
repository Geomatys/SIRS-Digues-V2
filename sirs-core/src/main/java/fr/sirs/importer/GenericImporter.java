package fr.sirs.importer;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.Identifiable;
import fr.sirs.importer.v2.CorruptionLevel;
import fr.sirs.importer.v2.ErrorReport;
import fr.sirs.importer.v2.ImportContext;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An importer is suposed to retrive data from one and only one table of the
 * given database.
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T> Type of computed output.
 */
public abstract class GenericImporter<T extends Element> {

    public static CoordinateReferenceSystem outputCrs;

    protected final DateTimeFormatter dateTimeFormatter;

    /**
     *
     * Debug purpose. List all columns present in input table, with a flag specifiying
     * if it is usable (needed for the mapping and not empty) for import.
     */
    private Map<String, Boolean> columnDataFlags;

    /**
     * Map which binds imported row ids to the ids of their version in output
     * database. If computing has not been performed yet, its value is null.
     */
    protected Map<Integer, String> importedRows;

    @Autowired
    protected ImportContext context;

    public GenericImporter() {
        InjectorCore.injectDependencies(this);

        this.dateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
        this.columnDataFlags = new HashMap<>();

        // Set the data flags to false for all the columns used by the importer.
        this.getUsedColumns().stream().forEach((column) -> {
            this.columnDataFlags.put(column, Boolean.FALSE);
        });

        /*
         * DEBUG
         */
//        SirsCore.LOGGER.log(Level.FINE, "=================================================");
//        SirsCore.LOGGER.log(Level.FINE, "======== IMPORTER CHECK for table : " + getTableName() + "====");
//        try {
//            // Detect the empty fields.
//            final List<String> emptyFields = this.getEmptyUsedFields();
//            if (!emptyFields.isEmpty()) {
//                SirsCore.LOGGER.log(Level.FINE, "Empty used fields for table " + getTableName() + " : ");
//                emptyFields.stream().forEach((field) -> {
//                    SirsCore.LOGGER.log(Level.FINE, field);
//                });
//            }
//
//            // Detect the coluns used by the importer that do not exist in the table.
//            final List<String> erroneousFields = this.getErroneousUsedFields();
//            if (!erroneousFields.isEmpty()) {
//                SirsCore.LOGGER.log(Level.FINE, "Erroneous fields for table " + getTableName() + " : ");
//                erroneousFields.stream().forEach((field) -> {
//                    SirsCore.LOGGER.log(Level.FINE, field);
//                });
//            }
//
//            // Detect the coluns forgotten by the importer but containing data;
//            final List<String> forgottenFields = this.getForgottenFields();
//            if (!forgottenFields.isEmpty()) {
//                SirsCore.LOGGER.log(Level.FINE, "Forgotten fields for table " + getTableName() + " : ");
//                forgottenFields.stream().forEach((field) -> {
//                    SirsCore.LOGGER.log(Level.FINE, field);
//                });
//            }
//
//        } catch (IOException ex) {
//            Logger.getLogger(GenericImporter.class.getName()).log(Level.FINE, null, ex);
//        }
//        SirsCore.LOGGER.log(Level.FINE, "*************************************************\n");
        /*
         * END DEBUG
         */
    }

    @PostConstruct
    private void register() {
        context.importers.put(getDocumentClass(), this);
    }

    public CoordinateReferenceSystem getOutputCrs() {
        return outputCrs;
    }

    /**
     * @return type for the object to create / update at import.
     */
    protected abstract Class<T> getDocumentClass();

    /**
     *
     * @return the list of the column names used by the importer. This method
     * must not return the whole columns from the table, but only those used by
     * the importer.
     */
    protected abstract List<String> getUsedColumns();

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
     * A method which can be overrided to provide a specific treatment before table import.
     */
    protected void preCompute() throws AccessDbImporterException {};

    /**
     * A method which can be overrided to provide a specific treatment after table import.
     */
    protected void postCompute() throws AccessDbImporterException {};

    /**
     * Compute the maps referencing the retrieved objects.
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
         * Import entire table  content. We split import in packets to avoid memory overload.
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

    /**
     * Retrieve ID of the object in output database which corresponds to the given ms-access row Id.
     * Note : If this importer has not imported yet its affected table, it will do it before returning
     * a result, making this method possibly time/cpu consuming.
     * @param rowId Id of the object to return.
     * @return Id of the wanted object in output database, or null if we cannot find it.
     */
    public final String getImportedId(final Integer rowId) throws IOException, AccessDbImporterException {
        if (importedRows == null) {
            compute();
        }
        return importedRows.get(rowId);
    }

    /**
     * Create an empty {@link Element} to put data from input row into it.
     * @param input The row to import.
     * @return The object to import row into.
     */
    protected T getOrCreateElement(final Row input) {
        return ElementCreator.createAnonymValidElement(getDocumentClass());
    }

    /**
     * Import a single row for current table.
     * @param row The row to import.
     * @param output The object to feed with input row value.
     * @return The object to insert in output database. Should be the same the output parameter.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public abstract T importRow(final Row row, final T output)
            throws IOException, AccessDbImporterException;

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
        final List<String> emptyFields = new ArrayList<>();
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();

        // For each table row
        while (it.hasNext()) {
            final Row row = it.next();

            // For eache table column
            this.getUsedColumns().stream().forEach((column) -> {

                // Look for data in the cell if the data flag of the column is
                // false. If there is data, set the flag to true.
                if (!this.columnDataFlags.get(column) && row.get(column) != null)
                    this.columnDataFlags.put(column, Boolean.TRUE);
            });

            // If all the columns contains data, do not continue to look for data
            // in the following rows and break the loop.
            if (!this.columnDataFlags.containsValue(Boolean.FALSE))
                break;
        }

        // List the column names detected to not contain data.
        this.getUsedColumns().stream().forEach((column) -> {
            if (!this.columnDataFlags.get(column))
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
}
