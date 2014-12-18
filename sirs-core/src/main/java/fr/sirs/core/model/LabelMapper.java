package fr.sirs.core.model;

import java.util.ResourceBundle;
import org.apache.sis.util.ArgumentChecks;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class LabelMapper {
    
    private Class modelClass;
    private ResourceBundle bundle;
    
    public LabelMapper(final Class modelClass) {
        setModelClass(modelClass);
    }
    
    public final void setModelClass(final Class modelClass) {
        ArgumentChecks.ensureNonNull("Input model class", modelClass);
        this.modelClass = modelClass;
        bundle = ResourceBundle.getBundle(modelClass.getName());
    }
    
    public Class getModelClass() {return this.modelClass;}
    
    public String mapPropertyName(final String property) {
        String result = bundle.getString(property);
        if (result == null) {
            return property;
        } else {
            return result;
        }
    }
    
    public static String mapPropertyName(final Class modelClass, final String property) {
        final LabelMapper labelMapper = new LabelMapper(modelClass);
        return labelMapper.mapPropertyName(property);
    }

    public String mapClassName() {
        final String name = bundle.getString("class");
        return name!=null ? name : modelClass.getSimpleName();
    }
}
