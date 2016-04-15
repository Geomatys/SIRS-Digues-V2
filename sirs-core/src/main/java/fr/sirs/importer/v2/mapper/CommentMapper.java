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
import fr.sirs.core.model.AvecCommentaire;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class CommentMapper extends AbstractMapper<AvecCommentaire> {

    private static final String[] DEFAULT_FIELDS = new String[]{"DESCRIPTION_", "COMMENTAIRE"};

    private final String[] fieldNames;

    final BiConsumer<Row, AvecCommentaire> mapper;

    private CommentMapper(final Table t, String[] fieldNames) {
        super(t);
        this.fieldNames = fieldNames;
        if (fieldNames.length > 1) {
            mapper = this::mapMultiple;
        } else {
            mapper = this::mapSingle;
        }
    }

    @Override
    public void map(Row input, AvecCommentaire output) throws IllegalStateException, IOException, AccessDbImporterException {
        mapper.accept(input, output);
    }

    private void mapSingle(final Row input, final AvecCommentaire output) {
        final String comment = input.getString(fieldNames[0]);
        if (comment != null) {
            output.setCommentaire(comment);
        }
    }

    private void mapMultiple(final Row input, final AvecCommentaire output) {
        final StringBuilder commentBuilder = new StringBuilder();

        String comment = input.getString(fieldNames[0]);
        if (comment != null) {
            commentBuilder.append(comment);
        }

        for (int i = 1; i < fieldNames.length; i++) {
            comment = input.getString(fieldNames[i]);
            if (comment != null) {
                commentBuilder.append(System.lineSeparator()).append(comment);
            }
        }

        if (commentBuilder.length() > 0) {
            output.setCommentaire(comment);
        }
    }

    @Component
    public static class Spi implements MapperSpi<AvecCommentaire> {

        @Override
        public Optional<Mapper<AvecCommentaire>> configureInput(Table inputType) {
            final ArrayList<String> foundComments = new ArrayList<>(DEFAULT_FIELDS.length);
            for (final Column c : inputType.getColumns()) {
                for (final String expected : DEFAULT_FIELDS) {
                    if (c.getName().toUpperCase().startsWith(expected.toUpperCase())) {
                        foundComments.add(c.getName());
                    }
                    if (foundComments.size() >= DEFAULT_FIELDS.length)
                        break;
                }
            }

            if (foundComments.size() > 0) {
                return Optional.of(new CommentMapper(inputType, foundComments.toArray(new String[foundComments.size()])));
            }
            return Optional.empty();
        }

        @Override
        public Class<AvecCommentaire> getOutputClass() {
            return AvecCommentaire.class;
        }
    }
}
