package fr.sirs.core.h2;

import java.io.File;
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
import java.util.List;
import java.util.Optional;

import org.ektorp.CouchDbConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sirs.core.DocHelper;
import fr.sirs.core.SirsCore;
import fr.sirs.core.SirsDBInfo;
import fr.sirs.core.model.sql.SQLHelper;

public class H2Helper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(H2Helper.class);
    
    public static void exportDataToRDBMS(CouchDbConnector connector)
            throws IOException {
        final SirsDBInfo sirs = connector.get(SirsDBInfo.class, "$sirs");
        Path file = SirsCore.H2_PATH.resolve(URLEncoder.encode(sirs.getUuid()));

        if (Files.isDirectory(SirsCore.H2_PATH)) {
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
        try (Connection conn = DriverManager.getConnection(
                "jdbc:h2:" + file.toString(), "sirs$user", "sirs$pwd")) {
            init(conn, connector);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }

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

        conn.close();

    }
}
