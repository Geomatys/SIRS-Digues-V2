
package fr.sym.theme;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Parent;

/**
 * Un thème permet de gerer un ensemble d'objet du meme type.
 * 
 * 
 * @author Johann Sorel
 */
public abstract class Theme {
    
    public static enum Type{
        STANDARD,
        OTHER
    }
    
    private final String name;
    private final Type type;
    private final List<Theme> subThemes = new ArrayList<>();

    public Theme(String name, Type type) {
        this.name = name;
        this.type = type;
    }
    
    public String getName(){
        return name;
    }

    public Type getType() {
        return type;
    }

    public List<Theme> getSubThemes() {
        return subThemes;
    }
        
    public abstract Parent createPane();
    
}
