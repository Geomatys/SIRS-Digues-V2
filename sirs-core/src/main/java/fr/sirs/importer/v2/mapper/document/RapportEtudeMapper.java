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
import fr.sirs.core.model.RapportEtude;
import fr.sirs.core.model.RefRapportEtude;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.mapper.MapperSpi;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class RapportEtudeMapper extends AbstractMapper<RapportEtude>{

    private final AbstractImporter<RapportEtude> typeImporter;

    private enum Columns {
        TITRE_RAPPORT_ETUDE,
        ID_TYPE_RAPPORT_ETUDE,
        AUTEUR_RAPPORT,
        DATE_RAPPORT
    };

    public RapportEtudeMapper(Table table) {
        super(table);
        typeImporter = context.importers.get(RefRapportEtude.class);
        if (typeImporter == null) {
            throw new IllegalStateException("No importer found for "+RefRapportEtude.class);
        }
    }

    @Override
    public void map(Row input, RapportEtude output) throws IllegalStateException, IOException, AccessDbImporterException {
        final String title = input.getString(Columns.TITRE_RAPPORT_ETUDE.toString());
        if (title != null) {
            output.setLibelle(title);
        }

        // Apparently, not a reference to any contact. It's just a character sequence.
        String author = input.getString(Columns.AUTEUR_RAPPORT.name());
        if (author != null) {
            output.setAuteur(author);
        }

        final Date date = input.getDate(Columns.DATE_RAPPORT.toString());
        if (date != null) {
            output.setDate(context.convertData(date, LocalDate.class));
        }

        final Integer rapportType = input.getInt(Columns.ID_TYPE_RAPPORT_ETUDE.toString());
        if (rapportType != null) {
            output.setTypeRapportEtudeId(typeImporter.getImportedId(rapportType));
        }
    }

    @Component
    public static class Spi implements MapperSpi<RapportEtude> {

        @Override
        public Optional<Mapper<RapportEtude>> configureInput(Table inputType) throws IllegalStateException {
            if (MapperSpi.checkColumns(inputType, Columns.values())) {
                return Optional.of(new RapportEtudeMapper(inputType));
            }
            return Optional.empty();
        }

        @Override
        public Class<RapportEtude> getOutputClass() {
            return RapportEtude.class;
        }
    }
}
