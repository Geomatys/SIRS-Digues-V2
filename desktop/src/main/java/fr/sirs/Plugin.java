
package fr.sirs;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.sql.SQLHelper;
import fr.sirs.map.FXMapPane;
import fr.sirs.theme.Theme;
import fr.sirs.util.FXFreeTab;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import org.geotoolkit.map.MapItem;

/**
 * Un plugin est un ensemble de thèmes et de couches de données cartographique.
 * - Les thèmes se retrouvent dans les menus de la barre d'outil principale de l'application.
 * - Les couches cartographiques seront ajoutées dans la vue cartographique.
 * 
 * @author Johann Sorel (Geomatys)
 */
public abstract class Plugin {
    
    public static String PLUGIN_FLAG = "pluginSirs";
    
    protected String name;
    /** Message affiché lors du chargement du plugin */
    protected final SimpleStringProperty loadingMessage = new SimpleStringProperty("");
    /** Liste des themes géré par le plugin */
    protected final List<Theme> themes = new ArrayList<>();
        
    /**
     * Récupérer la session SIRS en cours.
     * 
     * @return Session, jamais nulle
     */
    public Session getSession() {
        return Injector.getBean(Session.class);
    }
    
    /**
     * Récupérer la liste des couches de données à ajouter dans la vue
     * cartographique.
     * 
     * @return Liste de MapItem, jamais nulle
     */
    public List<MapItem> getMapItems() {
        return Collections.EMPTY_LIST;
    }
    
    /**
     * Message affiché lors du chargement du plugin.
     * 
     * @return SimpleStringProperty, jamais nulle
     */
    public final ReadOnlyStringProperty getLoadingMessage() {
        return loadingMessage;
    }
    
    /**
     * Liste des themes géré par le plugin.
     * 
     * @return Liste de Theme, jamais nulle
     */
    public List<Theme> getThemes() {
        return themes;
    }
    
    /**
     * Récupère les actions disponibles pour un object selectionné sur la carte.
     * 
     * @param candidate objet selectionné
     * @return Liste d'action possible, jamais nulle
     */
    public List<MenuItem> getMapActions(Object candidate) {
        return Collections.EMPTY_LIST;
    }

    /**
     * Barre d'outils complémentaire pour la carte.
     *
     * @param mapPane Panneau de la carte.
     * @return list, peut etre null
     */
    public List<ToolBar> getMapToolBars(final FXMapPane mapPane){
        return null;
    }

    /**
     * Récupère le titre du plugin.
     *
     * @return Le titre du plugin, jamais nulle ou vide.
     */
    public abstract CharSequence getTitle();

    /**
     * Renvoit l'image du plugin, si une image a été fournie. Peut être {@code null}.
     * @return 
     */
    public abstract Image getImage();

    /**
     * Chargement du plugin.
     * Cette méthode est appelée au démarrage de l'application.
     * Il est recommandé de remplir et de mettre à jour la valeur de 'loadingMessage'
     * au cours du chargement.
     * 
     * @throws java.lang.Exception : en cas d'erreur de chargement du plugin
     */
    public abstract void load() throws Exception;
    
    /**
     * Opérations à effectuer après importation. Il s'agit par exemple de la
     * génération des vues.
     * 
     * Par défaut, on ne fait rien.
     * 
     * @throws Exception 
     */
    public void afterImport() throws Exception {}
    
    /**
     * SQLHelper chargé de l'export des données dans la base RDBMS.
     * 
     * @return 
     */
    public abstract SQLHelper getSQLHelper();

    /**
     * This method declares the plugin is able to display the type of TronconDigue
     * given as a parameter, using openTronconPane() method.
     *
     * @param tronconType
     * @return
     */
    public boolean handleTronconType(final Class<? extends Element> tronconType){
        return false;
    }
    
    /**
     * This method opens a pane for the TronconDigue given as a parameter. It 
     * garantees to open the right pane if and only if the method 
     * handleTronconType() had returned "true" for the runtime type of the 
     * troncon.
     * 
     * Note if handleTronconType had returned "false", this method result is
     * undefined. The default implementation returns null, but other 
     * redefinitions could return a pane without error but which doesn't exactly
     * match the runtime type of the given TronconDigue.
     * 
     * For instance, it may be possible to open a Berge (which inherits 
     * TronconDigue) using openTronconPane() of the CorePlugin, which is 
     * designed for TronconDigue. To avoid this "inconsistent" case, 
     * handleTronconType() of the CorePlugin must return false for Berge class.
     * 
     * @param element
     * @return 
     */
    public FXFreeTab openTronconPane(final TronconDigue element){
        return null;
    }
    
    /**
     * Cherche une configuration valide pour le plugin courant. Par défaut, la
     * méthode cherche un JSON descriptif dans le dossier des plugins. Si aucun
     * fichier ne peut être utilisé, on essaie de construire un descriptif grâce
     * aux informations de la classe Java.
     * @return 
     */
    public PluginInfo getConfiguration() {
        final String pluginName = name == null? this.getClass().getSimpleName() : name;
        final Path pluginPath = SirsCore.PLUGINS_PATH.resolve(pluginName);
        final Pattern jsonPattern = Pattern.compile("(?i)" + pluginName + ".*(\\.json)$");
        try {
            Optional<Path> pluginDescriptor = Files.walk(pluginPath, 1)
                    .filter((Path p) -> jsonPattern.matcher(p.getFileName().toString()).matches())
                    .findAny();
            if (pluginDescriptor.isPresent()) {
                return new ObjectMapper().readValue(pluginDescriptor.get().toFile(), PluginInfo.class);
            }
        } catch (IOException e) {
            SirsCore.LOGGER.log(Level.FINE, "Plugin "+name +" has not any json descriptor.", e);
        }

        final PluginInfo info = new PluginInfo();
        info.setName(pluginName);

        final Matcher versionMatcher;
        final String jarVersion = this.getClass().getPackage().getImplementationVersion();
        if (jarVersion == null || jarVersion.isEmpty()) {
            final String jarLocation = this.getClass().getProtectionDomain().getCodeSource().getLocation().toExternalForm();
            versionMatcher = Pattern.compile("(\\d+)\\.(\\d+)\\.jar$").matcher(jarLocation);
        } else {
            versionMatcher = Pattern.compile("(\\d+)\\.(\\d+)").matcher(jarVersion);
        }
        if (versionMatcher.find()) {
            info.setVersionMajor(Integer.parseInt(versionMatcher.group(1)));
            info.setVersionMinor(Integer.parseInt(versionMatcher.group(2)));
        }
        return info;
    }
       
}
