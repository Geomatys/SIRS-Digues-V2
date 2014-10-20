

package fr.sym;

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

import static fr.sym.Session.PROJECTION;
import fr.sym.store.SymadremStore;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.RandomStyleBuilder;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CorePlugin extends Plugin{

    public CorePlugin() {
    }

    @Override
    public List<MapItem> getMapItems() {
        final List<MapItem> items = new ArrayList<>();
        
        try{
            final SymadremStore symStore = new SymadremStore(session,null,PROJECTION);
            final org.geotoolkit.data.session.Session symSession = symStore.createSession(false);
            for(Name name : symStore.getNames()){
                final FeatureCollection col = symSession.getFeatureCollection(QueryBuilder.all(name));
                final MutableStyle style = RandomStyleBuilder.createRandomVectorStyle(col.getFeatureType());
                final FeatureMapLayer fml = MapBuilder.createFeatureLayer(col, style);
                fml.setName(name.getLocalPart());
                items.add(fml);
            }
        }catch(DataStoreException ex){
            Symadrem.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        
        return items;
    }

    @Override
    public void load() throws SQLException, IOException {
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
