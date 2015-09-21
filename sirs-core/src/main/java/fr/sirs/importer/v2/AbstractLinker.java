package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Element;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * An importer which can be called to link content it imported to link it to the specified target.
 *
 * @author Alexis Manin (Geomatys)
 * @param <T> Type of object to import (target of the link).
 * @param <U> object type for the link holder.
 */
public abstract class AbstractLinker<T extends Element, U extends Element> extends AbstractImporter<T> implements Linker<U> {

    /**
     * Contains relation between row Ids and the value of their foreign key pointing to link holder.
     * Key is the id of the "link holder", and value is the list of "targets" bound to it.
     */
    private final HashMap<Integer, HashSet<Integer>> holder2targetIds = new HashMap<>();

    @Override
    public void link(Integer accessContainerId, U container) throws AccessDbImporterException {
        final HashSet<Integer> targetIds = holder2targetIds.get(accessContainerId);
        if (targetIds == null) return;
        for (final Integer leveId : targetIds) {
            try {
                bind(container, getImportedId(leveId));
            } catch (IOException ex) {
                throw new AccessDbImporterException("Cannot link leves to their profile.", ex);
            }
        }
    }

    /**
     * Create link to input id into given object.
     *
     * @param holder The object which will hold (contain) the link.
     * @param targetId Id (in CouchDB database) of the link target.
     * @throws AccessDbImporterException If an error occurs while crating the link.
     */
    public abstract void bind(final U holder, final String targetId) throws AccessDbImporterException;

    /**
     *
     * @return The name of the column, in input ms-access table (as defined by {@link #getTableName() }), which contains link holder id.
     */
    public abstract String getHolderColumn();

    @Override
    public T importRow(Row row, T output) throws IOException, AccessDbImporterException {
        output = super.importRow(row, output);
        final String holderColumn = getHolderColumn();
        final Integer profilId = row.getInt(holderColumn);
        if (profilId == null) {
            throw new AccessDbImporterException("Empty foreign key : "+holderColumn);
        }
        HashSet<Integer> value = holder2targetIds.get(profilId);
        if (value == null) {
            value = new HashSet();
            holder2targetIds.put(profilId, value);
        }
        value.add(row.getInt(getRowIdFieldName()));

        return output;
    }
}
