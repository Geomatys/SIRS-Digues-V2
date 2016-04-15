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
package fr.sirs.importer.v2.mapper.document;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.SIRSReference;
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
public class SIRSReferenceMapper extends AbstractMapper<SIRSReference> {

    private static final String COLUMN_NAME = "REFERENCE_PAPIER";

    public SIRSReferenceMapper(Table table) {
        super(table);
    }

    @Override
    public void map(Row input, SIRSReference output) throws IllegalStateException, IOException, AccessDbImporterException {
        String ref = input.getString(COLUMN_NAME);
        if (ref != null) {
            output.setReferencePapier(ref);
        }
    }

    @Component
    public static class SIRSReferenceMapperSpi implements MapperSpi<SIRSReference> {

        @Override
        public Optional<Mapper<SIRSReference>> configureInput(Table inputType) throws IllegalStateException {
            if (ImportContext.columnExists(inputType, COLUMN_NAME)) {
                return Optional.of(new SIRSReferenceMapper(inputType));
            }
            return Optional.empty();
        }

        @Override
        public Class<SIRSReference> getOutputClass() {
            return SIRSReference.class;
        }

    }
}
