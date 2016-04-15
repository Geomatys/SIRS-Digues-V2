/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.apache.sis.util.UnconvertibleObjectException;
import org.geotoolkit.feature.util.converter.SimpleConverter;

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
