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

import fr.sirs.core.SirsCore;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import org.apache.sis.math.FunctionProperty;
import org.apache.sis.util.ObjectConverter;
import org.apache.sis.util.UnconvertibleObjectException;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class LocalDateToDateConverter implements ObjectConverter<LocalDate, Date> {

    @Override
    public Set<FunctionProperty> properties() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Class<LocalDate> getSourceClass() {
        return LocalDate.class;
    }

    @Override
    public Class<Date> getTargetClass() {
        return Date.class;
    }

    @Override
    public Date apply(LocalDate s) throws UnconvertibleObjectException {
        return Date.from(s.atStartOfDay(SirsCore.PARIS_ZONE_ID).toInstant());
    }

    @Override
    public ObjectConverter<Date, LocalDate> inverse() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
