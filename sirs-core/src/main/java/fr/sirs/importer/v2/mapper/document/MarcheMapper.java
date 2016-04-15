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
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.Organisme;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
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
public class MarcheMapper extends AbstractMapper<Marche> {

    private final AbstractImporter<Organisme> orgImporter;

    private enum Columns {
        ID_MAITRE_OUVRAGE,
        MONTANT_MARCHE,
        N_OPERATION
    };

    public MarcheMapper(Table table) {
        super(table);
        orgImporter = context.importers.get(Organisme.class);
        if (orgImporter == null) {
            throw new IllegalStateException("No importer found for class Organisme !");
        }
    }

    @Override
    public void map(Row input, Marche output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Integer maitreOuvrage = input.getInt(Columns.ID_MAITRE_OUVRAGE.toString());
        if (maitreOuvrage != null) {
            final String importedId = orgImporter.getImportedId(maitreOuvrage);
            if (importedId == null) {
                throw new AccessDbImporterException("No organism found for ID "+maitreOuvrage);
            }
            output.setMaitreOuvrageId(importedId);
        }

        final Double montant = input.getDouble(Columns.MONTANT_MARCHE.toString());
        if (montant != null) {
            output.setMontant(montant.floatValue());
        }

        final Integer operation = input.getInt(Columns.N_OPERATION.toString());
        if (operation != null) {
            output.setNumOperation(operation);
        }
    }

    @Component
    public static class Spi implements MapperSpi<Marche> {

        @Override
        public Optional<Mapper<Marche>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (!ImportContext.columnExists(inputType, c.name())) {
                    return Optional.empty();
                }
            }
            return Optional.of(new MarcheMapper(inputType));
        }

        @Override
        public Class<Marche> getOutputClass() {
            return Marche.class;
        }
    }
}
