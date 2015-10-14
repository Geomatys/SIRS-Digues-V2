package fr.sirs.util;

import fr.sirs.SIRS;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Preview;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.ObjectConverters;
import org.apache.sis.util.UnconvertibleObjectException;
import org.apache.sis.util.logging.Logging;
import org.ektorp.DocumentNotFoundException;
import org.geotoolkit.report.CollectionDataSource;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ObjectDataSource<T> implements JRDataSource {
    
    private final Iterator<T> iterator;
    private T currentObject;
    private final Previews previewRepository;
    private final SirsStringConverter stringConverter;
    
    public ObjectDataSource(final Iterable<T> iterable){
        this(iterable, null);
    }
    
    public ObjectDataSource(final Iterable<T> iterable, final Previews previewLabelRepository){
        this(iterable, previewLabelRepository, null);
    }
    
    public ObjectDataSource(final Iterable<T> iterable, final Previews previewLabelRepository, final SirsStringConverter stringConverter){
        ArgumentChecks.ensureNonNull("iterable", iterable);
        iterator = iterable.iterator();
        this.previewRepository = previewLabelRepository;
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
                    Logging.recoverableException(CollectionDataSource.class, "getFieldValue", e);
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
            if(previewRepository!=null){
                Preview previewLabel = null;
                try{
                    previewLabel = previewRepository.get((String) propertyValue);
                } catch(DocumentNotFoundException e){
                    SIRS.LOGGER.log(Level.FINEST, e.getMessage());
                }
                if(previewLabel!=null){
                    if(stringConverter!=null){
                        propertyValueToPrint = stringConverter.toString(previewLabel, false);
                    } else if(previewLabel.getDesignation()!=null && !"".equals(previewLabel.getDesignation())){
                        propertyValueToPrint = previewLabel.getDesignation();
                    } else propertyValueToPrint = propertyValue;
                }
                else{
                    propertyValueToPrint = propertyValue;
                }
            }
            else{
                propertyValueToPrint = propertyValue;
            }
        } else if(Element.class.isAssignableFrom(clazz) && stringConverter!=null){
            propertyValueToPrint = stringConverter.toString(propertyValue, false);
        } else if(List.class.isAssignableFrom(clazz)) {
            propertyValueToPrint = new PrintableArrayList(true);
            for(final Object o : (List) propertyValue){
                ((List)propertyValueToPrint).add(parsePropertyValue(o, o.getClass()));
            }
        } else {
            propertyValueToPrint = propertyValue;
        }
        return propertyValueToPrint;
    }
    
    /**
     * Extention of ArrayList for redefining toString() in order to improve printing.
     * 
     * @param <E> 
     */
    private class PrintableArrayList<E> extends ArrayList<E>{

        private final boolean ordered;
        
        /**
         * 
         * @param ordered Specifies if the list has to be ordered.
         */
        public PrintableArrayList(final boolean ordered) {
            super();
            this.ordered = ordered;
        }
        
        /**
         * Creates an unordered PrintableArrayList.
         */
        public PrintableArrayList(){
            this(false);
        }
        
        @Override
        public String toString(){
            Iterator<E> it = iterator();
            if (! it.hasNext())
                return "";

            StringBuilder sb = new StringBuilder();
            
            int order = 0;
            for (;;) {
                E e = it.next();
                if(ordered){
                    sb.append(order++);
                }
                sb.append('-').append(' ');
                sb.append(e == this ? "(this Collection)" : e);
                if (! it.hasNext()) return sb.toString();
                else sb.append('\n');
            }
        }
    }
    
}
