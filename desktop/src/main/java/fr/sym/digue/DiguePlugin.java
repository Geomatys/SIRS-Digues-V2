

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
import fr.sym.theme.ContactsTheme;
import fr.sym.theme.DesordreTheme;
import fr.sym.theme.DocumentsTheme;
import fr.sym.theme.EmpriseCommunaleTheme;
import fr.sym.theme.EvenementsHydrauliquesTheme;
import fr.sym.theme.FrancBordTheme;
import fr.sym.theme.MesureEvenementsTheme;
import fr.sym.theme.PrestationsTheme;
import fr.sym.theme.ProfilsEnTraversTheme;
import fr.sym.theme.ReseauxDeVoirieTheme;
import fr.sym.theme.ReseauxEtOuvragesTheme;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DiguePlugin extends Plugin{

    public DiguePlugin() {
        themes.add(new FrancBordTheme());
        themes.add(new ReseauxDeVoirieTheme());
        themes.add(new ReseauxEtOuvragesTheme());
        themes.add(new DesordreTheme());
        themes.add(new PrestationsTheme());
        themes.add(new MesureEvenementsTheme());
        themes.add(new EmpriseCommunaleTheme());
        themes.add(new ProfilsEnTraversTheme());
        themes.add(new ContactsTheme());
        themes.add(new EvenementsHydrauliquesTheme());
        themes.add(new DocumentsTheme());
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
