
package fr.sirs;

import fr.sirs.theme.Theme;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.MenuItem;
import org.geotoolkit.map.MapItem;

/**
 * Un plugin est un ensemble de thèmes et de couches de données cartographique.
 * - Les thèmes se retrouvent dans les menus de la bar d'outil principale de l'application.
 * - Les couches cartographiques seront ajoutées dans la vue cartographique.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class Plugin {
    
    /** Message affiché lors du chargement du plugin */
    protected final SimpleStringProperty loadingMessage = new SimpleStringProperty("");
    /** Liste des themes géré par le plugin */
    protected final List<Theme> themes = new ArrayList<>();
        
    /**
     * Récupérer la session SIRS en cours.
     * 
     * @return Session, jamais nulle
     */
    public Session getSession(){
        return Injector.getBean(Session.class);
    }
    
    /**
     * Récupérrer la liste des couches de données à ajouter dans la vue
     * cartographique.
     * 
     * @return Liste de MapItem, jamais nulle
     */
    public List<MapItem> getMapItems(){
        return Collections.EMPTY_LIST;
    }
    
    /**
     * Message affiché lors du chargement du plugin.
     * 
     * @return SimpleStringProperty, jamais nulle
     */
    public final ReadOnlyStringProperty getLoadingMessage(){
        return loadingMessage;
    }
    
    /**
     * Liste des themes géré par le plugin.
     * 
     * @return Liste de Theme, jamais nulle
     */
    public List<Theme> getThemes(){
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
       
}
