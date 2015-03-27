package fr.sirs.util;

import fr.sirs.core.component.PreviewLabelRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.PreviewLabel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.StringConverter;
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
    private final PreviewLabelRepository previewLabelRepository;
    private final StringConverter stringConverter;
    
    public ObjectDataSource(final Iterable<T> iterable){
        this(iterable, null);
    }
    
    public ObjectDataSource(final Iterable<T> iterable, final PreviewLabelRepository previewLabelRepository){
        this(iterable, previewLabelRepository, null);
    }
    
    public ObjectDataSource(final Iterable<T> iterable, final PreviewLabelRepository previewLabelRepository, final StringConverter stringConverter){
        ArgumentChecks.ensureNonNull("iterable", iterable);
        iterator = iterable.iterator();
        this.previewLabelRepository = previewLabelRepository;
        this.stringConverter = stringConverter;
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

    @Override
    public Object getFieldValue(JRField jrf) throws JRException {

        final String name = jrf.getName();
        final Class clazz = jrf.getValueClass();
        
        final Class currentClass = currentObject.getClass();
        try {
            final Method getter = currentClass.getMethod("get"+name.substring(0, 1).toUpperCase()+name.substring(1));
            final Object propertyValue = getter.invoke(currentObject);
            
            if(propertyValue != null){
                try {
                    final Object propertyValueToPrint = parsePropertyValue(propertyValue, clazz);
                    return ObjectConverters.convert(propertyValueToPrint, clazz);
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
    
    private Object parsePropertyValue(final Object propertyValue, final Class clazz){
        final Object propertyValueToPrint;
        if(String.class.isAssignableFrom(clazz)){
            if(previewLabelRepository!=null){
                final PreviewLabel previewLabel = previewLabelRepository.get((String) propertyValue);
                if(previewLabel!=null && previewLabel.getDesignation()!=null && !"".equals(previewLabel.getDesignation())){
                    if(stringConverter!=null){
                        propertyValueToPrint = stringConverter.toString(previewLabel);
                    }else{
                        propertyValueToPrint = previewLabel.getDesignation();
                    }
                }
                else{
                    propertyValueToPrint = propertyValue;
                }
            }
            else{
                propertyValueToPrint = propertyValue;
            }
        } else if(Element.class.isAssignableFrom(clazz) && stringConverter!=null){
            propertyValueToPrint = stringConverter.toString(propertyValue);
        } else if(List.class.isAssignableFrom(clazz)) {
            propertyValueToPrint = new ArrayList();
            for(final Object o : (List) propertyValue){
                ((List)propertyValueToPrint).add(parsePropertyValue(o, o.getClass()));
            }
        } else {
            propertyValueToPrint = propertyValue;
        }
        return propertyValueToPrint;
    }
    
}
