
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
    
    public static interface Deletor{
        void delete(TronconDigue t, Object c);
    }
    
    public static class ThemeGroup{
        private final String name;
        private final String tableTitle;
        private final Class dataClass;
        private final Function<TronconDigue,ObservableList> extractor;
        private final Deletor deletor;

        public ThemeGroup(String name, Class dataClass, Function<TronconDigue,ObservableList> extractor, Deletor deletor) {
            this(name,null,dataClass,extractor,deletor);
        }
        
        public ThemeGroup(String name, String tableTitle, Class dataClass, Function<TronconDigue,ObservableList> extractor, Deletor deletor) {
            this.name = name;
            this.tableTitle = tableTitle;
            this.dataClass = dataClass;
            this.extractor = extractor;
            this.deletor = deletor;
        }

        public String getName() {
            return name;
        }

        public String getTableTitle() {
            return tableTitle;
        }
        
        public Class getDataClass() {
            return dataClass;
        }
        
        public Function<TronconDigue, ObservableList> getExtractor() {
            return extractor;
        }

        public Deletor getDeletor() {
            return deletor;
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
    
    @Override
    public Parent createPane(){
        return new TronconThemePane(groups);
    }
    
}
