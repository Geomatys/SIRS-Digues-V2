package fr.sirs.importer;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.model.ReferenceType;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public abstract class GenericTypeReferenceImporter<T extends ReferenceType> extends GenericTypeImporter<T> {

    private static final String ID_PREFIX = "ID_";
    private static final String LIBELLE_PREFIX = "LIBELLE_";
    private static final String ABREGE_PREFIX = "ABREGE_";
    private static final String DATE_COLUMN = "DATE_DERNIERE_MAJ";

    /**
     * Name of the columns to import. Their order is important for import process :
     * 0 : id
     * 1 : libelle
     * 2 : abrege
     * 3 : date.
     */
    private final String[] columns;

    private Method setId;
    private Method setAbrege;

    public GenericTypeReferenceImporter() {
        super();

        final Table table;
        try {
            table = context.inputDb.getTable(getTableName());
        } catch (IOException ex) {
            throw new SirsCoreRuntimeException("Cannot create any importer for a reference type !");
        }

        columns = new String[4];
        for (final Column col : table.getColumns()) {
            String name = col.getName();
            switch (name) {
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
            final Class<T> outputClass = getOutputClass();
            setId = outputClass.getMethod("setId", outputClass);
            setId.setAccessible(true);
            setAbrege = outputClass.getMethod("setAbrege", outputClass);
            setId.setAccessible(true);
        } catch (Exception e) {
            throw new AccessDbImporterException("A required method cannot be found / accessed.", e);
        }
    }

    @Override
    protected void postCompute() throws AccessDbImporterException {
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
            setAbrege.invoke(output, row.getString(columns[2]));
        } catch (Exception e) {
            throw new SirsCoreRuntimeException("Cannot set reference attributes !", e);
        }

        final Date dateMaj = row.getDate(columns[3]);
        if (dateMaj != null) {
            output.setDateMaj(context.convertData(dateMaj, LocalDate.class));
        }

        output.setDesignation(refId.toString());
        return output;
    }




}
