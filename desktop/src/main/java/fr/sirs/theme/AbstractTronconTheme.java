
package fr.sirs.theme;

import fr.sirs.Injector;
import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;
import fr.sirs.core.component.AbstractPositionableRepository;
import fr.sirs.core.model.Positionable;
import fr.sirs.theme.ui.FXTronconThemePane;
import fr.sirs.theme.Theme.Type;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;

/**
 *
 * @author Johann Sorel
 */
public class AbstractTronconTheme extends Theme {
    
    
    public static class ThemeManager<T> {
        private final String name;
        private final String tableTitle;
        private final Class<T> dataClass;
        private final Function<String, ObservableList<T>> extractor;
        private final Consumer<T> deletor;

        public ThemeManager(String name, Class<T> dataClass, Function<String, ObservableList<T>> extractor, Consumer<T> deletor) {
            this(name,null,dataClass,extractor,deletor);
        }
        
        public ThemeManager(String name, String tableTitle, Class<T> dataClass, Function<String, ObservableList<T>> extractor, Consumer<T> deletor) {
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
        
        public Class<T> getDataClass() {
            return dataClass;
        }
        
        public Function<String, ObservableList<T>> getExtractor() {
            return extractor;
        }

        public Consumer getDeletor() {
            return deletor;
        }
        
    }
    
    private ThemeManager[] managers;
    
    
    public AbstractTronconTheme(String name, Class... classes) {
        super(name, Type.STANDARD);
        final ThemeManager[] mngrs = new ThemeManager[classes.length];
        for(int i = 0; i<classes.length; i++){
            mngrs[i] = generateThemeManager(classes[i]);
        }
        this.managers = mngrs;
        initThemeManager(mngrs);
    }
    
    protected AbstractTronconTheme(String name, ThemeManager... managers) {
        super(name, Type.STANDARD);
        this.managers = managers;
        initThemeManager(managers);
    }
    
    private void initThemeManager(final ThemeManager... managers){
        if(managers.length>1){
            for(ThemeManager manager : managers){
                final Theme subtheme = new AbstractTronconTheme(manager.getName(), manager);
                getSubThemes().add(subtheme);
            }
        }
    }
    
    protected void setManagers(final ThemeManager[] managers) {
        this.managers = managers;
    }
    
    @Override
    public Parent createPane(){
        return new FXTronconThemePane(managers);
    }
    
    protected static <T extends Positionable> ThemeManager<T> generateThemeManager(final Class<T> themeClass){
        final ResourceBundle bundle = ResourceBundle.getBundle(themeClass.getCanonicalName());
        final Function<String, ObservableList<T>> extractor = (String linearId) -> {
            final List<T> result = ((AbstractPositionableRepository<T>) Injector.getSession().getRepositoryForClass(themeClass)).getByLinearId(linearId);
            final ObservableList<T> observableResult = FXCollections.observableArrayList();
            for(final T t : result) observableResult.add(t);
            return observableResult;
        };
        final Consumer<T> deletor = (T themeElement) -> Injector.getSession().getRepositoryForClass(themeClass).remove(themeElement);
        
        return new ThemeManager<>(bundle.getString(BUNDLE_KEY_CLASS), 
                "Thème "+bundle.getString(BUNDLE_KEY_CLASS), 
                themeClass, extractor, deletor);
    }
}
