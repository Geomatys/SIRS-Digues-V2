package fr.sirs.util;

import fr.sirs.core.SirsCore;
import java.text.DecimalFormat;
import java.util.logging.Level;
import javafx.util.StringConverter;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FormattedDoubleConverter  extends StringConverter<Double> {

        final DecimalFormat format;

        public FormattedDoubleConverter(final DecimalFormat format) {
            if (format == null) {
                this.format = new DecimalFormat("#.#######");
            } else {
                this.format = format;
            }
        }

        @Override
        public String toString(Double object) {
            if (object == null)
                return "";
            return format.format(object);
        }

        @Override
        public Double fromString(String string) {
            if (string == null || (string = string.trim()).isEmpty()) {
                return null;
            }

            try {
                return format.parse(string).doubleValue();
            } catch (Exception e) {
                final String strCopy = string;
                SirsCore.LOGGER.log(Level.FINE, e, () -> "Cannot convert a string into number : " + strCopy);
                return null;
            }
        }

}
