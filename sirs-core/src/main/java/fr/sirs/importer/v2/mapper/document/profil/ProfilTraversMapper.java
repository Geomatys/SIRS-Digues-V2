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
package fr.sirs.importer.v2.mapper.document.profil;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
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
public class ProfilTraversMapper extends AbstractMapper<ProfilTravers> {

    private enum Columns {
        NOM
    }

    public ProfilTraversMapper(Table table) {
        super(table);
    }

    @Override
    public void map(Row input, ProfilTravers output) throws IllegalStateException, IOException, AccessDbImporterException {
        String name = input.getString(Columns.NOM.name());
        if (name != null) {
            output.setLibelle(name);
        }
    }

    @Component
    public static class Spi implements MapperSpi<ProfilTravers> {

        @Override
        public Optional<Mapper<ProfilTravers>> configureInput(Table inputType) throws IllegalStateException {
            if (MapperSpi.checkColumns(inputType, Columns.values())) {
                return Optional.of(new ProfilTraversMapper(inputType));
            }
            return Optional.empty();
        }

        @Override
        public Class<ProfilTravers> getOutputClass() {
            return ProfilTravers.class;
        }

    }
}
