

package fr.sym;

import java.io.File;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.h2.jdbcx.JdbcConnectionPool;

/**
 * Constants used for project.
 * 
 * @author Johann Sorel
 */
public final class Symadrem {
    
    public static final String NAME = "symadrem";
    public static final Logger LOGGER = Logging.getLogger(Symadrem.class);
    
    private static JdbcConnectionPool CNX_POOL;
    
    private Symadrem(){};
    
    /**
     * User directory root folder.
     * 
     * @return {user.home}/.symadrem
     */
    public static String getConfigPath(){
        final String userHome = System.getProperty("user.home");
        return userHome+File.separator+"."+NAME;
    }
    
    public static File getConfigFolder(){
        final String userHome = System.getProperty("user.home");
        return new File(userHome+File.separator+"."+NAME);
    }
    
    public static File getDatabaseFolder(){
        return new File(getConfigFolder(), "database");
    }
    
    public static synchronized JdbcConnectionPool getConnectionPool(){
        if(CNX_POOL==null){
            final File dbFile = new File(getDatabaseFolder(),"database");
            CNX_POOL = JdbcConnectionPool.create("jdbc:h2:"+dbFile.getPath(), "symadrem", "symadrem");
        }
        return CNX_POOL;
    }
    
}
