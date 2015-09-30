package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Element;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.util.property.Reference;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.BiConsumer;
import org.apache.sis.util.ArgumentChecks;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Create links between object using an MS-Access join table.
 *
 * @author Alexis Manin (Geomatys)
 * @param <T> Target element type --> type pointed by the link.
 * @param <U> Holder element type. Type containing the link.
 */
public abstract class JoinTableLinker<T extends Element, U extends Element> implements Linker<T, U> {

    @Autowired
    protected ImportContext context;

    @Autowired
    protected SessionCore session;

    private final String tableName;
    private final String targetColumn;
    private final String holderColumn;
    private final Class<T> targetType;
    private final Class<U> holderType;

    private final BiConsumer<String, U> linkAffector;

    private final BiConsumer<String, T> bidirectionalAffector;

    protected JoinTableLinker(final String tableName, final Class<T> targetType, final Class<U> holderType, final String targetColumn, final String holderColumn) {
        this(tableName, targetType, holderType, targetColumn, holderColumn, false);
    }

    protected JoinTableLinker(final String tableName, final Class<T> targetType, final Class<U> holderType, final String targetColumn, final String holderColumn, final boolean bidirectional) {
        ArgumentChecks.ensureNonNull("Table name", tableName);
        ArgumentChecks.ensureNonNull("Target column name", targetColumn);
        ArgumentChecks.ensureNonNull("Holder column name", holderColumn);
        ArgumentChecks.ensureNonNull("Target type", targetType);
        ArgumentChecks.ensureNonNull("Holder type", holderType);
        this.tableName = tableName;
        this.targetColumn = targetColumn;
        this.holderColumn = holderColumn;
        this.targetType = targetType;
        this.holderType = holderType;

        try {
            linkAffector = getLinkAffector(targetType, holderType);
            if (bidirectional) {
                bidirectionalAffector = getLinkAffector(holderType, targetType);
            } else {
                bidirectionalAffector = null;
            }
        } catch (IntrospectionException ex) {
            throw new IllegalStateException("An error occurred while analyzing a class.", ex);
        }
    }

    public final String getTableName() {
        return tableName;
    }

    /**
     * @return Type of the object from which we'll pick the ID to refer to.
     */
    @Override
    public final Class<T> getTargetClass() {
        return targetType;
    }

    /**
     * @return Name of the column containing target object ids
     */
    public final String getTargetColumn() {
        return targetColumn;
    }

    /**
     *
     * @return Type of the object which will contain ID of the object to point
     * to.
     */
    @Override
    public final Class<U> getHolderClass() {
        return holderType;
    }

    /**
     * @return Name of the column containing holder object ids
     */
    public final String getHolderColumn() {
        return holderColumn;
    }

    public void link() throws AccessDbImporterException, IOException {
        Iterator<Row> iterator = context.inputDb.getTable(tableName).iterator();

        final AbstractImporter<T> targetImporter = context.importers.get(targetType);
        if (targetImporter == null) {
            throw new AccessDbImporterException("No importer found for type " + targetType.getCanonicalName());
        }

        final AbstractImporter<U> holderImporter = context.importers.get(holderType);
        if (holderImporter == null) {
            throw new AccessDbImporterException("No importer found for type " + holderType.getCanonicalName());
        }

        final AbstractSIRSRepository<U> holderRepo = session.getRepositoryForClass(holderType);
        if (holderRepo == null) {
            throw new AccessDbImporterException("No repository available to get/update objects of type " + holderType.getCanonicalName());
        }

        final AbstractSIRSRepository<T> targetRepo;
        if (bidirectionalAffector != null) {
            targetRepo = session.getRepositoryForClass(targetType);
            if (targetRepo == null) {
                throw new AccessDbImporterException("No repository available to get/update objects of type " + targetType.getCanonicalName());
            }
        } else {
            targetRepo = null;
        }

        final HashSet<Element> toUpdate = new HashSet<>();

        String holderId, targetId;
        U holder;
        Row current;
        while (iterator.hasNext()) {

            // Split execution in bulks
            while (iterator.hasNext() && toUpdate.size() < context.bulkLimit) {
                current = iterator.next();

                // Those fields should be SQL join table keys, so they should never be null.
                try {
                    holderId = holderImporter.getImportedId(current.get(holderColumn));
                } catch (IllegalStateException e) {
                    context.reportError(new ErrorReport(e, current, tableName, holderColumn, null, null, "No imported object found for input Id.", CorruptionLevel.ROW));
                    continue;
                }

                try {
                    targetId = targetImporter.getImportedId(current.get(targetColumn));
                } catch (IllegalStateException e) {
                    context.reportError(new ErrorReport(e, current, tableName, targetColumn, null, null, "No imported object found for input Id.", CorruptionLevel.ROW));
                    continue;
                }

                holder = holderRepo.get(holderId);
                linkAffector.accept(targetId, holder);
                toUpdate.add(holder);

                if (bidirectionalAffector != null) {
                    final T inverseHolder = targetRepo.get(targetId);
                    bidirectionalAffector.accept(holderId, inverseHolder);
                    toUpdate.add(inverseHolder);
                }
            }

            context.executeBulk(toUpdate);
            toUpdate.clear();
        }
    }

    private <A> BiConsumer<String, A> getLinkAffector(final Class targetType, final Class<A> holderType) throws IntrospectionException {
        final PropertyDescriptor property = findLinkProperty(targetType, holderType);

        final Class holderPropertyType = property.getPropertyType();
        /*
         * We create the operator which will affect our property. If output property
         * is a collection, we simply add into. otherwise, we try to invoke its
         * write method.
         */
        if (Collection.class.isAssignableFrom(holderPropertyType)) {
            final Method readMethod = property.getReadMethod();
            return (targetId, holder) -> {
                try {
                    ((Collection) readMethod.invoke(holder)).add(targetId);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new SirsCoreRuntimeException("Invalid reflection statement !", ex);
                }
            };
        } else {
            final Method writeMethod = property.getWriteMethod();
            if (writeMethod == null) {
                throw new IllegalArgumentException("Unmodifiable property " + property.getName() + " from class " + holderType.getCanonicalName());
            }
            writeMethod.setAccessible(true);
            return (targetId, holder) -> {
                try {
                    writeMethod.invoke(holder, targetId);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new SirsCoreRuntimeException("Invalid reflection statement !", ex);
                }
            };
        }
    }

    /**
     * Analyze holder class to find the property able to contains a link to
     * target class.
     *
     * @return A property descriptor which represents the link property.
     * @throws IntrospectionException If an error occurred while analyzing the
     * class.
     * @throws IllegalArgumentException If we cannot find a matching property.
     */
    private PropertyDescriptor findLinkProperty(final Class targetType, final Class holderType) throws IntrospectionException {
        final BeanInfo info = Introspector.getBeanInfo(holderType);
        final PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        ArrayList<PropertyDescriptor> candidates = new ArrayList<>();
        for (final PropertyDescriptor desc : descriptors) {
            final Method readMethod = desc.getReadMethod();
            if (readMethod == null) {
                throw new IllegalArgumentException("Unreadable property " + desc.getName() + " from class " + holderType.getCanonicalName());
            } else {
                readMethod.setAccessible(true);
            }
            // Check if we've got a real data or a link.
            final Reference ref = readMethod.getAnnotation(Reference.class);
            if (ref != null) {
                if (targetType.equals(ref.ref())) {
                    candidates.add(0, desc);
                    break;
                } else if (targetType.isAssignableFrom(ref.ref())) {
                    candidates.add(desc);
                }
            }
        }

        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No property available in class " + holderType.getCanonicalName() + " to hold a link to an object of type " + targetType.getCanonicalName());
        }

        return candidates.get(0);
    }
}
