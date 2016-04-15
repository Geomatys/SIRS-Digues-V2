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
import fr.sirs.core.model.SIRSFileReference;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.ImportContext;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import fr.sirs.importer.v2.mapper.objet.PhotoColumns;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class SIRSFileReferenceMapper extends AbstractMapper<SIRSFileReference> {

    private static final String DEFAULT_COLUMN_NAME = "REFERENCE_NUMERIQUE";

    private final String columnName;

    private SIRSFileReferenceMapper(Table table, final String columnName) {
        super(table);
        this.columnName = columnName;
    }

    @Override
    public void map(Row input, SIRSFileReference output) throws IllegalStateException, IOException, AccessDbImporterException {
        String ref = input.getString(columnName);
        if (ref != null) {
            output.setChemin(ref);
        }
    }

    @Component
    public static class SIRSReferenceMapperSpi implements MapperSpi<SIRSFileReference> {

        @Override
        public Optional<Mapper<SIRSFileReference>> configureInput(Table inputType) throws IllegalStateException {
            if (ImportContext.columnExists(inputType, DEFAULT_COLUMN_NAME)) {
                return Optional.of(new SIRSFileReferenceMapper(inputType, DEFAULT_COLUMN_NAME));
            } else if (ImportContext.columnExists(inputType, PhotoColumns.NOM_FICHIER_PHOTO.name())) {
                return Optional.of(new SIRSFileReferenceMapper(inputType, PhotoColumns.NOM_FICHIER_PHOTO.name()));
            }
            return Optional.empty();
        }

        @Override
        public Class<SIRSFileReference> getOutputClass() {
            return SIRSFileReference.class;
        }

    }
}
