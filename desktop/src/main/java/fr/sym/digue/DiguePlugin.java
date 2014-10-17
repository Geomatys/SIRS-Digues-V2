

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
import fr.sym.digue.theme.DisorderTheme;
import fr.sym.digue.theme.DocumentsTheme;
import fr.sym.digue.theme.FreeBordTheme;
import fr.sym.digue.theme.HydraulicsMesuresTheme;
import fr.sym.digue.theme.PhotosTheme;
import fr.sym.digue.theme.PresentationsTheme;
import fr.sym.digue.theme.RoadNetworkTheme;
import fr.sym.digue.theme.SeaWorkTheme;
import fr.sym.digue.theme.StructureTheme;
import fr.sym.digue.theme.TownAreaTheme;
import fr.sym.digue.theme.WorksNetwork;

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
