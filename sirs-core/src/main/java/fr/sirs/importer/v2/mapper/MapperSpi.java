package fr.sirs.importer.v2.mapper;

import com.healthmarketscience.jackcess.Table;
import fr.sirs.importer.v2.ImportContext;
import java.util.Optional;

/**
 *
 * @author Alexis Manin (Geomatys)
 * @param <T> Managed output type.
 */
public interface MapperSpi<T> {

    /**
     * Ask SPI to prepare a mapper to work with a given table. If table format is not
     * compatible with available mapper capabilities, no mapper will be provided.
     *
     * @param inputType The table which must serve as enry point for the mapping.
     * @return True if current mapper can extract information from given table. False otherwise.
     */
    Optional<Mapper<T>> configureInput(final Table inputType) throws IllegalStateException;

    /**
     * @return type of object managed by this mapper.
     */
    Class<T> getOutputClass();

    public static String[] getEnumNames(final Enum[] source) {
        final String[] values = new String[source.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = source[i].name();
        }

        return values;
    }

    /**
     * Ensure that all column given as argument are present in given table
     *
     * @param source The table to test.
     * @param expected Column names we expect to find in input table.
     * @return True if all given column names have been found, false otherwise.
     */
    public static boolean checkColumns(final Table source, final Enum[] expected) {
        return checkColumns(source, getEnumNames(expected));
    }

    /**
     * Ensure that all column given as argument are present in given table
     *
     * @param source The table to test.
     * @param expected Column names we expect to find in input table.
     * @return True if all given column names have been found, false otherwise.
     */
    public static boolean checkColumns(final Table source, final String[] expected) {
        for (final String str : expected) {
            if (!ImportContext.columnExists(source, str)) {
                return false;
            }
        }
        return true;
    }
}
