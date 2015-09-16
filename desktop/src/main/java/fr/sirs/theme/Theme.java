package fr.sirs.theme;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import org.apache.sis.util.ArgumentChecks;

/**
 * Un thème permet de gerer un ensemble d'objet du meme type.
 * Un thème peut être sous divisé en sous-thème.
 * 
 * @author Johann Sorel
 */
public abstract class Theme {
    
    /**
     * Les thèmes sont classés en trois catégories (Type) :
     * - LOCALIZED : pour les thèmes d'objets rattachés aux tronçons et qui ont une représentation graphique.
     * - UNLOCALIZED : pour les thèmes sans relations ni représentation graphique.
     * - PLUGINS : regroupe les plugins additionnels chargés par l'utilisateur pour son application.
     */
    public static enum Type{
        LOCALIZED,
        UNLOCALIZED,
        PLUGINS
    }
    
    private final StringProperty name = new SimpleStringProperty("");
    private final Type type;
    private final List<Theme> subThemes = new ArrayList<>();

    /**
     * Constructeur.
     * 
     * @param name nom du thème
     * @param type type de thème
     */
    public Theme(String name, Type type) {
        ArgumentChecks.ensureNonNull("name", name);
        ArgumentChecks.ensureNonNull("type", type);
        this.name.set(name);
        this.type = type;
    }
    
    /**
     * 
     * @return String, jamais nulle
     */
    public String getName(){
        return name.get();
    }

    /**
     * Récupérer le type du thème.
     * 
     * @return Type, jamais nulle.
     */
    public Type getType() {
        return type;
    }

    /**
     * Récupérer la liste des sous-thèmes.
     * 
     * @return Liste des sous-thème. jamais nulle
     */
    public List<Theme> getSubThemes() {
        return subThemes;
    }
        
    /**
     * Création d'un panneau d'interface utilisateur permettant la consultation
     * et l'édition des objets de ce thème.
     * 
     * @return Composant d'interface, jamais nulle.
     */
    public abstract Parent createPane();

    /**
     * Indicates if the theme pane must be cached or reloaded.
     * 
     * @return true if the pane must be cached.
     */
    public boolean isCached(){return true;}
    
    
    /**
     * Un listener sur la selection du theme. Ne fais rien par default, doit etre surchargé par les sous classes.
     * @return 
     */
    public ChangeListener<Boolean> getSelectedPropertyListener() {
        return (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            // ne fais rien par defaut.
        };
    }
}
