package fr.sirs.importer.v2.mapper;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.AvecDateMaj;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DateMajMapper extends AbstractMapper<AvecDateMaj> {

    private static final String DEFAULT_FIELD = "DATE_DERNIERE_MAJ";

    private final String fieldName;

    private DateMajMapper(final Table table, final String fieldName) {
        super(table);
        this.fieldName = fieldName;
    }

    @Override
    public void map(Row input, AvecDateMaj output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Date dateMaj = input.getDate(fieldName);
        if (dateMaj != null) {
            output.setDateMaj(context.convertData(dateMaj, LocalDate.class));
        }
    }

    public static class Spi implements MapperSpi<AvecDateMaj> {

        @Override
        public Optional<Mapper<AvecDateMaj>> configureInput(Table inputType) {
            String fieldName = null;
            if (inputType.getColumn(DEFAULT_FIELD) != null) {
                fieldName = DEFAULT_FIELD;
            } else {
                for (final Column c : inputType.getColumns()) {
                    if (c.getName().toUpperCase().startsWith(DEFAULT_FIELD)) {
                        fieldName = c.getName();
                        break;
                    }
                }
            }

            if (fieldName != null) {
                return Optional.of(new DateMajMapper(inputType, fieldName));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Class<AvecDateMaj> getOutputClass() {
            return AvecDateMaj.class;
        }
    }
}
