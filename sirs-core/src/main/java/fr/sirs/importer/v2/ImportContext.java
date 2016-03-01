package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.vividsolutions.jts.geom.Coordinate;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Identifiable;
import fr.sirs.core.model.Positionable;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import fr.sirs.util.ImportParameters;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.ObjectConverters;
import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentOperationResult;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.esrigeodb.GeoDBStore;
import org.geotoolkit.feature.type.GeometryDescriptor;
import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengis.util.GenericName;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Contains main properties and data needed for an import from an access
 * database to a couchDB database.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ImportContext implements ApplicationContextAware {

    public static final String MAIN_DB_QUALIFIER = "main-access-db";
    public static final String CARTO_DB_QUALIFIER = "carto-access-db";
    public static final String OUTPUT_CRS_QUALIFIER = "output-crs";

    public static final String NULL_STRING = "null";

    public final String startXName = "X_DEBUT";
    public final String startYName = "Y_DEBUT";
    public final String endXName = "X_FIN";
    public final String endYName = "Y_FIN";

    /**
     * Input database containing object properties.
     */
    public final Database inputDb;
    /**
     * Input database containing projection and geometric information.
     */
    public final Database inputCartoDb;

    /**
     * Source database projection.
     */
    public final CoordinateReferenceSystem inputCRS;

    /**
     * Target database.
     */
    public final CouchDbConnector outputDb;

    /**
     * Target database projection.
     */
    public final CoordinateReferenceSystem outputCRS;

    /**
     * Transformation to convert a geometry from input database projection to
     * target database CRS.
     */
    public final MathTransform geoTransform;

    public final ConcurrentHashMap<Class, AbstractImporter> importers = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Class, HashSet<ElementModifier>> modifiers = new ConcurrentHashMap<>();

    public final ConcurrentHashMap<Class, HashSet<MapperSpi>> mappers = new ConcurrentHashMap<>();

    public final ArrayList<Linker> linkers = new ArrayList<>();

    /**
     * recommended limit size for bulk updates. /!\ This flag is NOT used by {@link #executeBulk(java.util.Collection)
     * }. It's only an informative attribute which should be used by importers
     * when computing.
     */
    public int bulkLimit = 500;

    private static ApplicationContext appCtx;

    private final SimpleIntegerProperty workProperty = new SimpleIntegerProperty(-1);
    private int totalWork;

    /**
     * List errors which occured while importing database. Errors can be
     * registered using {@link #reportError(fr.sirs.importer.v2.ErrorReport)}.
     * This list is only a buffer, and will be emptied regularly to avoid memory overload.
     */
    public final ObservableList<ErrorReport> errorBuffer = FXCollections.observableArrayList();
    private final int errorBufferLimit = 20;

    private final Path errorOutput;
    private boolean flushErrors = true;
    private ObjectOutputStream errorSerializer;

    @Autowired
    public ImportContext(final ImportParameters parameters)
            throws FactoryException, MalformedURLException, DataStoreException {

        this.inputDb = parameters.inputDb;
        this.inputCartoDb = parameters.inputCartoDb;
        this.outputDb = parameters.outputDb;
        this.outputCRS = parameters.outputCRS;

        CoordinateReferenceSystem crs = null;
        try (final GeoDBStore store = new GeoDBStore("no namespace", inputCartoDb.getFile().toURI().toURL())) {
            for (final GenericName name : store.getNames()) {
                GeometryDescriptor geomDesc = store.getFeatureType(name).getGeometryDescriptor();
                if (geomDesc != null) {
                    crs = geomDesc.getType().getCoordinateReferenceSystem();
                    if (crs != null)
                        break;
                }
            }
        }

        if (crs == null) {
            inputCRS = CRS.decode("EPSG:27593", true);
        } else {
            inputCRS = crs;
        }

        geoTransform = CRS.findMathTransform(inputCRS, outputCRS, true);

        final long time = System.currentTimeMillis();
        errorOutput = SirsCore.IMPORT_ERROR_DIR.resolve(outputDb.getDatabaseName() + "_at_" + time);
    }

    @PostConstruct
    private void registerComponent() {
        if (appCtx == null) {
            throw new IllegalStateException("Application context has not been registered yet !");
        }

        final Collection<AbstractImporter> importerList = appCtx.getBeansOfType(AbstractImporter.class).values();
        final Collection<MapperSpi> mapperList = appCtx.getBeansOfType(MapperSpi.class).values();
        final Collection<ElementModifier> modifierList = appCtx.getBeansOfType(ElementModifier.class).values();
        final Collection<WorkMeasurable> workMeasurableList = appCtx.getBeansOfType(WorkMeasurable.class).values();
        linkers.addAll(appCtx.getBeansOfType(Linker.class).values());

        /*
         * Register importers. Should get one per output type.
         */
        for (final AbstractImporter importer : importerList) {
            registerImporter(importer.getElementClass(), importer);
            if (importer instanceof MultipleSubTypes) {
                Collection<Class> subTypes = ((MultipleSubTypes) importer).getSubTypes();
                for (final Class subtype : subTypes) {
                    registerImporter(subtype, importer);
                }
            }
        }

        /*
         * Register mappers
         */
        for (final MapperSpi spi : mapperList) {
            HashSet<MapperSpi> mapperSet = mappers.get(spi.getOutputClass());
            if (mapperSet == null) {
                mapperSet = new HashSet<>();
                mappers.put(spi.getOutputClass(), mapperSet);
            }
            mapperSet.add(spi);
        }

        for (final ElementModifier modifier : modifierList) {
            HashSet<ElementModifier> modifierSet = modifiers.get(modifier.getDocumentClass());
            if (modifierSet == null) {
                modifierSet = new HashSet<>();
                modifiers.put(modifier.getDocumentClass(), modifierSet);
            }
            modifierSet.add(modifier);
        }

        final Iterator<WorkMeasurable> workIt = workMeasurableList.iterator();
        if (workIt.hasNext()) {
            WorkMeasurable next = workIt.next();
            totalWork = next.getTotalWork();
            NumberBinding bind = next.getWorkDone().add(0);
            while (workIt.hasNext()) {
                next = workIt.next();
                bind = bind.add(next.getWorkDone());
                totalWork += next.getTotalWork();
            }
            workProperty.bind(bind);
        } else {
            totalWork = -1;
        }
    }

    private void registerImporter(final Class key, final AbstractImporter importer) {
        AbstractImporter deleted = importers.put(key, importer);
        if (deleted != null) {
            final StringBuilder builder = new StringBuilder("Two importers declared for type ")
                    .append(key.getCanonicalName())
                    .append(" :\n")
                    .append(deleted.getClass().getCanonicalName())
                    .append('\n')
                    .append(importer.getClass().getCanonicalName());
            throw new IllegalStateException(builder.toString());
        }
    }

    @PreDestroy
    private void destroy() {
        if (!errorBuffer.isEmpty()) {
            try {
                serialize(errorBuffer.toArray(new ErrorReport[errorBuffer.size()]));
            } catch (IOException ex) {
                SirsCore.LOGGER.log(Level.WARNING, "Cannot flush errors !", ex);
            }
        }

        if (errorSerializer != null)
            try {
                errorSerializer.close();
        } catch (IOException ex) {
            SirsCore.LOGGER.log(Level.WARNING, "An error occurred while closing a stream. It may cause memory leaks !", ex);
        }
    }

    public ReadOnlyIntegerProperty getWorkDone() {
        return workProperty;
    }

    public int getTotalWork() {
        return totalWork;
    }

    public <T> T convertData(final Object input, final Class<T> outputClass) {
        if (outputClass.isAssignableFrom(input.getClass())) {
            return (T) input;
        } else if (input instanceof Date) {
            if (outputClass.isAssignableFrom(LocalDate.class)) {
                return (T) toLocalDate((Date) input);
            } else if (outputClass.isAssignableFrom(LocalDateTime.class)) {
                return (T) toLocalDateTime((Date) input);
            }
        }

        return ObjectConverters.convert(input, outputClass);
    }

    /**
     * Try to extract start and end points from a given row
     *
     * @param input Row to extract data from.
     * @param toSet Positionable to write position information into.
     * @throws TransformException If an error occurs while converting read points from access CRS to user CRS.
     */
    public void setGeoPositions(final Row input, final Positionable toSet) throws TransformException {
        final Double startX = input.getDouble(startXName);
        final Double startY = input.getDouble(startYName);
        final Double endX = input.getDouble(endXName);
        final Double endY = input.getDouble(endYName);

        final boolean hasGeoStart = startX != null && startY != null;
        final boolean hasGeoEnd = endX != null && endY != null;

        if (hasGeoStart && hasGeoEnd) {
            final double[] points = new double[]{startX, startY, endX, endY};
            geoTransform.transform(points, 0, points, 0, 2);
            toSet.setPositionDebut(GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(points[0], points[1])));
            toSet.setPositionFin(GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(points[2], points[3])));

        } else if (hasGeoStart) {
            final double[] points = new double[]{startX, startY};
            geoTransform.transform(points, 0, points, 0, 1);
            toSet.setPositionDebut(GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(points[0], points[1])));

        } else if (hasGeoEnd) {
            final double[] points = new double[]{endX, endY};
            geoTransform.transform(points, 0, points, 0, 1);
            toSet.setPositionFin(GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(points[0], points[1])));
        }
    }

    public void reportError(final String tableName, final Row sourceRow, final Exception occurredError) {
        ImportContext.this.reportError(new ErrorReport(occurredError, sourceRow, tableName));
    }

    public void reportError(final String tableName, final Row sourceRow, final Exception occurredError, final String message) {
        ErrorReport errorReport = new ErrorReport(occurredError, sourceRow, tableName);
        errorReport.customErrorMsg = message;
        ImportContext.this.reportError(errorReport);
    }

    /**
     * Inform system that an error occured while importing a document.
     *
     * @param report Error report to submit.
     */
    public synchronized void reportError(final ErrorReport report) {
        ArgumentChecks.ensureNonNull("Error report", report);
        logError(report);
        if (errorBufferLimit < errorBuffer.size()) {
            if (flushErrors) {
                try {
                    serialize(errorBuffer.toArray(new ErrorReport[errorBuffer.size()]));
                } catch (IOException ex) {
                    SirsCore.LOGGER.log(Level.WARNING, "Errors cannot be written on disk. Serialization deactivated.", ex);
                    flushErrors = false;
                }
            }

            errorBuffer.clear();
        }

        errorBuffer.add(report);

    }

    public static void logError(final ErrorReport report) {
        SirsCore.LOGGER.log(Level.WARNING, new ErrorMessenger(report));
    }

    /**
     * Insert or update elements given, then return them along with their ids.
     * Documents which failed to be updated are not listed. Instead, an error
     * report is submitted for each one.
     *
     * @param toUpdate collection of elements to insert or update.
     * @return successfully inserted / updated documents and their ids.
     */
    public Map<String, Element> executeBulk(Collection<Element> toUpdate) {
        ArgumentChecks.ensureNonNull("Objects to update.", toUpdate);
        if (toUpdate.isEmpty())
            return new HashMap<>();

        // Ensure we update a document only once.
        if (!(toUpdate instanceof HashSet)) {
            toUpdate = new HashSet(toUpdate);
        }

        // Try to perform bulk, then analyze result to find errors.
        List<DocumentOperationResult> bulkResult = outputDb.executeBulk(toUpdate);
        HashMap<String, Element> ids = buildIdMap(toUpdate);
        if (bulkResult != null && !bulkResult.isEmpty()) {
            for (final DocumentOperationResult opResult : bulkResult) {
                final ErrorReport report = new ErrorReport();
                report.corruptionLevel = CorruptionLevel.ROW;
                report.customErrorMsg = buildErrorMsg(opResult);
                final String id = opResult.getId();
                if (id != null) {
                    report.target = ids.get(id);
                    ids.remove(id); // Remove, because we must not send ids of failed updates.
                }
                reportError(report);
            }
        }

        return ids;
    }

    /*
     * Utilities
     */
    /**
     * Convert a {@link Date} to a {@link LocalDate}.
     *
     * @param date Date to convert. If null, a null value is returned.
     * @return The converted date, or null if no input was given.
     */
    public static LocalDate toLocalDate(final Date date) {
        return date == null ? null : LocalDate.from(date.toInstant().atZone(SirsCore.PARIS_ZONE_ID));
    }

    /**
     * Convert a {@link Date} to a {@link LocalDateTime}.
     *
     * @param date Date to convert. If null, a null value is returned.
     * @return The converted date and time, or null if no input was given.
     */
    public static LocalDateTime toLocalDateTime(final Date date) {
        return date == null ? null : LocalDateTime.from(date.toInstant().atZone(SirsCore.PARIS_ZONE_ID));
    }

    /**
     * Get error information from input report to build a detailed error
     * message.
     *
     * @param error The CouchDB error report.
     * @return A formatted error message. Never null.
     * @throws IllegalArgumentException if input error report is null.
     */
    public static String buildErrorMsg(final DocumentOperationResult error) {
        ArgumentChecks.ensureNonNull("Error report", error);
        final StringBuilder builder = new StringBuilder();
        builder.append("An error occurred while ");
        final String id = error.getId();
        if (id != null && !id.isEmpty()) {
            builder.append("updating document with ID : ").append(id);
            final String revision = error.getRevision();
            if (revision != null && !revision.isEmpty()) {
                builder.append(" - revision : ").append(revision);
            }
        } else {
            builder.append("inserting a document.");
        }

        if (error.isErroneous()) {
            builder.append('\n')
                    .append("--- Error ---\n")
                    .append(builder);
        }

        final String reason = error.getReason();
        if (reason != null && !reason.isEmpty()) {
            builder.append('\n')
                    .append("--- Reason ---\n")
                    .append(builder);
        }

        return builder.toString();
    }

    /**
     * For each object in given collection, if it is an {@link Identifiable}
     * object, put it in a map whose keys are values identifiers.
     *
     * @param <T> Type of the objects to analyze.
     * @param toAnalyze Collection of objects to sort.
     * @return A map containing all identifiable objects of input collection.
     * Never null, but can be empty.
     */
    public static <T> HashMap<String, T> buildIdMap(final Collection<T> toAnalyze) {
        final HashMap<String, T> result = new HashMap<>();
        for (final T o : toAnalyze) {
            if (o instanceof Identifiable) {
                final Identifiable i = (Identifiable) o;
                if (i.getId() != null) {
                    result.put(i.getId(), o);
                }
            }
        }
        return result;
    }

    public static boolean columnExists(final Table table, final String columnName) {
        try {
            table.getColumn(columnName);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /**
     * Retrieve object imported from given row, then return it.
     *
     * @param currentRow The row which has been used for import of searched
     * object.
     * @return Object created after input row import. Never null.
     * @throws IllegalStateException if we cannot find any imported document for
     * given row.
     */
    Object getBoundTarget(Row currentRow) throws IllegalStateException {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T> Set<Mapper<T>> getCompatibleMappers(final Table source, final Class<T> destination) {
        final HashSet<Mapper<T>> result = new HashSet<>();
        for (final Map.Entry<Class, HashSet<MapperSpi>> entry : mappers.entrySet()) {
            if (entry.getKey().isAssignableFrom(destination)) {
                for (final MapperSpi spi : entry.getValue()) {
                    Optional<Mapper> mapper = spi.configureInput(source);
                    if (mapper.isPresent()) {
                        result.add(mapper.get());
                    }
                }
            }
        }
        return result;
    }

    public <T extends Element> Set<ElementModifier<T>> getCompatibleModifiers(final Table source, final Class<T> destination) {
        final HashSet<ElementModifier<T>> result = new HashSet<>();
        for (final Map.Entry<Class, HashSet<ElementModifier>> entry : modifiers.entrySet()) {
            if (entry.getKey().isAssignableFrom(destination)) {
                result.addAll((Collection) entry.getValue());
            }
        }
        return result;
    }

    /**
     * Return all importers which work on objects inheriting given class.
     *
     * @param sourceClass Pojo type to retrieve importers for.
     * @return List of found importers, or an empty list if we cannot find any
     * importer for given object type.
     */
    public List<AbstractImporter> getImporters(final Class sourceClass) {
        final ArrayList<AbstractImporter> result = new ArrayList<>();

        for (final Map.Entry<Class, AbstractImporter> entry : importers.entrySet()) {
            if (sourceClass.isAssignableFrom(entry.getKey())) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Return an operator able to read data from a specific column of a row to
     * put it in a specific property of a specific class.
     *
     * TODO : IMPLEMENT MECHANISM (including a registry).
     *
     * @param <T> Type of object to affect.
     * @param outputClass Class of the object which will be modified by the
     * returned consumer.
     * @param outputProperty The property to set in output object.
     * @param columnName Name of the column to read from input row.
     * @return Adequat operator, or an empty optional if we cannot find any.
     */
    public <T> Optional<BiConsumer<Row, T>> getConsumer(final Class<T> outputClass, final PropertyDescriptor outputProperty, final String columnName) {
        return Optional.empty();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appCtx = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return appCtx;
    }

    private static class ErrorMessenger implements Supplier<String> {

        final ErrorReport report;

        public ErrorMessenger(ErrorReport report) {
            this.report = report;
        }

        @Override
        public String get() {
            return new StringBuilder("\n------- ERROR -------\n")
                    .append(valueOrUndefined(report.customErrorMsg))
                    .append('\n')
                    .append("Caused by : ")
                    .append(valueOrUndefined(report.error))
                    .append('\n')
                    .append("Corruption level : ")
                    .append(valueOrUndefined(report.corruptionLevel))
                    .append('\n')
                    .append("Table : ")
                    .append(valueOrUndefined(report.sourceTableName))
                    .append('\n')
                    .append("Column : ")
                    .append(valueOrUndefined(report.sourceColumnName))
                    .append('\n')
                    .append("Input row : ")
                    .append(valueOrUndefined(report.sourceData))
                    .append('\n')
                    .append("Output document : ")
                    .append(valueOrUndefined(report.target))
                    .append('\n')
                    .append("Target field : ")
                    .append(valueOrUndefined(report.targetFieldName))
                    .append('\n')
                    .toString();
        }

        private static Object valueOrUndefined(final Object input) {
            return input == null ? "Undefined" : input;
        }

    }

    /**
     * Serialize input error reports in the file parameterized for current import.
     * @param reports All reports to flush.
     * @throws IOException If an error happens when creating or while writing into output file.
     */
    public synchronized void serialize(ErrorReport... reports) throws IOException {
        if (errorSerializer == null) {
            if (!Files.isRegularFile(errorOutput)) {
                Files.createDirectories(errorOutput.getParent());
                Files.createFile(errorOutput);
            }
            errorSerializer = new ObjectOutputStream(Files.newOutputStream(errorOutput));
        }

        /* Each object is written to a buffer, because some of them may fail. If
           it is the case, we'll apply a treatment to make them serializable.
           After it, serialization is done, we flush result in error file.
        */
        try (
                final ByteArrayOutputStream tmpStream = new ByteArrayOutputStream();
                final ObjectOutputStream tmpSerializer = new ObjectOutputStream(tmpStream)) {

            for (final ErrorReport report : reports) {
                try {
                    tmpSerializer.writeObject(report);
                } catch (NotSerializableException e) {
                    report.setSerializable();
                } finally {
                    tmpStream.reset();
                }

                errorSerializer.writeObject(report);
            }
            errorSerializer.flush();
        }
    }

    /**
     * Read all error reports found in given file.
     * @param reportFile File to read.
     * @return a list of all found error reports. Never null, but can be empty.
     * @throws IOException If an error occurs while reading input file.
     * @throws IllegalArgumentException If input file is not readable.
     */
    public static ArrayList<ErrorReport> deserialize(final Path reportFile) throws IOException {
        if (!Files.isRegularFile(reportFile)) {
            throw new IllegalArgumentException("Input file is invalid !");
        }
        try (
                final InputStream fileInput = Files.newInputStream(reportFile);
                final ObjectInputStream in = new ObjectInputStream(fileInput)) {
            final ArrayList<ErrorReport> reports = new ArrayList<>();
            while (fileInput.available() > 0) {
                try {
                    Object readObject = in.readObject();
                    if (readObject instanceof ErrorReport) {
                        reports.add((ErrorReport) readObject);
                    } else {
                        SirsCore.LOGGER.warning("Unknown object in error report file !\n"+readObject);
                    }
                } catch (ClassNotFoundException ex) {
                    SirsCore.LOGGER.log(Level.WARNING, "Unknown object in error report file !", ex);
                }
            }
            return reports;
        }
    }
}
