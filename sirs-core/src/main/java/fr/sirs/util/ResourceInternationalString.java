
package fr.sirs.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Extends ResourceInternationalString to provide a specific class loader.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class ResourceInternationalString extends org.apache.sis.util.iso.ResourceInternationalString {

    private final ClassLoader cl;

    public ResourceInternationalString(String resources, String key, ClassLoader cl) {
        super(resources, key);
        this.cl = cl;
    }

    @Override
    protected ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(resources, locale, cl);
    }
    
}
