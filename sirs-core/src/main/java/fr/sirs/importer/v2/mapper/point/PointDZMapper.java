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
package fr.sirs.importer.v2.mapper.point;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.PointDZ;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.ImportContext;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PointDZMapper extends AbstractMapper<PointDZ> {

    private enum Columns {
        DISTANCE,
        PR_SAISI
    }

    private final String fieldName;

    public PointDZMapper(Table table, final String fieldName) {
        super(table);
        this.fieldName = fieldName;
    }

    @Override
    public void map(Row input, PointDZ output) throws IllegalStateException, IOException, AccessDbImporterException {
        Double pr = input.getDouble(fieldName);
        if (pr != null) {
            output.setD(pr);
        }
    }

    @Component
    public static class Spi implements MapperSpi<PointDZ> {

        @Override
        public Optional<Mapper<PointDZ>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (ImportContext.columnExists(inputType, c.name())) {
                    return Optional.of(new PointDZMapper(inputType, c.name()));
                }
            }
            return Optional.empty();
        }

        @Override
        public Class<PointDZ> getOutputClass() {
            return PointDZ.class;
        }
    }

}
