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
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.document.JournalRegistry;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ArticleJournalMapper extends AbstractMapper<ArticleJournal> {

    @Autowired
    private JournalRegistry registry;

    private enum Columns {
        ID_JOURNAL,
        INTITULE_ARTICLE,
        DATE_ARTICLE
    }

    public ArticleJournalMapper(Table table) {
        super(table);
    }

    @Override
    public void map(Row input, ArticleJournal output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Object jId = input.get(Columns.ID_JOURNAL.toString());
        if (jId != null) {
            String title = registry.getTitle(jId);
            if (title != null) {
                output.setNomJournal(title);
            }
        }

        final String libelle = input.getString(Columns.INTITULE_ARTICLE.toString());
        if (libelle != null) {
            output.setLibelle(libelle);
        }

        final Date date = input.getDate(Columns.DATE_ARTICLE.toString());
        if (date != null) {
            output.setDateArticle(context.convertData(date, LocalDate.class));
        }
    }

    @Component
    public static class Spi implements MapperSpi<ArticleJournal> {

        @Override
        public Optional<Mapper<ArticleJournal>> configureInput(Table inputType) throws IllegalStateException {
            if (MapperSpi.checkColumns(inputType, Columns.values())) {
                return Optional.of(new ArticleJournalMapper(inputType));
            }
            return Optional.empty();
        }

        @Override
        public Class<ArticleJournal> getOutputClass() {
            return ArticleJournal.class;
        }

    }
}
