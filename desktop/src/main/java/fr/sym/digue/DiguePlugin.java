

package fr.sym.digue;

import fr.sym.Symadrem;
import fr.sym.Plugin;
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
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.geotoolkit.util.FileUtilities;
import org.h2.jdbcx.JdbcConnectionPool;

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
        
            final DatabaseMetaData metadata = cnx.getMetaData();
            ResultSet rs = metadata.getTables(null, null, "DIGUE", null);
            final boolean loaded = rs.next();
            rs.close();

            if(!loaded){
                //run table creation script
                final Statement stmt = cnx.createStatement();
                String script = FileUtilities.getStringFromStream(DiguePlugin.class.getResourceAsStream("/fr/sym/db/ADdbDonnees_create.sql"));
                //remove comments
                script = script.replaceAll("--.*\n", "");
                
                final String[] parts = script.split(";");
                for(String str : parts){
                    str = str.trim();
                    if(str.isEmpty()) continue;
                    stmt.executeUpdate(str);
                }
                stmt.close();
                cnx.commit();
            }
            
        }
    }
    
}
