
package fr.sym.theme;

import fr.sym.theme.Theme.Type;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.util.function.Function;
import javafx.collections.ObservableList;
import javafx.scene.Parent;

/**
 *
 * @author Johann Sorel
 */
public class AbstractTronconTheme extends Theme {
    
    public static class ThemeGroup{
        private final String name;
        private final Class dataClass;
        private final Function<TronconDigue,ObservableList> extractor;

        public ThemeGroup(String name, Class dataClass, Function<TronconDigue,ObservableList> extractor) {
            this.name = name;
            this.dataClass = dataClass;
            this.extractor = extractor;
        }

        public String getName() {
            return name;
        }

        public Class getDataClass() {
            return dataClass;
        }
        
        public Function<TronconDigue, ObservableList> getExtractor() {
            return extractor;
        }
        
    }
    
    private final ThemeGroup[] groups;
    
    public AbstractTronconTheme(String name, ThemeGroup ... groups) {
        super(name,Type.STANDARD);
        this.groups = groups;
        
        if(groups.length>1){
            for(ThemeGroup group : groups){
                final Theme subtheme = new AbstractTronconTheme(group.getName(), group);
                getSubThemes().add(subtheme);
            }
        }
        
    }
    
    public Parent createPane(){
        return new TronconThemePane(groups);
    }
    
}
