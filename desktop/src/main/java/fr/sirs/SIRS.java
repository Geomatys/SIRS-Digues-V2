

package fr.sirs;

import fr.sirs.core.Repository;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.ContactOrganisme;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.digue.FXDiguePane;
import fr.sirs.digue.FXTronconDiguePane;
import fr.sirs.other.FXContactOrganismePane;
import fr.sirs.other.FXContactPane;
import fr.sirs.other.FXOrganismePane;
import fr.sirs.theme.ui.AbstractFXElementPane;
import fr.sirs.theme.ui.FXElementPane;
import fr.sirs.theme.ui.FXThemePane;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.property.SirsPreferences;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.util.ComboBoxCompletion;

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
    public static final Image ICON_FILE_BLACK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_FILE,22,Color.BLACK),null);
    public static final Image ICON_TABLE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TABLE,22,Color.WHITE),null);
    public static final Image ICON_UNDO_BLACK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_UNDO, 22, Color.BLACK),null);
    public static final Image ICON_INFO_BLACK_16 = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_INFO, 16, Color.BLACK),null);
    public static final Image ICON_EYE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_EYE, 16, Color.BLACK),null);
    
    
    public static final Logger LOGGER = Logging.getLogger(SIRS.class);
    public static final String CSS_PATH = "/fr/sirs/theme.css";

    private static Stage LAUNCHER;
    public static void setLauncher(Stage currentWindow) {
        LAUNCHER=currentWindow;
    }
    public static Stage getLauncher() {
        return LAUNCHER;
    }

    public static Loader LOADER;
        
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
        ArgumentChecks.ensureNonNull("JavaFX controller object", candidate);
        final Class cdtClass = candidate.getClass();
        final String fxmlpath = "/"+cdtClass.getName().replace('.', '/')+".fxml";
        URL resource = cdtClass.getResource(fxmlpath);
        if (resource == null) {
            throw new RuntimeException("No FXMl document can be found for path : "+fxmlpath);
        }
        final FXMLLoader loader = new FXMLLoader(resource);
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
    
    /**
     * Reconstruit une liste d'éléments depuis la liste en entrée et le {@link Repository} donné.
     * Si la liste en paramètre est nulle ou vide, une liste vide est renvoyée.
     * Si elle contient des éléments, elle est renvoyée telle quel.
     * Si c'est une liste d'ID, on construit une liste des élements correspondants.
     * 
     * @param sourceList La liste depuis laquelle on doit reconstruire la liste des éléments.
     * @param repo Le repository servant à retrouver les éléments depuis leur ID.
     * @return Une liste d'éléments. Peut être vide, mais jamais nulle.
     */
    public static ObservableList<Element> toElementList(final List sourceList, final Repository repo) {
        if (sourceList == null || sourceList.isEmpty()) {
            if (sourceList instanceof ObservableList) {
                return (ObservableList) sourceList;
            } else {
                return FXCollections.observableArrayList();
            }
        } else if (sourceList.get(0) instanceof Element) {
            if (sourceList instanceof ModifiableObservableListBase) {
                return (ObservableList) sourceList;
            } else {
                return FXCollections.observableArrayList(sourceList);
            }
        } else if (repo == null) {
            return FXCollections.observableArrayList();
        } else {
            ObservableList resultList = FXCollections.observableArrayList();
            final Iterator<String> it = sourceList.iterator();
            while (it.hasNext()) {
                resultList.add(repo.get(it.next()));
            }
            return resultList;
        }
    }
    
    /**
     * Retourne la liste d'{@link Objet}s dont les identifiants sont passés dans 
     * le {@link HashSet} d'entrée. La recherche s'effectue uniquement sur le 
     * tronçon donné en paramètre.
     * 
     * /!\ Pour chaque objet trouvé, son identifiant est supprimé du set d'entrée.
     * 
     * @param objetIds Les identifiants des objets à retrouver.
     * @param sourceTroncon Le tronçon où chercher les objets.
     * @return Une liste d'objets. Elle peut être vide, mais jamais nulle.
     */
    public static ObservableList<Objet> getStructures(final HashSet<String> objetIds, final Element sourceTroncon) {
        ObservableList<Objet> result = FXCollections.observableArrayList();
        if (objetIds == null || objetIds.isEmpty()) {
            return result;
        }
        
        if (sourceTroncon instanceof TronconDigue && ((TronconDigue)sourceTroncon).getStructures() !=  null) {
            final Iterator<Objet> it = ((TronconDigue)sourceTroncon).getStructures().iterator();
            Objet o;
            while (objetIds.size() > 0 && it.hasNext()) {
                o = it.next();
                if (objetIds.remove(o.getId())) {
                    result.add(o);
                }
            }
        }
        
        return result;
    }
        
    /**
     * Retourne la liste d'{@link Objet}s dont les identifiants sont passés dans 
     * le {@link HashSet} d'entrée. La recherche s'effectue sur tous les tronçons
     * de la base de donnée chargée. Pour éviter un surcoût mémoire et processeur
     * trop important, la recherche se limite aux objets du type passé en paramètre.
     * 
     * /!\ Pour chaque objet trouvé, son identifiant est supprimé du set d'entrée.
     * 
     * @param objetIds Les identifiants des objets à retrouver.
     * @param objetClass L'implémentation d'Objet sur laquelle se concentrer.
     * @return Une liste d'objets. Elle peut être vide, mais jamais nulle.
     */
    public static ObservableList<Objet> getStructures(final HashSet<String> objetIds, final Class objetClass) {
        ObservableList<Objet> result = FXCollections.observableArrayList();
        if (objetClass == null || !Objet.class.isAssignableFrom(objetClass)
                || objetIds == null || objetIds.isEmpty()) {
            return result;
        }
        
        List<? extends Objet> allFromView = Injector.getSession().getTronconDigueRepository().getAllFromView(objetClass.getSimpleName());
        if (allFromView != null) {
            final Iterator<? extends Objet> it = allFromView.iterator();
            Objet o;
            while (objetIds.size() > 0 && it.hasNext()) {
                o = it.next();
                if (objetIds.remove(o.getId())) {
                    result.add(o);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Tente de trouver un éditeur d'élément compatible avec l'objet passé en paramètre.
     * @param pojo
     * @return Un éditeur pour l'objet d'entrée, ou null si aucun ne peut être 
     * trouvé. L'éditeur aura déjà été initialisé avec l'objet en paramètre.
     */
    public static AbstractFXElementPane generateEditionPane(final Element pojo) {
        final AbstractFXElementPane content;        
        if (pojo instanceof Contact) {
            content = new FXContactPane((Contact) pojo);
            
        } else if (pojo instanceof Organisme) {
            content = new FXOrganismePane((Organisme) pojo);
            
        } else if (pojo instanceof ContactOrganisme) {
            content = new FXContactOrganismePane((ContactOrganisme) pojo);
            
        } else if (pojo instanceof ProfilTravers) {
            content = new FXThemePane((ProfilTravers) pojo);
            ((FXThemePane) content).setShowOnMapButton(false);
            
        } else if (pojo instanceof LeveProfilTravers){
            content = new FXThemePane((LeveProfilTravers) pojo);
            ((FXThemePane) content).setShowOnMapButton(false);
            
        } else if (pojo instanceof TronconDigue) {
            final FXTronconDiguePane ctrl = new FXTronconDiguePane();
            ctrl.setElement((TronconDigue) pojo);
            content = ctrl;
            
        } else if (pojo instanceof Digue) {
            final FXDiguePane ctrl = new FXDiguePane();
            ctrl.setElement((Digue) pojo);
            content = ctrl;
            
        } else {
            content = new FXThemePane((Element) pojo);
        }
        return content;
    }
    
        /**
     * initialize ComboBox items using input list. We also activate completion.
     * @param comboBox The combo box to set value on.
     * @param items The items we want into the ComboBox.
     * @param current the element to select by default.
     */
    public static void initCombo(ComboBox comboBox, final ObservableList items, final Object current) {
        comboBox.setItems(items);
        comboBox.getSelectionModel().select(current);
        
        new ComboBoxCompletion(comboBox);
        comboBox.setConverter(new SirsStringConverter());
        comboBox.setEditable(true);
    }
}
