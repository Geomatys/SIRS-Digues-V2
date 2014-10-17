
package fr.sym.theme;

import fr.sym.theme.Theme.Type;
import fr.symadrem.sirs.core.Repository;
import javafx.scene.Parent;

/**
 *
 * @author Johann Sorel
 */
public class AbstractTronconTheme extends Theme {
    
    private final Repository[] repositories;
    
    public AbstractTronconTheme(String name, Repository ... repositories) {
        super(name,Type.STANDARD);
        this.repositories = repositories;
    }
    
    public Parent createPane(){
        return new TronconThemePane();
    }
    
}
