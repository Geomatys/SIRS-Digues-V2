package fr.sirs.core.model;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.collection.Cache;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class LabelMapper {

    /**
     * Cache.
     */
    private static final Cache<Class,LabelMapper> MAPPERS = new Cache<>(12, 0, false);

    private final Class modelClass;
    private final ResourceBundle bundle;

    /**
     * Return a mapper which give translation for the attribute names of the given class.
     * @param clazz Class to get translations for.
     * @return A mapper, or null if we have no bundle for input class.
     */
    public static synchronized LabelMapper get(Class clazz){
        try {
            return MAPPERS.getOrCreate(clazz, () -> new LabelMapper(clazz));
        } catch (Exception ex) {
            Logger.getLogger(LabelMapper.class.getName()).log(Level.WARNING, "No label mapper found for class "+clazz.getCanonicalName(), ex);
        }
        return null;
    }

    private LabelMapper(final Class modelClass) throws MissingResourceException {
        ArgumentChecks.ensureNonNull("Input model class", modelClass);
        this.modelClass = modelClass;
        bundle = ResourceBundle.getBundle(modelClass.getName(), Locale.getDefault(), Thread.currentThread().getContextClassLoader());
    }

    public Class getModelClass() {return this.modelClass;}

    public String mapPropertyName(final String property) {
        try {
            return bundle.getString(property);
        } catch (NullPointerException | MissingResourceException e) {
            return property;
        }
    }

    public static String mapPropertyName(final Class modelClass, final String property) {
        return get(modelClass).mapPropertyName(property);
    }

    public String mapClassName() {
        String name = null;
        try{
            name = bundle.getString("class");
        }catch(MissingResourceException ex){
            //not important
        }
        return name!=null ? name : modelClass.getSimpleName();
    }
}
