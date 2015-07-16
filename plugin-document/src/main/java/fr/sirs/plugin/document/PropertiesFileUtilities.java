
package fr.sirs.plugin.document;

import fr.sirs.core.model.Digue;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.plugin.document.ui.DocumentsPane;
import static fr.sirs.plugin.document.ui.DocumentsPane.UNCLASSIFIED;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;

/**
 *
 * @author guilhem
 */
public class PropertiesFileUtilities {
    
    private static final Logger LOGGER = Logging.getLogger(PropertiesFileUtilities.class);
    
    public static String getInventoryNumber(final File f) {
        final Properties prop = getSirsProperties(f);
        return prop.getProperty(f.getName() + "_inventory_number", "");
    }
    
    public static void setInventoryNumber(final File f, String value) {
        final Properties prop   = getSirsProperties(f);
        prop.put(f.getName() + "_inventory_number", value);
        
        try {
            final File sirsPropFile = getSirsPropertiesFile(f);
            prop.store(new FileWriter(sirsPropFile), "");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Erro while accessing sirs properties file.", ex);
        }
    }
    
    public static void removeInventoryNumber(final File f) {
        final Properties prop   = getSirsProperties(f);
        prop.remove(f.getName() + "_inventory_number");
        
        try {
            final File sirsPropFile = getSirsPropertiesFile(f);
            prop.store(new FileWriter(sirsPropFile), "");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Erro while accessing sirs properties file.", ex);
        }
    }
    
    public static String getClassPlace(final File f) {
        final Properties prop = getSirsProperties(f);
        return prop.getProperty(f.getName() + "_class_place", "");
    }
    
    public static void setClassPlace(final File f, String value) {
        final Properties prop   = getSirsProperties(f);
        prop.put(f.getName() + "_class_place", value);
        
        try {
            final File sirsPropFile = getSirsPropertiesFile(f);
            prop.store(new FileWriter(sirsPropFile), "");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Erro while accessing sirs properties file.", ex);
        }
    }
    
    public static void removeClassPlace(final File f) {
        final Properties prop   = getSirsProperties(f);
        prop.remove(f.getName() + "_class_place");
        
        try {
            final File sirsPropFile = getSirsPropertiesFile(f);
            prop.store(new FileWriter(sirsPropFile), "");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Erro while accessing sirs properties file.", ex);
        }
    }
    
    public static Boolean getDOIntegrated(final File f) {
        final Properties prop = getSirsProperties(f);
        return Boolean.parseBoolean(prop.getProperty(f.getName() + "_do_integrated", "false"));
    }
    
    public static void setDOIntegrated(final File f, boolean value) {
        final Properties prop   = getSirsProperties(f);
        prop.put(f.getName() + "_do_integrated", Boolean.toString(value));
        
        try {
            final File sirsPropFile = getSirsPropertiesFile(f);
            prop.store(new FileWriter(sirsPropFile), "");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Erro while accessing sirs properties file.", ex);
        }
    }
    
    public static Boolean getIsModelFolder(final File f) {
        return getIsSe(f) || getIsDg(f) || getIsTr(f);
    }
    
    public static Boolean getIsSe(final File f) {
        final Properties prop = getSirsProperties(f);
        return Boolean.parseBoolean(prop.getProperty(f.getName() + "_se", "false"));
    }
    
    public static void setIsSe(final File f, boolean value) {
        final Properties prop   = getSirsProperties(f);
        prop.put(f.getName() + "_se", Boolean.toString(value));
        
        try {
            final File sirsPropFile = getSirsPropertiesFile(f);
            prop.store(new FileWriter(sirsPropFile), "");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Erro while accessing sirs properties file.", ex);
        }
    }
    
    public static Boolean getIsTr(final File f) {
        final Properties prop = getSirsProperties(f);
        return Boolean.parseBoolean(prop.getProperty(f.getName() + "_tr", "false"));
    }
    
    public static void setIsTr(final File f, boolean value) {
        final Properties prop   = getSirsProperties(f);
        prop.put(f.getName() + "_tr", Boolean.toString(value));
        
        try {
            final File sirsPropFile = getSirsPropertiesFile(f);
            prop.store(new FileWriter(sirsPropFile), "");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Erro while accessing sirs properties file.", ex);
        }
    }
    
    public static Boolean getIsDg(final File f) {
        final Properties prop = getSirsProperties(f);
        return Boolean.parseBoolean(prop.getProperty(f.getName() + "_dg", "false"));
    }
    
    public static void setIsDg(final File f, boolean value) {
        final Properties prop   = getSirsProperties(f);
        prop.put(f.getName() + "_dg", Boolean.toString(value));
        
        try {
            final File sirsPropFile = getSirsPropertiesFile(f);
            prop.store(new FileWriter(sirsPropFile), "");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Erro while accessing sirs properties file.", ex);
        }
    }
    
    public static void removeDOIntegrated(final File f) {
        final Properties prop   = getSirsProperties(f);
        prop.remove(f.getName() + "_do_integrated");
        
        try {
            final File sirsPropFile = getSirsPropertiesFile(f);
            prop.store(new FileWriter(sirsPropFile), "");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Erro while accessing sirs properties file.", ex);
        }
    }
    
    public static File getSirsPropertiesFile(final File f) throws IOException {
        return getSirsPropertiesFile(f, true);
    }
    
    public static File getSirsPropertiesFile(final File f, final boolean parent) throws IOException {
        final File parentFile;
        if (parent) {
            parentFile = f.getParentFile();
        } else {
            parentFile = f;
        }
        if (parentFile != null) {
            final File sirsPropFile = new File(parentFile, "sirs.properties");
            if (!sirsPropFile.exists()) {
                sirsPropFile.createNewFile();
            }
            return sirsPropFile;
        }
        return null;
    }
    
    public static Properties getSirsProperties(final File f) {
        return getSirsProperties(f, true);
    }
    
    public static Properties getSirsProperties(final File f, final boolean parent) {
        final Properties prop = new Properties();
        try {
            final File sirsPropFile = getSirsPropertiesFile(f, parent);
            if (sirsPropFile != null) {
                prop.load(new FileReader(sirsPropFile));
            } 
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Erro while loading/creating sirs properties file.", ex);
        }
        return prop;
    }
    
    public static String getStringSizeFile(final File f) {
        final long size        = getFileSize(f);
        final DecimalFormat df = new DecimalFormat("0.0");
        final float sizeKb     = 1024.0f;
        final float sizeMo     = sizeKb * sizeKb;
        final float sizeGo     = sizeMo * sizeKb;
        final float sizeTerra  = sizeGo * sizeKb;

        if (size < sizeKb) {
            return df.format(size)          + " o";
        } else if (size < sizeMo) {
            return df.format(size / sizeKb) + " Ko";
        } else if (size < sizeGo) {
            return df.format(size / sizeMo) + " Mo";
        } else if (size < sizeTerra) {
            return df.format(size / sizeGo) + " Go";
        }
        return "";
    }
    
    public static long getFileSize(final File f) {
        if (f.isDirectory()) {
            long result = 0;
            for (File child : f.listFiles()) {
                result += getFileSize(child);
            }
            return result;
        } else {
            return f.length();
        }
    }
    
    public static File getOrCreateSE(final File rootDirectory, SystemeEndiguement sd){
        String name = sd.getLibelle();
        if (name == null) {
            name = "null";
        }
        final File sdDir = new File(rootDirectory, name);
        if (!sdDir.exists()) {
            sdDir.mkdir();
        }
        setIsSe(sdDir, true);
        final File docDir = new File(sdDir, DocumentsPane.DOCUMENT_FOLDER); 
        if (!docDir.exists()) {
            docDir.mkdir();
        }
        return sdDir;
    }
    
    public static File getOrCreateDG(final File rootDirectory, Digue digue){
        String name = digue.getLibelle();
        if (name == null) {
            name = "null";
        }
        final File digueDir = new File(rootDirectory, name);
        if (!digueDir.exists()) {
            digueDir.mkdir();
        }
        setIsDg(digueDir, true);
        final File docDir = new File(digueDir, DocumentsPane.DOCUMENT_FOLDER); 
        if (!docDir.exists()) {
            docDir.mkdir();
        }
        return digueDir;
    }
    
    public static File getOrCreateTR(final File rootDirectory, TronconDigue tr){
        String name = tr.getLibelle();
        if (name == null) {
            name = "null";
        }
        final File trDir = new File(rootDirectory, name);
        if (!trDir.exists()) {
            trDir.mkdir();
        }
        setIsTr(trDir, true);
        final File docDir = new File(trDir, DocumentsPane.DOCUMENT_FOLDER); 
        if (!docDir.exists()) {
            docDir.mkdir();
        }
        return trDir;
    }
    
    public static File getOrCreateUnclassif(final File rootDirectory){
        final File unclassifiedDir = new File(rootDirectory, UNCLASSIFIED); 
        if (!unclassifiedDir.exists()) {
            unclassifiedDir.mkdir();
        }
        
        final File docDir = new File(unclassifiedDir, DocumentsPane.DOCUMENT_FOLDER); 
        if (!docDir.exists()) {
            docDir.mkdir();
        }
        return unclassifiedDir;
    }
    
    public static String getExistingDatabaseIdentifier(final File rootDirectory) {
        Properties prop = getSirsProperties(rootDirectory, false);
        return (String) prop.get("database_identifier");
    }
    
    public static void setDatabaseIdentifier(final File rootDirectory, final String key) {
        Properties prop = getSirsProperties(rootDirectory, false);
        prop.put("database_identifier", key);
        
        try {
            final File sirsPropFile = getSirsPropertiesFile(rootDirectory, false);
            prop.store(new FileWriter(sirsPropFile), "");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Erro while accessing sirs properties file.", ex);
        }
    }
    
}
