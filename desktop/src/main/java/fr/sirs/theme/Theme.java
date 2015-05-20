package fr.sirs.theme;

import java.util.ArrayList;
import java.util.List;
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
     * Les thèmes sont classés en deux catégories (Type) :
     * - STANDARD : pour les thèmes d'objets rattachés aux tronçons et qui ont une représentation graphique.
     * - OTHER : pour les thèmes sans relations ni représentation graphique.
     */
    public static enum Type{
        STANDARD,
        OTHER
    }
    
    private final String name;
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
        this.name = name;
        this.type = type;
    }
    
    /**
     * 
     * @return String, jamais nulle
     */
    public String getName(){
        return name;
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
     * Creation d'un panneau d'interface utilisateur permettant la consultation
     * et l'édition des objets de se thème.
     * 
     * @return Composant d'interface, jamais nulle.
     */
    public abstract Parent createPane();
    
}
