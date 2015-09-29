package fr.sirs.importer.v2.references;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public abstract class GenericTypeReferenceImporter<T extends ReferenceType> extends AbstractImporter<T> {

    private static final String ID_PREFIX = "ID";
    private static final String LIBELLE_PREFIX = "LIBELLE";
    private static final String ABREGE_PREFIX = "ABREGE";
    private static final String DATE_COLUMN = "DATE_DERNIERE_MAJ";

    /**
     * Name of the columns to import. Their order is important for import
     * process : 0 : id 1 : libelle 2 : abrege 3 : date.
     */
    private final String[] columns;

    private Method setId;
    private Method setAbrege;

    public GenericTypeReferenceImporter() {
        super();
        columns = new String[4];
    }

    @PostConstruct
    private void indexColumns() {
        final Table table;
        try {
            table = context.inputDb.getTable(getTableName());
        } catch (IOException ex) {
            throw new SirsCoreRuntimeException("Cannot create any importer for a reference type !");
        }

        String name;
        String start;
        for (final Column col : table.getColumns()) {
            name = col.getName();
            start = name.substring(0, name.indexOf('_')).toUpperCase();
            switch (start) {
                case ID_PREFIX:
                    columns[0] = name;
                    break;
                case LIBELLE_PREFIX:
                    columns[1] = name;
                    break;
                case ABREGE_PREFIX:
                    columns[2] = name;
                    break;
            }
        }
        columns[3] = DATE_COLUMN;
    }

    @Override
    public List<String> getUsedColumns() {
        return Arrays.asList(columns);
    }

    @Override
    public String getRowIdFieldName() {
        return columns[0];
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();

        try {
            final Class<T> outputClass = getElementClass();
            setId = outputClass.getMethod("setId", String.class);
            setId.setAccessible(true);
            // Some reference table don't have an "Abrege" column.
            if (columns[2] != null) {
                setAbrege = outputClass.getMethod("setAbrege", String.class);
            }
            setId.setAccessible(true);
        } catch (Exception e) {
            throw new AccessDbImporterException("A required method cannot be found / accessed.", e);
        }
    }

    @Override
    protected void postCompute() {
        super.postCompute();

        setId = null;
        setAbrege = null;
    }

    @Override
    public T importRow(Row row, T output) throws IOException, AccessDbImporterException {
        final Integer refId = row.getInt(columns[0]);

        try {
            setId.invoke(output, output.getClass().getSimpleName() + ":" + refId);
            output.setLibelle(row.getString(columns[1]));
            if (setAbrege != null)
                setAbrege.invoke(output, row.getString(columns[2]));
        } catch (Exception e) {
            throw new SirsCoreRuntimeException("Cannot set reference attributes !", e);
        }

        final Date dateMaj = row.getDate(columns[3]);
        if (dateMaj != null) {
            output.setDateMaj(context.convertData(dateMaj, LocalDate.class));
        }

        return output;
    }
}
