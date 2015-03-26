package fr.sirs.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.ObjectConverters;
import org.apache.sis.util.UnconvertibleObjectException;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.report.FeatureCollectionDataSource;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ObjectDataSource<T> implements JRDataSource {
    
    private final Iterator<T> iterator;
    private T currentObject;
    
    public ObjectDataSource(final Iterable<T> iterable){
        ArgumentChecks.ensureNonNull("iterable", iterable);
        iterator = iterable.iterator();
    }

    @Override
    public boolean next() throws JRException {
        if(iterator.hasNext()){
            currentObject = iterator.next();
            return true;
        } else {
            return false;
        }
    }
    
    private String getFieldNameFromSetter(final Method setter){
        return setter.getName().substring(3, 4).toLowerCase()
                            + setter.getName().substring(4);
    }

    @Override
    public Object getFieldValue(JRField jrf) throws JRException {

        //casual field types
        final String name = jrf.getName();
        final Class currentClass = currentObject.getClass();
        try {
            final Method getter = currentClass.getMethod("get"+name.substring(0, 1).toUpperCase()+name.substring(1));
            final Object prop = getter.invoke(currentObject);
            
        if(prop != null){
            //just in case the type is not rigourously the same.
            final Class clazz = jrf.getValueClass();
            try {
                return ObjectConverters.convert(prop, clazz);
            } catch (UnconvertibleObjectException e) {
                Logging.recoverableException(FeatureCollectionDataSource.class, "getFieldValue", e);
                // TODO - do we really want to ignore?
            }
        }
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            Logger.getLogger(ObjectDataSource.class.getName()).log(Level.SEVERE, null, ex);
        } 

        //No field that match this name, looks like the feature type
        //used is not the exact one returned by the JasperReportservice.
        //This is not necessarly an error if for exemple someone ignore
        //some attribut from the template because he doesn't need them.
        return null;
    }
    
}
