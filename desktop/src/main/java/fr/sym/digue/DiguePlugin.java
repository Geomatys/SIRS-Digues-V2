

package fr.sym.digue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.h2.jdbcx.JdbcConnectionPool;

import fr.sym.Plugin;
import fr.sym.Symadrem;
import fr.sym.theme.DisorderTheme;
import fr.sym.theme.DocumentsTheme;
import fr.sym.theme.FreeBordTheme;
import fr.sym.theme.HydraulicsMesuresTheme;
import fr.sym.theme.PhotosTheme;
import fr.sym.theme.PresentationsTheme;
import fr.sym.theme.RoadNetworkTheme;
import fr.sym.theme.SeaWorkTheme;
import fr.sym.theme.StructureTheme;
import fr.sym.theme.TownAreaTheme;
import fr.sym.theme.WorksNetwork;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DiguePlugin extends Plugin{

    public DiguePlugin() {
        themes.add(new DisorderTheme());
        themes.add(new DocumentsTheme());
        themes.add(new FreeBordTheme());
        themes.add(new HydraulicsMesuresTheme());
        themes.add(new PhotosTheme());
        themes.add(new PresentationsTheme());
        themes.add(new RoadNetworkTheme());
        themes.add(new SeaWorkTheme());
        themes.add(new StructureTheme());
        themes.add(new TownAreaTheme());
        themes.add(new WorksNetwork());
    }

    @Override
    public void load() throws SQLException, IOException {
        
        //check that table exist
        final JdbcConnectionPool pool = Symadrem.getConnectionPool();
        try(final Connection cnx = pool.getConnection()){
            Liquibase liquibase = null;
                Database database;
                try {
                    database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(cnx));
                    liquibase = new Liquibase("fr/symadrem/sirs/db/db-changelog.xml", new ClassLoaderResourceAccessor(), database);
                    liquibase.update("");
                    database.close();
                } catch (LiquibaseException e) {
                    e.printStackTrace();
                }
            
        }
    }

}
