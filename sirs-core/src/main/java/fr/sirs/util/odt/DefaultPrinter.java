package fr.sirs.util.odt;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.component.Previews;
import fr.sirs.util.property.Reference;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collections;
import org.apache.sis.util.ArgumentChecks;

/**
 * Printer used by default to convert object properties into string for ODT templates.
 *
 * @author Alexis Manin (Geomatys)
 */
class DefaultPrinter extends PropertyPrinter {

    private final Previews previews;

    public DefaultPrinter() {
        super(Collections.EMPTY_LIST);
        previews = InjectorCore.getBean(SessionCore.class).getPreviews();
    }

    @Override
    public String print(Object source, PropertyDescriptor property) throws ReflectiveOperationException {
        ArgumentChecks.ensureNonNull("Object holding property to print", source);
        ArgumentChecks.ensureNonNull("Descriptor for the property to print", property);
        return printImpl(source, property);
    }


    @Override
    protected String printImpl(Object source, PropertyDescriptor property) throws ReflectiveOperationException {
        final Method readMethod = property.getReadMethod();
            if (readMethod == null) {
                throw new IllegalArgumentException("Given property descriptor has no accessor defined.");
            } else {
                readMethod.setAccessible(true);
            }

            // Check if we've got a real data or a link.
            final Reference ref = readMethod.getAnnotation(Reference.class);
            final Class<?> refClass;
            if (ref != null) {
                refClass = ref.ref();
            } else {
                refClass = null;
            }

            Object value = readMethod.invoke(source);
            if (value == null) {
              return "N/A";
            } else if (refClass != null && (value instanceof String)) {
                return previews.get((String) value).getLibelle();
            } else if (refClass == null) {
                return value.toString(); // TODO : better string conversion ?
            } else {
                throw new SirsCoreRuntimeException("A reference attribute must be a string ! Found : "+value + " from object "+source);
            }
    }


}
