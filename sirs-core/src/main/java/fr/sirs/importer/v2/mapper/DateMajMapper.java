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
package fr.sirs.importer.v2.mapper;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.AvecDateMaj;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.ImportContext;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DateMajMapper extends AbstractMapper<AvecDateMaj> {

    private static final String DEFAULT_FIELD = "DATE_DERNIERE_MAJ";

    private final String fieldName;

    private DateMajMapper(final Table table, final String fieldName) {
        super(table);
        this.fieldName = fieldName;
    }

    @Override
    public void map(Row input, AvecDateMaj output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Date dateMaj = input.getDate(fieldName);
        if (dateMaj != null) {
            output.setDateMaj(context.convertData(dateMaj, LocalDate.class));
        }
    }

    @Component
    public static class Spi implements MapperSpi<AvecDateMaj> {

        @Override
        public Optional<Mapper<AvecDateMaj>> configureInput(Table inputType) {
            String fieldName = null;
            if (ImportContext.columnExists(inputType, DEFAULT_FIELD)) {
                fieldName = DEFAULT_FIELD;
            } else {
                for (final Column c : inputType.getColumns()) {
                    if (c.getName().toUpperCase().startsWith(DEFAULT_FIELD)) {
                        fieldName = c.getName();
                        break;
                    }
                }
            }

            if (fieldName != null) {
                return Optional.of(new DateMajMapper(inputType, fieldName));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Class<AvecDateMaj> getOutputClass() {
            return AvecDateMaj.class;
        }
    }
}
