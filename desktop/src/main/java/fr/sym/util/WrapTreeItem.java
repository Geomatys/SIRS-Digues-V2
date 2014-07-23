

package fr.sym.util;

import fr.sym.Plugins;
import fr.sym.Session;
import fr.sym.Theme;
import fr.sym.digue.dto.Dam;
import fr.sym.digue.dto.DamSystem;
import fr.sym.digue.dto.Section;
import java.util.Collections;
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
        return false;
    }

    @Override
    public synchronized ObservableList getChildren() {
        if(!loaded){
            loaded = true;
            final Object obj = getValue();
            List candidates = Collections.EMPTY_LIST;
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
