
package fr.sirs;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sirs.core.SirsCore;
import fr.sirs.theme.Theme;
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
import org.geotoolkit.map.MapItem;

/**
 * Un plugin est un ensemble de thèmes et de couches de données cartographique.
 * - Les thèmes se retrouvent dans les menus de la barre d'outil principale de l'application.
 * - Les couches cartographiques seront ajoutées dans la vue cartographique.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class Plugin {
    
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
     * Chargement du plugin.
     * Cette méthode est appelée au démarrage de l'application.
     * Il est recommandé de remplir et de mettre à jour la valeur de 'loadingMessage'
     * au cours du chargement.
     * 
     * @throws java.lang.Exception : en cas d'erreur de chargement du plugin
     */
    public void load() throws Exception {
        
    }
    
    /**
     * Cherche une description valide pour le plugin courant. Par défaut, la 
     * méthode cherche un JSON descriptif dans le dossier des plugins. Si aucun
     * fichier ne peut être utilisé, on essaie de construire un descriptif grâce
     * aux informations de la classe Java.
     * @return 
     */
    public PluginInfo getDescription() {
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
