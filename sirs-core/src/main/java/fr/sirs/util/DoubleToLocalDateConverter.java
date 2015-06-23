package fr.sirs.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.apache.sis.util.UnconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DoubleToLocalDateConverter extends SimpleConverter<Double, LocalDate>{

    @Override
    public Class<Double> getSourceClass() {
        return Double.class;
    }

    @Override
    public Class<LocalDate> getTargetClass() {
        return LocalDate.class;
    }

    @Override
    public LocalDate apply(Double s) throws UnconvertibleObjectException {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(s.longValue()), ZoneId.systemDefault()).toLocalDate();
    }
    
}
