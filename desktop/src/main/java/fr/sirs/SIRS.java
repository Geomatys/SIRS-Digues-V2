

package fr.sirs;

import fr.sirs.core.SirsCore;
import java.awt.Color;
import java.io.IOException;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

/**
 * Constants used for project.
 * 
 * @author Johann Sorel
 */
public final class SIRS extends SirsCore {
    
    public static final Image ICON_ADD_WHITE    = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_PLUS,22,Color.WHITE),null);
    public static final Image ICON_ADD_BLACK    = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_PLUS,22,Color.BLACK),null);
    public static final Image ICON_SEARCH       = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_SEARCH,22,Color.WHITE),null);
    public static final Image ICON_TRASH        = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TRASH_O,22,Color.WHITE),null);
    public static final Image ICON_CROSSHAIR_BLACK= SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CROSSHAIRS,22,Color.BLACK),null);
    public static final Image ICON_PREVIOUS = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_ARROW_LEFT,22,Color.WHITE),null);
    public static final Image ICON_NEXT = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_ARROW_RIGHT,22,Color.WHITE),null);
    public static final Image ICON_PLAY = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_PLAY,22,Color.WHITE),null);
    public static final Image ICON_STOP = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_STOP,22,Color.WHITE),null);
    
    public static final Logger LOGGER = Logging.getLogger(SIRS.class);
    public static final String CSS_PATH = "/fr/sirs/theme.css";
        
    private SIRS(){};
    
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
