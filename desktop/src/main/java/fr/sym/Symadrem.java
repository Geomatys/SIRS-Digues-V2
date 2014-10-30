

package fr.sym;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.apache.sis.util.logging.Logging;

/**
 * Constants used for project.
 * 
 * @author Johann Sorel
 */
public final class Symadrem {
    
    public static final String NAME = "symadrem";
    public static final Logger LOGGER = Logging.getLogger(Symadrem.class);
    public static final String CSS_PATH = "/fr/sym/theme.css";
        
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
        
    public static void loadFXML(Parent candidate){
        final Class cdtClass = candidate.getClass();
        final String fxmlpath = "/"+cdtClass.getName().replace('.', '/')+".fxml";
        final FXMLLoader loader = new FXMLLoader(cdtClass.getResource(fxmlpath));
        loader.setController(candidate);
        loader.setRoot(candidate);
        //in special environement like osgi or other, we must use the proper class loaders
        //not necessarly the one who loaded the FXMLLoader class
        loader.setClassLoader(cdtClass.getClassLoader());
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        candidate.getStylesheets().add(CSS_PATH);
    }
    
}
