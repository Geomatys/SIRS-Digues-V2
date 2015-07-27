package fr.sirs.core.model.sql;

import fr.sirs.core.model.Element;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public interface SQLHelper {
    
    void createTables(Connection conn, int srid) throws SQLException;

    void addForeignKeys(Connection conn) throws SQLException;

    boolean insertElement(Connection conn, Element element);

    boolean updateElement(Connection conn, Element element);

}
