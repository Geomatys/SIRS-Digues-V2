package fr.sirs.core.model;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.sis.util.ArgumentChecks;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class LabelMapper {

    /**
     * Cache.
     */
    private static final Map<Class,LabelMapper> MAPPERS = new HashMap<>();

    private final Class modelClass;
    private final ResourceBundle bundle;

    public static synchronized LabelMapper get(Class clazz){
        LabelMapper mapper = MAPPERS.get(clazz);
        if(mapper==null){
            mapper = new LabelMapper(clazz);
            MAPPERS.put(clazz, mapper);
        }
        return mapper;
    }

    private LabelMapper(final Class modelClass) {
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
