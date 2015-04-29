package fr.sirs.core.h2;

import java.io.IOException;
import java.net.URLEncoder;
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
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.db.h2.H2FeatureStoreFactory;
import org.geotoolkit.parameter.Parameters;
import org.h2.util.JdbcUtils;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.opengis.parameter.ParameterValueGroup;

import fr.sirs.core.DocHelper;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCore;
import fr.sirs.core.SirsDBInfo;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.sql.SQLHelper;
import java.util.Iterator;
import java.util.Optional;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javafx.concurrent.Task;
import org.ektorp.StreamingViewResult;
import org.ektorp.ViewResult;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.geotoolkit.jdbc.DBCPDataSource;

public class H2Helper {
    
    private static final Pattern UNMANAGED_IDS = Pattern.compile("^($|_).*");
    
    private static FeatureStore STORE = null;

    public static Task init() {
        if (STORE != null) {
            try {
                STORE.close();
            } catch (DataStoreException ex) {
                SirsCore.LOGGER.log(Level.WARNING, "Cannot close H2 feature store.", ex);
            }
            STORE = null;
        }
        
        return TaskManager.INSTANCE.submit(new H2Helper.ExportToRDBMS(InjectorCore.getBean(CouchDbConnector.class)));
    }
    
    public static Connection createConnection(CouchDbConnector connector) throws SQLException {
        Path file = getDBFile(connector);
        Connection connection = DriverManager.getConnection(
                "jdbc:h2:" + file.toString(), "sirs$user", "sirs$pwd");
        CreateSpatialExtension.initSpatialExtension(connection);
        return connection;
    }
    
    public static synchronized FeatureStore getStore(CouchDbConnector connector) throws SQLException, DataStoreException {
        if (STORE == null) {
            final Path file = getDBFile(connector);
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
            Parameters.getOrCreate(H2FeatureStoreFactory.SIMPLETYPE, params).setValue(Boolean.FALSE);
            Parameters.getOrCreate(H2FeatureStoreFactory.DATASOURCE, params).setValue(new DBCPDataSource(ds));

            STORE = new H2FeatureStoreFactory().create(params);
        }

        return STORE;
    }

    public static Path getDBFile(CouchDbConnector connector) {
        final SirsDBInfo sirs = connector.get(SirsDBInfo.class, "$sirs");
        Path file = SirsCore.H2_PATH.resolve(URLEncoder.encode(sirs.getUuid()));
        return file;
    }
    
    public static void dumbSchema(Connection connection, Path file) throws SQLException {
        Statement stat = null;
        String create = "SCRIPT TO '" + file.resolve("sirs-schema.sql") + "' ";
        try {
            stat = connection.createStatement();
            stat.execute(create);

        } finally {
            JdbcUtils.closeSilently(stat);
            JdbcUtils.closeSilently(connection);
        }
    }

    /**
     * A task which export a couchDb database content to SQL database.
     */
    public static class ExportToRDBMS extends Task<Boolean> {

        private final CouchDbConnector couchConnector;

        public ExportToRDBMS(CouchDbConnector connector) {
            couchConnector = connector;
            updateTitle("Export vers la base RDBMS");
        }

        @Override
        protected Boolean call() throws Exception {
            updateMessage("Nettoyage de la base.");
            Path file = getDBFile(couchConnector);
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

            try (Connection conn = createConnection(couchConnector)) {
                updateMessage("Création des tables.");
                int srid = InjectorCore.getBean(SessionCore.class).getSrid();
                SQLHelper.createTables(conn, srid);
                conn.setAutoCommit(false);

                updateMessage("Création de la liste des éléments à insérer.");
                List<String> allDocIds = couchConnector.getAllDocIds();
                Iterator<String> idIt = allDocIds.iterator();
                while (idIt.hasNext()) {
                    if (UNMANAGED_IDS.matcher(idIt.next()).matches()) {
                        idIt.remove();
                    }
                }
                DocHelper docHelper = new DocHelper(couchConnector);

                updateMessage("Insertion des éléments");
                int currentProgress = 0;
                updateProgress(currentProgress, allDocIds.size());
                final Thread currentThread = Thread.currentThread();

                try (final StreamingViewResult allDocsAsStream = docHelper.getAllDocsAsStream()) {
                    Iterator<ViewResult.Row> iterator = allDocsAsStream.iterator();
                    while (iterator.hasNext()) {
                        if (currentThread.isInterrupted()) {
                            return false;
                        }

                        final ViewResult.Row currentRow = iterator.next();
                        if (UNMANAGED_IDS.matcher(currentRow.getId()).matches()) {
                            continue;
                        }

                        Optional<Element> element = docHelper.toElement(currentRow.getDocAsNode());
                        if (element.isPresent()) {
                            SQLHelper.insertElement(conn, element.get());
                            conn.commit();
                            updateProgress(currentProgress++, allDocIds.size());
                        }
                    }

                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
                
                updateProgress(-1, -1);
                updateMessage("Mise à jour des clés étrangères");
                SQLHelper.addForeignKeys(conn);
            }
            return true;
        }
    }
}
