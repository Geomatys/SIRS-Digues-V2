package fr.sirs.theme;

import fr.sirs.Injector;
import fr.sirs.core.component.AbstractPositionableRepository;
import fr.sirs.core.model.Positionable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;

import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;

/**
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractTheme extends Theme {

    /**
     * Handle theme
     * @param <T>
     */
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

    protected ThemeManager[] managers;

    public AbstractTheme(String name, Class... classes) {
        super(name, Type.LOCALIZED);
        final ThemeManager[] mngrs = new ThemeManager[classes.length];
        for(int i = 0; i<classes.length; i++){
            mngrs[i] = generateThemeManager(classes[i]);
        }
        this.managers = mngrs;
        initThemeManager(mngrs);
    }

    protected AbstractTheme(String name, ThemeManager... managers) {
        super(name, Type.LOCALIZED);
        this.managers = managers;
        initThemeManager(managers);
    }

    protected void setManagers(final ThemeManager[] managers) {
        this.managers = managers;
    }


    protected static <T extends Positionable> ThemeManager<T> generateThemeManager(final Class<T> themeClass){
        final ResourceBundle bundle = ResourceBundle.getBundle(themeClass.getCanonicalName(), Locale.getDefault(),
                Thread.currentThread().getContextClassLoader());
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

    @Override
    public abstract Parent createPane();

    protected abstract void initThemeManager(final ThemeManager... managers);
}
