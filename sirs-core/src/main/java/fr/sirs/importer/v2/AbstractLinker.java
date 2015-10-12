package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Element;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * An importer which can be called to link content it imported to link it to the specified target.
 *
 * @author Alexis Manin (Geomatys)
 * @param <T> Type of object to import (target of the link).
 * @param <U> object type for the link holder.
 */
public abstract class AbstractLinker<T extends Element, U extends Element> extends AbstractImporter<T> implements Linker<T, U> {

    /**
     * Temporarily keeps target objects to link. Will be replaced with target IDs after they've been posted.
     */
    private final HashMap<Object, HashSet<T>> buffer = new HashMap<>();

    /**
     * Contains relation between row Ids and the value of their foreign key pointing to link holder.
     * Key is the id of the "link holder", and value is the list of "targets" bound to it.
     */
    private final HashMap<Object, HashSet<String>> holder2targetIds = new HashMap<>();

    public void link() throws IOException, AccessDbImporterException {

        final AbstractImporter<U> holderImporter = context.importers.get(getHolderClass());
        if (holderImporter == null) {
            throw new AccessDbImporterException("No importer found for type "+getHolderClass());
        }

        final AbstractSIRSRepository<U> holderRepo = session.getRepositoryForClass(getHolderClass());
        if (holderRepo == null) {
            throw new AccessDbImporterException("No repository found for type "+getHolderClass());
        }

        Iterator<Map.Entry<Object, HashSet<String>>> it = holder2targetIds.entrySet().iterator();

        int linkCount;
        while ((linkCount = holder2targetIds.size()) > 0) {
            final int bulkSize = StrictMath.min(linkCount, context.bulkLimit);
            final HashSet<U> holders = new HashSet<>(bulkSize);
            while (it.hasNext() && holders.size() < bulkSize) {
                Map.Entry<Object, HashSet<String>> next = it.next();
                final U holder = holderRepo.get(holderImporter.getImportedId(next.getKey()));
                bind(holder, next.getValue());
                holders.add(holder);

                it.remove();
            }

            context.executeBulk((Collection<Element>) holders);
        }
        context.linkCount.incrementAndGet();
    }

    /**
     * Create link to input id into given object.
     *
     * @param holder The object which will hold (contain) the link.
     * @param targetIds Ids (in CouchDB database) of the link targets.
     * @throws AccessDbImporterException If an error occurs while crating the link.
     */
    public abstract void bind(final U holder, final Collection<String> targetIds) throws AccessDbImporterException;

    /**
     *
     * @return The name of the column, in input ms-access table (as defined by {@link #getTableName() }), which contains link holder id.
     */
    public abstract String getHolderColumn();

    @Override
    public Class<T> getTargetClass() {
        return getElementClass();
    }

    @Override
    protected void afterPost(Map<String, Element> posted, Map<Object, T> imports) {
        super.afterPost(posted, imports);
        Iterator<Map.Entry<Object, HashSet<T>>> it = buffer.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, HashSet<T>> entry = it.next();
            HashSet<String> value = holder2targetIds.get(entry.getKey());
            if (value == null) {
                value = new HashSet<>();
                holder2targetIds.put(entry.getKey(), value);
            }
            for (final T target : entry.getValue()) {
                value.add(target.getId());
            }
            // clear checked buffer entry.
            it.remove();
        }
    }

    @Override
    protected Element prepareToPost(Object rowId, Row row, T output) {
        final String holderColumn = getHolderColumn();
        final Object holderId = row.get(holderColumn);
        if (holderId == null) {
            context.reportError(getTableName(), row, new IllegalArgumentException("Empty foreign key : "+holderColumn));
            //throw new AccessDbImporterException("Empty foreign key : "+holderColumn);
        } else {
            HashSet<T> value = buffer.get(holderId);
            if (value == null) {
                value = new HashSet<>();
                buffer.put(holderId, value);
            }
            value.add(output);
        }
        return super.prepareToPost(rowId, row, output);
    }
}
