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

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.ImportContext;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ForeignParentMapper extends AbstractMapper<AvecForeignParent> {

    private static final String TRONCON_ID_COLUMN = "ID_TRONCON_GESTION";
    private final AbstractImporter<TronconDigue> tdImporter;

    private ForeignParentMapper(final Table t) {
        super(t);
        tdImporter = context.importers.get(TronconDigue.class);
        if (tdImporter == null) {
            throw new IllegalStateException("Missing import resource !");
        }
    }

    @Override
    public void map(Row input, AvecForeignParent output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Object tdId = input.get(TRONCON_ID_COLUMN);
        if (tdId != null) {
            final String importedId = tdImporter.getImportedId(tdId);
            if (importedId != null) {
                output.setForeignParentId(importedId);
                return;
            }
        }
        throw new AccessDbImporterException("Input row does not reference any valid " + TRONCON_ID_COLUMN);
    }

    @Component
    public static class Spi implements MapperSpi<AvecForeignParent> {

        @Override
        public Optional<Mapper<AvecForeignParent>> configureInput(Table inputType) throws IllegalStateException {
            if (ImportContext.columnExists(inputType, TRONCON_ID_COLUMN)) {
                return Optional.of(new ForeignParentMapper(inputType));
            }
            return Optional.empty();
        }

        @Override
        public Class<AvecForeignParent> getOutputClass() {
            return AvecForeignParent.class;
        }
    }
}
