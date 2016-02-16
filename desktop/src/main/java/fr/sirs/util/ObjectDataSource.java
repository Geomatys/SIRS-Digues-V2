package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.util.property.Reference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.ObjectConverters;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public class ObjectDataSource<T> implements JRDataSource {

    protected final Iterator<T> iterator;
    protected T currentObject;
    protected final Previews previewRepository;
    protected final SirsStringConverter stringConverter;

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
    public Object getFieldValue(final JRField jrf) throws JRException {
        final String name = jrf.getName();
        try {
            final Method getter = currentObject.getClass().getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1));
            final Object propertyValue = getter.invoke(currentObject);
            if (propertyValue != null) {
                final Reference ref = getter.getAnnotation(Reference.class);
                return parsePropertyValue(propertyValue, ref == null ? null : ref.ref(), jrf.getValueClass());
            }
        } catch (Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, "Impossible to print a field value.", ex);
        }

        //No field that match this name, looks like the feature type
        //used is not the exact one returned by the JasperReportservice.
        //This is not necessarly an error if for exemple someone ignore
        //some attribut from the template because he doesn't need them.
        return null;
    }

    /**
     * Extract information from input object to put it in an object of queried type.
     * @param propertyValue The object to get data from.
     * @param refClass If input object is a reference to an element, this class give the pointed element type. Can be null.
     * @param outputClass The type of object to return.
     * @return Extracted information, or null if analysis failed.
     */
    private Object parsePropertyValue(Object propertyValue, final Class refClass, final Class outputClass) {
        if (propertyValue instanceof Collection) {
            final PrintableArrayList resultList = new PrintableArrayList(propertyValue instanceof List);
            for (final Object data : (Collection) propertyValue) {
                resultList.add(parsePropertyValue(data, refClass, String.class));
            }

            return resultList;
        }

        if (refClass != null) {
            if (!refClass.isAssignableFrom(propertyValue.getClass()) && (propertyValue instanceof String)) {
                if (ReferenceType.class.isAssignableFrom(refClass)) {
                    propertyValue = Injector.getSession().getRepositoryForClass(refClass).get((String)propertyValue);
                } else {
                    propertyValue = previewRepository.get((String)propertyValue);
                }
            }
        }

        if (outputClass.isAssignableFrom(propertyValue.getClass()))
            return propertyValue;

        if (String.class.isAssignableFrom(outputClass)) {
            return stringConverter.toString(propertyValue);
        } else {
            return ObjectConverters.convert(propertyValue, outputClass);
        }
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
            final Iterator<E> it = iterator();
            if (! it.hasNext())
                return "";

            final StringBuilder sb = new StringBuilder();

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
