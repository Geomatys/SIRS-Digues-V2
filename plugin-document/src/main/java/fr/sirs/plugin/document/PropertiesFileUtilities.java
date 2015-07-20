
package fr.sirs.plugin.document;

import fr.sirs.core.model.Digue;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.plugin.document.ui.DocumentsPane;
import static fr.sirs.plugin.document.ui.DocumentsPane.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.util.FileUtilities;

/**
 * Utility class managing the properties file adding different properties to the filesystem objects.
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class PropertiesFileUtilities {
    
    private static final Logger LOGGER = Logging.getLogger(PropertiesFileUtilities.class);
    
    /**
     * Extract a property in the sirs.properties file coupled to the specified file.
     * 
     * @param f A file, can be a folder correspounding to a SE, DG or TR. Or a simple file.
     * @param property Name of the property.
     * @return 
     */
    public static String getProperty(final File f, final String property) {
        final Properties prop = getSirsProperties(f, true);
        return prop.getProperty(f.getName() + "_" + property, "");
    }
    
    /**
     * Set a property in the sirs.properties file coupled to the specified file.
     * 
     * @param f A file, can be a folder correspounding to a SE, DG or TR. Or a simple file.
     * @param property Name of the property.
     * @param value The value to set.
     */
    public static void setProperty(final File f, final String property, final String value) {
        final Properties prop   = getSirsProperties(f, true);
        prop.put(f.getName() + "_" + property, value);
        storeSirsProperties(prop, f, true);
    }
    
    /**
     * Remove a property in the sirs.properties file coupled to the specified file.
     * @param f A file, can be a folder correspounding to a SE, DG or TR. Or a simple file.
     * @param property Name of the property.
     */
    public static void removeProperty(final File f, final String property) {
        final Properties prop   = getSirsProperties(f, true);
        prop.remove(f.getName() + "_" + property);
        storeSirsProperties(prop, f, true);
    }
    
    /**
     * Extract a property in the sirs.properties file coupled to the specified file.
     * 
     * @param f A file, can be a folder correspounding to a SE, DG or TR. Or a simple file.
     * @param property Name of the property.
     * @return 
     */
    public static Boolean getBooleanProperty(final File f, final String property) {
        final Properties prop = getSirsProperties(f, true);
        return Boolean.parseBoolean(prop.getProperty(f.getName() + "_" + property, "false"));
    }
    
    /**
     * Set a property in the sirs.properties file coupled to the specified file.
     * 
     * @param f A file, can be a folder correspounding to a SE, DG or TR. Or a simple file.
     * @param property Name of the property.
     * @param value The value to set.
     */
    public static void setBooleanProperty(final File f, final String property, boolean value) {
        final Properties prop   = getSirsProperties(f, true);
        prop.put(f.getName() + "_" + property, Boolean.toString(value));
        
        storeSirsProperties(prop, f, true);
    }
    
    /**
     * Return true if the specified file correspound to a a SE, DG or TR folder.
     * 
     * @param f A file.
     * @return 
     */
    public static Boolean getIsModelFolder(final File f) {
        return getIsModelFolder(f, SE) || getIsModelFolder(f, TR) || getIsModelFolder(f, DG);
    }
    
    /**
     * Return true if the specified file correspound to a a specific specified model (SE, DG or TR).
     * @param f A file.
     * @param model SE, DG or TR.
     * @return 
     */
    public static Boolean getIsModelFolder(final File f, final String model) {
        final Properties prop = getSirsProperties(f, true);
        return Boolean.parseBoolean(prop.getProperty(f.getName() + "_" + model, "false"));
    }
    
    /**
     * Set the specific specified model (SE, DG or TR) for a folder.
     * 
     * @param f A model folder.
     * @param model SE, DG or TR.
     * @param libelle The name that will be displayed in UI.
     */
    private static void setIsModelFolder(final File f, final String model, final String libelle) {
        final Properties prop   = getSirsProperties(f, true);
        prop.put(f.getName() + "_" + model, "true");
        prop.put(f.getName() + "_" + LIBELLE, libelle);
        
       storeSirsProperties(prop, f, true);
    }
    
    /**
     * Remove all properties coupled to the specified file.
     * 
     * @param f A file.
     */
    public static void removeProperties(final File f) {
        final Properties prop   = getSirsProperties(f, true);
        
        Set<Entry<Object,Object>> properties = new HashSet<>(prop.entrySet());
        for (Entry<Object,Object> entry : properties) {
            if (((String)entry.getKey()).startsWith(f.getName())) {
                prop.remove(entry.getKey());
            }
        }
        //save cleaned properties file
        storeSirsProperties(prop, f, true);
    }
    
    /**
     * Store the updated properties to the sirs file.
     * 
     * @param prop the updated properties. (will replace the previous one in the file).
     * @param f The file adding properties (not the sirs file).
     * @param parent {@code true} if the file f is not the root directory.
     */
    private static void storeSirsProperties(final Properties prop, final File f, boolean parent) {
        try {
            final File sirsPropFile = getSirsPropertiesFile(f, parent);
            prop.store(new FileWriter(sirsPropFile), "");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error while accessing sirs properties file.", ex);
        }
    }
    
    /**
     * Get or create a sirs.properties file next to the specified one (or in the directory if parent is set to false)
     * 
     * @param f A file.
     * @param parent {@code true} if the file f is not the root directory.
     * 
     * @return A sirs.properties file.
     * @throws IOException 
     */
    private static File getSirsPropertiesFile(final File f, final boolean parent) throws IOException {
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
    
    /**
     * Return the Properties associated with all the files next to the one specified (or in the directory if parent is set to false).
     * 
     * @param f A file.
     * @param parent {@code true} if the file f is not the root directory.
     * @return 
     */
    private static Properties getSirsProperties(final File f, final boolean parent) {
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
    
    /**
     * Return a label for a file size (if it is a directory the all the added size of its children).
     * 
     * @param f A file.
     * @return 
     */
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
    
    /**
     * Return the size of a file (if it is a directory the all the added size of its children).
     * @param f
     * @return 
     */
    private static long getFileSize(final File f) {
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
        final File sdDir = new File(rootDirectory, sd.getId());
        if (!sdDir.exists()) {
            sdDir.mkdir();
        }
        String name = sd.getLibelle();
        if (name == null) {
            name = "null";
        }
        setIsModelFolder(sdDir, SE, name);
        final File docDir = new File(sdDir, DocumentsPane.DOCUMENT_FOLDER); 
        if (!docDir.exists()) {
            docDir.mkdir();
        }
        return sdDir;
    }
    
    public static File getOrCreateDG(final File rootDirectory, Digue digue){
        final File digueDir = new File(rootDirectory, digue.getId());
        if (!digueDir.exists()) {
            digueDir.mkdir();
        }
        String name = digue.getLibelle();
        if (name == null) {
            name = "null";
        }
        setIsModelFolder(digueDir, DG, name);
        final File docDir = new File(digueDir, DocumentsPane.DOCUMENT_FOLDER); 
        if (!docDir.exists()) {
            docDir.mkdir();
        }
        return digueDir;
    }
    
    public static File getOrCreateTR(final File rootDirectory, TronconDigue tr){
        final File trDir = new File(rootDirectory, tr.getId());
        if (!trDir.exists()) {
            trDir.mkdir();
        }
        String name = tr.getLibelle();
        if (name == null) {
            name = "null";
        }
        setIsModelFolder(trDir, TR, name);
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
        final Properties prop = getSirsProperties(rootDirectory, false);
        return (String) prop.get("database_identifier");
    }
    
    public static void setDatabaseIdentifier(final File rootDirectory, final String key) {
        final Properties prop = getSirsProperties(rootDirectory, false);
        prop.put("database_identifier", key);
        
        storeSirsProperties(prop, rootDirectory, false);
    }
 
    public static void backupDirectories(final File saveDir, final Collection<File> files) {
        for (File f : files) {
            backupDirectory(saveDir, f);
        }
    }
    
    public static void backupDirectory(final File saveDir, final File f) {
        
        // extract properties
        final Map<Object, Object> extracted  = new HashMap<>();
        final Properties prop                = getSirsProperties(f, true);
        Set<Entry<Object,Object>> properties = new HashSet<>(prop.entrySet());
        for (Entry<Object,Object> entry : properties) {
            if (((String)entry.getKey()).startsWith(f.getName())) {
                extracted.put(entry.getKey(), entry.getValue());
                prop.remove(entry.getKey());
            }
        }
        
        //save cleaned properties file
        storeSirsProperties(prop, f, true);
        
        
        final File newDir = new File(saveDir, f.getName());
        try {
            // we copy only the "dossier d'ouvrage" directory
            if (!newDir.exists()) {
                newDir.mkdir();
            }
            
            final File doFile    = new File(f, DOCUMENT_FOLDER);
            final File newDoFile = new File(newDir, DOCUMENT_FOLDER);
            
            FileUtilities.copy(doFile, newDoFile);
            FileUtilities.deleteDirectory(f);
            
            // save new properties
            final Properties newProp = getSirsProperties(newDir, true);
            newProp.putAll(extracted);
            
            storeSirsProperties(newProp, newDir, true);
            
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error while moving destroyed obj to backup folder", ex);
        }
    }
    
    public  static Set<File> listModel(final File rootDirectory, final String model) {
        Set<File> modelList = new HashSet<>();
        listModel(rootDirectory, modelList, model, true);
        return modelList;
    }
    
    public  static Set<File> listModel(final File rootDirectory, final String model, final boolean deep) {
        Set<File> modelList = new HashSet<>();
        listModel(rootDirectory, modelList, model, deep);
        return modelList;
    }
    
    private static void listModel(final File rootDirectory, Set<File> modelList, String model, final boolean deep) {
        for (File f : rootDirectory.listFiles()) {
            if (f.isDirectory()) {
                if (getIsModelFolder(f, model)) {
                    modelList.add(f);
                } else if (deep){
                    listModel(f, modelList, model, deep);
                }
            }
        }
    }
    
    public static File findFile(final File rootDirectory, File file) {
        for (File f : rootDirectory.listFiles()) {
            if (f.getName().equals(file.getName()) && !f.getPath().equals(file.getPath())) {
                return f;
            } else if (f.isDirectory()) {
                File child = findFile(f, file);
                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }
}
