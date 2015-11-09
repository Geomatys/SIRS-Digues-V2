package fr.sirs.core.h2;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.sis.storage.DataStoreException;
import org.ektorp.CouchDbConnector;
import org.geotoolkit.db.h2.H2FeatureStoreFactory;
import org.geotoolkit.parameter.Parameters;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.opengis.parameter.ParameterValueGroup;

import fr.sirs.core.DocHelper;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCore;
import static fr.sirs.core.SirsCore.INFO_DOCUMENT_ID;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.SirsDBInfo;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.sql.CoreSqlHelper;
import fr.sirs.core.model.sql.SQLHelper;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.ektorp.StreamingViewResult;
import org.ektorp.ViewResult;
import org.geotoolkit.db.JDBCFeatureStore;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.geotoolkit.jdbc.DBCPDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO : Set as component to check rdbms integrity when querying data store.
 *
 * @author Alexis Manin (Geomatys)
 * @author Olivier Nouguier (Geomatys)
 */
@Component
public class H2Helper {

    private static final Pattern UNMANAGED_IDS = Pattern.compile("^($|_).*");

    private JDBCFeatureStore store;
    private final CouchDbConnector couchDb;
    private final List<SQLHelper> sqlHelpers;

    private Task<Boolean> exportTask;

    @Autowired
    private H2Helper(final CouchDbConnector connector, final List<SQLHelper> helpers) {
        couchDb = connector;

        // Put core helper in first position.
        if (!helpers.isEmpty() && !(helpers.get(0) instanceof CoreSqlHelper)) {
            for (int i = 1; i < helpers.size(); i++) {
                if (helpers.get(i) instanceof CoreSqlHelper) {
                    helpers.add(0, helpers.remove(i));
                    break;
                }
            }
        }
        sqlHelpers = helpers;
    }

    /**
     * Export data from current CouchDB to an H2 dump. If dump have already been
     * performed yet, it will be erased and done a second time. However, only
     * one export can be performed at a time, and returned task
     *
     * @return The task in charge of export. Returned task is already submitted
     * for execution. As only one export can be performed at a time, the
     * returned task could be a process initiated by another thread / component.
     */
    public synchronized Task<Boolean> export() {
        if (exportTask == null || exportTask.isDone()) {
            exportTask = new ExportToRDBMS();
        }

        TaskManager.MockTask<Boolean> exportState = new TaskManager.MockTask<>(() -> exportTask.isRunning());
        if (Platform.isFxApplicationThread()) {
            exportState.run();
        } else {
            Platform.runLater(exportState);
        }
        final Boolean running;
        try {
            running = exportState.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new SirsCoreRuntimeException("Cannot check SQL export task status", ex);
        }

        if (Boolean.TRUE.equals(running)) {
            return exportTask;
        } else {
            return TaskManager.INSTANCE.submit(exportTask);
        }
    }

    private Connection createConnection() throws SQLException {
        final Connection connection = DriverManager.getConnection("jdbc:h2:" + getDBFile().toString(), "sirs$user", "sirs$pwd");
        return connection;
    }

    public synchronized Task<JDBCFeatureStore> getStore() throws SQLException, DataStoreException {
        return TaskManager.INSTANCE.submit(new Task<JDBCFeatureStore>() {

            @Override
            protected JDBCFeatureStore call() throws Exception {
                /**
                 * First, we check that database have already been exported. If not,
                 * we launch a new db export.
                 */
                synchronized (H2Helper.this) {
                    if (exportTask == null) {
                        export();
                    }
                }

                Platform.runLater(() -> {
                    if (exportTask.isRunning()) {
                        updateTitle(exportTask.getTitle());
                        exportTask.messageProperty().addListener((obs, oldValue, newValue) -> updateMessage(newValue));
                    }
                });

                // Ensure db export is finished before going further.
                if (!exportTask.get()) {
                    throw new IllegalStateException("Failed to export database !");
                }

                updateTitle("Connexion à la base de donnée");
                updateMessage("Connexion à la base de donnée");

                // Now we can create the data store. Ensure thread synchronization to avoid multiple initializations.
                synchronized (H2Helper.this) {
                    if (store == null) {
                        final Path file = getDBFile();
                        final BasicDataSource ds = new BasicDataSource();
                        ds.setUrl("jdbc:h2:" + file.toString());
                        ds.setUsername("sirs$user");
                        ds.setPassword("sirs$pwd");

                        final ParameterValueGroup params = H2FeatureStoreFactory.PARAMETERS_DESCRIPTOR.createValue();
                        Parameters.getOrCreate(H2FeatureStoreFactory.USER, params).setValue("sirs$user");
                        Parameters.getOrCreate(H2FeatureStoreFactory.PASSWORD, params).setValue("sirs$pwd");
                        Parameters.getOrCreate(H2FeatureStoreFactory.PORT, params).setValue(5555);
                        Parameters.getOrCreate(H2FeatureStoreFactory.DATABASE, params).setValue("sirs");
                        Parameters.getOrCreate(H2FeatureStoreFactory.HOST, params).setValue("localhost");
                        Parameters.getOrCreate(H2FeatureStoreFactory.NAMESPACE, params).setValue("no namespace");
                        Parameters.getOrCreate(H2FeatureStoreFactory.SIMPLETYPE, params).setValue(Boolean.FALSE);
                        Parameters.getOrCreate(H2FeatureStoreFactory.DATASOURCE, params).setValue(new DBCPDataSource(ds));

                        store = (JDBCFeatureStore) new H2FeatureStoreFactory().create(params);
                    }

                    return store;
                }
            }
        });
    }

    /**
     *
     * @return Path to the file storing H2 dump for this CouchDB database.
     */
    public Path getDBFile() {
        final SirsDBInfo sirs = couchDb.get(SirsDBInfo.class, INFO_DOCUMENT_ID);
        Path file = SirsCore.H2_PATH.resolve(sirs.getUuid());
        return file;
    }

    /**
     *
     * Dump database in a script file. Dump is performed asynchonously using a task.
     * @param file File to put dump into.
     * @return The task performing export (task is already running).
     */
    public Task dumbSchema(Path file) {
        return TaskManager.INSTANCE.submit(new Task() {

            @Override
            protected Object call() throws Exception {
                /**
                 * First, we check that database have already been exported. If
                 * not, we launch a new db export.
                 */
                synchronized (H2Helper.this) {
                    if (exportTask == null) {
                        export();
                    }
                }

                Platform.runLater(() -> {
                    if (exportTask.isRunning()) {
                        updateTitle(exportTask.getTitle());
                        exportTask.messageProperty().addListener((obs, oldValue, newValue) -> updateMessage(newValue));
                    }
                });

                // Ensure db export is finished before going further.
                if (!exportTask.get()) {
                    throw new IllegalStateException("Failed to export database !");
                }

                updateTitle("Export SQL");
                updateMessage("Export de la base SQL dans un script");

                final String create = "SCRIPT TO '" + file.resolve("sirs-schema.sql") + "' ";
                try (final Connection con = createConnection();
                        final Statement stat = con.createStatement()) {
                    stat.execute(create);
                }

                return null;
            }
        });
    }

    /**
     * A task which export a couchDb database content to SQL database.
     */
    public class ExportToRDBMS extends Task<Boolean> {

        public ExportToRDBMS() {
            updateTitle("Export vers la base RDBMS");
        }

        @Override
        protected Boolean call() throws Exception {
            updateMessage("Nettoyage de la base.");
            updateProgress(-1, -1);

            synchronized (H2Helper.this) {
                if (store != null) {
                    try {
                        Connection cnx = store.getDataSource().getConnection();
                        cnx.createStatement().executeUpdate("SHUTDOWN");
                    } finally {
                        store.close();
                        store = null;
                    }
                    store = null;
                }
            }

            final Path file = getDBFile();
            if (Files.isDirectory(file.getParent())) {
                Files.walkFileTree(file.getParent(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        Files.delete(dir);
                        return super.postVisitDirectory(dir, exc);
                    }

                    @Override
                    public FileVisitResult visitFile(Path file,
                            BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return super.visitFile(file, attrs);
                    }
                });
            }

            if (sqlHelpers == null || sqlHelpers.isEmpty()) {
                return false;
            }

            final Connection conn = createConnection();
            try {
                CreateSpatialExtension.initSpatialExtension(conn);
                String create = "SET REFERENTIAL_INTEGRITY FALSE ";
                try (final Statement stat = conn.createStatement()) {
                    stat.execute(create);
                }

                int srid = InjectorCore.getBean(SessionCore.class).getSrid();

                conn.setAutoCommit(false);

                /**
                 * First, we ask SQL helpers to create database structure.
                 */
                updateMessage("Création des tables et contraintes");
                for (SQLHelper sqlHelper : sqlHelpers) {
                    if (sqlHelper != null) {
                        sqlHelper.createTables(conn, srid);
                        sqlHelper.addForeignKeys(conn);
                    }
                }

                conn.commit();

                // Compute number of documents to export
                updateMessage("Analyse des éléments à exporter");
                List<String> allDocIds = couchDb.getAllDocIds();
                Iterator<String> idIt = allDocIds.iterator();
                while (idIt.hasNext()) {
                    if (UNMANAGED_IDS.matcher(idIt.next()).matches()) {
                        idIt.remove();
                    }
                }

                // Start document insertion
                DocHelper docHelper = new DocHelper(couchDb);
                try (final StreamingViewResult allDocsAsStream = docHelper.getAllDocsAsStream()) {

                    Iterator<ViewResult.Row> iterator = allDocsAsStream.iterator();

                    final Thread currentThread = Thread.currentThread();
                    int currentProgress = 0;
                    updateMessage("Insertion des éléments");
                    while (iterator.hasNext()) {
                        if (currentThread.isInterrupted() || isCancelled()) {
                            return false;
                        }

                        final ViewResult.Row currentRow = iterator.next();
                        if (UNMANAGED_IDS.matcher(currentRow.getId()).matches()) {
                            continue;
                        }

                        updateProgress(currentProgress++, allDocIds.size());
                        Optional<Element> element = docHelper.toElement(currentRow.getDocAsNode());
                        if (element.isPresent()) {
                            for (final SQLHelper sqlHelper : sqlHelpers) {
                                if (sqlHelper != null) {
                                    sqlHelper.insertElement(conn, element.get());
                                    conn.commit();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.close();
            }

            return true;
        }
    }
}
