

package fr.sirs;

import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Element;
import fr.sirs.util.property.SirsPreferences;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
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
    public static final Image ICON_CARET_LEFT = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CARET_LEFT,22,Color.WHITE),null);
    public static final Image ICON_CARET_RIGHT = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CARET_RIGHT,22,Color.WHITE),null);
    public static final Image ICON_FILE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_FILE,22,Color.WHITE),null);
    public static final Image ICON_TABLE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TABLE,22,Color.WHITE),null);
    public static final Image ICON_UNDO_BLACK= SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_UNDO, 22, Color.BLACK),null);
    
    
    public static final Logger LOGGER = Logging.getLogger(SIRS.class);
    public static final String CSS_PATH = "/fr/sirs/theme.css";
        
    private SIRS(){};
    
    public static void loadFXML(Parent candidate) {
        loadFXML(candidate, null);
    }
    
    /**
     * Load FXML document matching input controller. If a model class is given, 
     * we'll try to load a bundle for text internationalization.
     * @param candidate The controller object to get FXMl for.
     * @param modelClass A class which will be used for bundle loading.
     */
    public static void loadFXML(Parent candidate, final Class modelClass) {
        final Class cdtClass = candidate.getClass();
        final String fxmlpath = "/"+cdtClass.getName().replace('.', '/')+".fxml";
        final FXMLLoader loader = new FXMLLoader(cdtClass.getResource(fxmlpath));
        loader.setController(candidate);
        loader.setRoot(candidate);
        //in special environement like osgi or other, we must use the proper class loaders
        //not necessarly the one who loaded the FXMLLoader class
        loader.setClassLoader(cdtClass.getClassLoader());
        
        // If possible, initialize traduction bundle.
        if (modelClass != null) {
            loader.setResources(ResourceBundle.getBundle(modelClass.getName()));
        }
        
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        
        candidate.getStylesheets().add(CSS_PATH);
    }
    
    /**
     * 
     * @param relativeReference Un chemin relatif dénotant une référence dans un {@link Element}
     * @return Un chemin absolu vers la réference passée en paramètre.
     * @throws IllegalStateException Si la propriété {@link SirsPreferences.PROPERTIES#DOCUMENT_ROOT} est inexistante ou ne dénote pas un chemin valide.
     * Dans ce cas, il est FORTEMENT conseillé d'attraper l'exception, et de proposer à l'utilisateur de vérifier la valeur de cette propriété dans les 
     * préférences de l'application.
     * @throws InvalidPathException Si il est impossible de construire un chemin valide avec le paramètre d'entrée.
     * 
     * Note : les deux exceptions ci-dessus ne sont pas lancées dans le cas où le 
     * chemin créé dénote un fichier inexistant. Elles sont invoquées uniquement 
     * si les chemins sont incorrects syntaxiquement.
     */
    public static Path getDocumentAbsolutePath(final String relativeReference) throws IllegalStateException, InvalidPathException {
        String rootStr = SirsPreferences.INSTANCE.getProperty(SirsPreferences.PROPERTIES.DOCUMENT_ROOT);
        final Path docRoot;
        try {
            docRoot = Paths.get(rootStr);
        } catch (InvalidPathException e) {
            throw new IllegalStateException("La preference " + SirsPreferences.PROPERTIES.DOCUMENT_ROOT.name()
                    + "ne dénote pas un chemin valide. Vous pouvez vérifier sa valeur "
                    + "depuis les préférences de l'application (Fichier > Preferences).", e);
        }

        /* HACK : change all separators, because when we use 2 different system 
         * separator in the same time, it produces invalid paths. We also check
         * if path starts with file separato, because unix consider it as system
         * root, and will not resolve image path as relative if we keep it.
         */
        return docRoot.resolve(relativeReference.replaceFirst("^(/+|\\\\+)", "").replaceAll("/+|\\\\+", File.separator));
    }
}
