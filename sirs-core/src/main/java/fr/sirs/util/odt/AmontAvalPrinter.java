package fr.sirs.util.odt;

import fr.sirs.core.SirsCore;
import java.beans.PropertyDescriptor;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class AmontAvalPrinter extends PropertyPrinter {

    public AmontAvalPrinter() {
        super(SirsCore.BORNE_DEBUT_AVAL, SirsCore.BORNE_FIN_AVAL);
    }

    @Override
    protected String printImpl(Object source, PropertyDescriptor property) throws ReflectiveOperationException {
        if (Boolean.TRUE.equals(property.getReadMethod().invoke(source))) {
            return "en amont de la borne";
        } else {
            return "en aval de la borne";
        }
    }
}