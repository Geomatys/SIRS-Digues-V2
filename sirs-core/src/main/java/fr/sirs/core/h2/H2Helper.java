package fr.sirs.core.h2;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.h2.util.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sirs.core.DocHelper;
import fr.sirs.core.SirsCore;
import fr.sirs.core.SirsDBInfo;
import fr.sirs.core.model.sql.SQLHelper;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.db.h2.H2FeatureStore;
import org.geotoolkit.db.h2.H2FeatureStoreFactory;
import org.geotoolkit.jdbc.DBCPDataSource;
import org.geotoolkit.parameter.Parameters;
import org.opengis.parameter.ParameterValueGroup;

public class H2Helper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(H2Helper.class);
    
    public static void exportDataToRDBMS(CouchDbConnector connector)
            throws IOException {
        Path file = getDBFile(connector);

        if (Files.isDirectory(SirsCore.H2_PATH) && Files.exists(SirsCore.H2_PATH)) {
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
        try (Connection conn = createConnection(connector)) {
            init(conn, connector);
            
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    public static Connection createConnection(CouchDbConnector connector) throws SQLException {
        Path file = getDBFile(connector);
        return DriverManager.getConnection(
                "jdbc:h2:" + file.toString(), "sirs$user", "sirs$pwd");
    }
    
    public static FeatureStore createStore(CouchDbConnector connector) throws SQLException, DataStoreException {
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
        Parameters.getOrCreate(H2FeatureStoreFactory.DATASOURCE, params).setValue(ds);
        
        return new H2FeatureStoreFactory().create(params);
    }

    private static Path getDBFile(CouchDbConnector connector) {
        final SirsDBInfo sirs = connector.get(SirsDBInfo.class, "$sirs");
        Path file = SirsCore.H2_PATH.resolve(URLEncoder.encode(sirs.getUuid()));
        return file;
    }
    
    public static void init(Connection conn, CouchDbConnector db)
            throws SQLException {
        SQLHelper.createTables(conn);

        List<String> allDocIds = db.getAllDocIds();

        DocHelper docHelper = new DocHelper(db);

        for (String id : allDocIds) {
            if (id.startsWith("$"))
                continue;
            if (id.startsWith("_"))
                continue;
            try {
                conn.setAutoCommit(false);
                docHelper.getElement(id).map(
                        e -> SQLHelper.updateElement(conn, e));
                
                conn.commit();
            } catch (Exception e) {
                LOGGER.error("DocId: " + id, e);
                
            }

        }
        
        SQLHelper.addForeignKeys(conn);
        
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
   
}
