package fr.sirs.core;

import org.geotoolkit.gui.javafx.util.TaskManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import javax.sql.DataSource;
import org.apache.sis.geometry.GeneralEnvelope;

import org.apache.sis.util.logging.Logging;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.internal.referencing.CRSUtilities;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.geotoolkit.referencing.factory.epsg.EpsgInstaller;
import org.opengis.geometry.Envelope;
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
    
    public static final Path EPSG_PATH = CONFIGURATION_PATH.resolve("EPSG");
    
    public static final Path ELASTIC_SEARCH_PATH = CONFIGURATION_PATH.resolve("elasticSearch");
    
    public static final Path LOCAL_QUERIES_PATH = CONFIGURATION_PATH.resolve("queries.properties");
    
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
        
        final String url = "jdbc:hsqldb:file:" + SirsCore.EPSG_PATH.toString()+"/db";
        
        final DataSource ds = new DefaultDataSource(url);
        Hints.putSystemDefault(Hints.EPSG_DATA_SOURCE, ds);
        
        final EpsgInstaller installer = new EpsgInstaller();
        installer.setDatabase(url);
        if (!installer.exists()) {
            installer.call();
        }

        // work in lazy mode, do your best for lenient datum shift
        Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
        
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
    
    public static String getVersion() {
        return SirsCore.class.getPackage().getImplementationVersion();
    }

    /**
     * 
     * @return the application task manager, designed to start users tasks in a 
     * separate thread pool.
     */
    public static TaskManager getTaskManager() {
        return TaskManager.INSTANCE;
    }
    
        /**
     * Try to expand a little an envelope. Main purpose is to ensure we won't 
     * have an envelope which is merely a point.
     * @param input The input to expand.
     * @return An expanded envelope. If we cannot analyze CRS or it's unit on
     * horizontal axis, the same envelope is returned.
     */
    public static Envelope pseudoBuffer(final Envelope input) {
        double additionalDistance = 0.01;
        if (input.getCoordinateReferenceSystem() != null) {
            CoordinateReferenceSystem crs = input.getCoordinateReferenceSystem();
            int firstAxis = CRSUtilities.firstHorizontalAxis(crs);
            
            if (firstAxis >=0) {
                Unit unit = crs.getCoordinateSystem().getAxis(firstAxis).getUnit();
                if (unit != null && SI.METRE.isCompatible(unit)) {
                    additionalDistance = SI.METRE.getConverterTo(unit).convert(1);
                }
                
                final GeneralEnvelope result = new GeneralEnvelope(input);
                result.setRange(firstAxis, 
                        result.getLower(firstAxis)-additionalDistance, 
                        result.getUpper(firstAxis)+additionalDistance);
                final int secondAxis = firstAxis +1;
                result.setRange(secondAxis, 
                        result.getLower(secondAxis)-additionalDistance, 
                        result.getUpper(secondAxis)+additionalDistance);
                return result;
            }
        }
        return input;
    }
}
