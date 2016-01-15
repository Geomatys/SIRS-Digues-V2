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
