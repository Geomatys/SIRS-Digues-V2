package fr.sirs.util.odt;

import fr.sirs.core.SirsCore;
import java.beans.PropertyDescriptor;
import org.opengis.feature.Feature;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class AmontAvalPrinter extends PropertyPrinter {

    private static final String AVAL_MSG = "en aval de la borne";
    private static final String AMONT_MSG = "en amont de la borne";

    public AmontAvalPrinter() {
        super(SirsCore.BORNE_DEBUT_AVAL, SirsCore.BORNE_FIN_AVAL);
    }

    @Override
    protected String printImpl(Object source, PropertyDescriptor property) throws ReflectiveOperationException {
        if (Boolean.TRUE.equals(property.getReadMethod().invoke(source))) {
            return AMONT_MSG;
        } else {
            return AVAL_MSG;
        }
    }

    @Override
    protected String printImpl(Feature source, String propertyToPrint) {
         if (Boolean.TRUE.equals(source.getPropertyValue(propertyToPrint))) {
             return AMONT_MSG;
         } else return AVAL_MSG;
    }
}