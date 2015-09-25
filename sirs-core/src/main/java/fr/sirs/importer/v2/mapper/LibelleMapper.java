package fr.sirs.importer.v2.mapper;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.AvecLibelle;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class LibelleMapper extends AbstractMapper<AvecLibelle> {

    private static final String DEFAULT_FIELD = "LIBELLE";

    private final String fieldName;

    private LibelleMapper(final Table table, final String fieldName) {
        super(table);
        this.fieldName = fieldName;
    }

    @Override
    public void map(Row input, AvecLibelle output) throws IllegalStateException, IOException, AccessDbImporterException {
        String libelle = input.getString(fieldName);
        if (libelle != null) {
            output.setLibelle(libelle);
        }
    }

    @Override
    public void close() throws IOException {}

    @Component
    public static class Spi implements MapperSpi<AvecLibelle> {

        @Override
        public Optional<Mapper<AvecLibelle>> configureInput(Table inputType) {
            for (final Column c : inputType.getColumns()) {
                if (c.getName().toUpperCase().startsWith(DEFAULT_FIELD)) {
                    return Optional.of(new LibelleMapper(inputType, c.getName()));
                }
            }

            return Optional.empty();
        }

        @Override
        public Class<AvecLibelle> getOutputClass() {
            return AvecLibelle.class;
        }
    }
}
