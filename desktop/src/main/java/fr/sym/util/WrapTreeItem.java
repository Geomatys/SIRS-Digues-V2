

package fr.sym.util;

import fr.sym.Plugins;
import fr.sym.Session;
import fr.sym.Theme;
import fr.sym.digue.dto.Dam;
import fr.sym.digue.dto.DamSystem;
import fr.sym.digue.dto.Section;
import fr.symadrem.sirs.core.model.Digue;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class WrapTreeItem extends TreeItem {

    private boolean loaded = false;
    
    public WrapTreeItem(Object candidate) {
        super(candidate);
    }

    @Override
    public boolean isLeaf() {
        return getValue() instanceof Theme;
    }

    @Override
    public synchronized ObservableList getChildren() {
        if(!loaded){
            loaded = true;
            final Object obj = getValue();
            List candidates = new ArrayList();
            
            if(obj instanceof Digue){
                candidates = Session.getInstance().getChildren((Digue)obj);
            }else
            
            
            if(obj instanceof DamSystem){
                candidates = Session.getInstance().getChildren((DamSystem)obj);
            }else if(obj instanceof Dam){
                candidates = Session.getInstance().getChildren((Dam)obj);
            }else if(obj instanceof Section){
                final Theme[] themes = Plugins.getThemes();
                for(Theme theme : themes){
                    candidates.add(theme);
                }
            }
            
            for(Object candidate : candidates){
                super.getChildren().add(new WrapTreeItem(candidate));
            }
        }
        
        return super.getChildren();
    }
    
}
