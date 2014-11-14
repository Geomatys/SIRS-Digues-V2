package fr.sirs.core;

import java.io.File;
import java.util.logging.Logger;

import org.apache.sis.util.logging.Logging;


public class SirsCore {

    public static final Logger LOGGER = Logging.getLogger(SirsCore.class);
    public static final String NAME = "sirs";
	
    /**
     * User directory root folder.
     * 
     * @return {user.home}/.sirs
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
    
}
