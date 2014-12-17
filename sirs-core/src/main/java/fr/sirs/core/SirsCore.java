package fr.sirs.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.sis.util.logging.Logging;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.geotoolkit.referencing.factory.epsg.EpsgInstaller;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;


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
    
    public static final Path EPSG_PATH = CONFIGURATION_PATH.resolve("EPSG");
    
    private static CoordinateReferenceSystem PROJECTION;
    
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

    public static void setEpsgCode(String epsgCode) {
        try {
            PROJECTION  = CRS.decode(epsgCode);
        } catch (FactoryException e) {
            throw new SirsCoreRuntimeExecption(e);
         }
        
    }

    public static CoordinateReferenceSystem getEpsgCode() {
        return PROJECTION;
    } 
    
    /**
     * Initialise la base EPSG utilisée par l'application. Si elle n'existe pas, 
     * elle sera créée. Dans tous les cas, on force le chargement de la base 
     * dans le système, ce qui permet de lever les potentiels problèmes au 
     * démarrage.
     * @throws FactoryException
     * @throws IOException 
     */
    public static void initEpsgDB() throws FactoryException, IOException {
        // create a database in user directory
        Files.createDirectories(SirsCore.EPSG_PATH);
        
        final String url = "jdbc:derby:" + SirsCore.EPSG_PATH.toString()
                + File.separator + "EPSG;create=true";
        
        final DataSource ds = new DefaultDataSource(url);
        Hints.putSystemDefault(Hints.EPSG_DATA_SOURCE, ds);
        final EpsgInstaller installer = new EpsgInstaller();
        installer.setDatabase(url);
        if (!installer.exists()) {
            installer.call();
        }

        // force loading epsg
        CRS.decode("EPSG:3395");
    }

    public static int getSrid() {
        try {
            return IdentifiedObjects.lookupEpsgCode(SirsCore.getEpsgCode(), true);
        } catch (FactoryException e) {
            throw new SirsCoreRuntimeExecption(e);
        }
    }
}
