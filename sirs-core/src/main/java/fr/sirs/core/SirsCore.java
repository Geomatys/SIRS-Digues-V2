package fr.sirs.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.apache.sis.util.logging.Logging;


public class SirsCore {

    public static final Logger LOGGER = Logging.getLogger(SirsCore.class);
    public static final String NAME = "sirs";
    
    public static final Path CONFIGURATION_PATH;
    static {
        Path tmpPath = Paths.get(System.getProperty("user.home"), "."+NAME);
        if (!Files.isDirectory(tmpPath)) {
            try {
                Files.createDirectory(tmpPath);
            } catch (IOException ex) {
                try {
                    tmpPath = Files.createTempDirectory(NAME);
                } catch (IOException ex1) {
                    ex.addSuppressed(ex1);
                    throw new ExceptionInInitializerError(ex);
                }
            }
        }
        CONFIGURATION_PATH = tmpPath;
    }
    
    public static final Path DATABASE_PATH = CONFIGURATION_PATH.resolve("database");
    
    public static final Path H2_PATH = CONFIGURATION_PATH.resolve("h2");
    
    public static final Path PLUGINS_PATH = CONFIGURATION_PATH.resolve("plugins");
    
    public static final Path LOGS_PATH = CONFIGURATION_PATH.resolve("logs.txt");
    
    public static final Path ERR_LOGS_PATH = CONFIGURATION_PATH.resolve("errors.log");
    
    /**
     * User directory root folder.
     * 
     * @return {user.home}/.sirs
     */
    public static String getConfigPath(){
        return CONFIGURATION_PATH.toString();
    }
    
    public static File getConfigFolder(){
        return CONFIGURATION_PATH.toFile();
    }
    
    public static Path getDatabaseFolder(){
        return DATABASE_PATH;
    }
    
}
