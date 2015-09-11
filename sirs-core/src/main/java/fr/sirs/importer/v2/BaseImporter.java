package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.Identifiable;
import fr.sirs.core.model.Positionable;
import fr.sirs.util.property.Reference;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class BaseImporter {

    @Autowired
    private transient ImportContext context;

    protected transient Class binding;
    protected transient Map<String, PropertyDescriptor> bindingProperties;

    protected String sourceTable;

    protected Map<String, String> attributes;
    protected Map<String, Relation> relations;
    private transient HashMap<Integer, String> idBindings;

    public final String getTableName() {
        return sourceTable;
    }

    public String getTargetClass() {
        return binding.getCanonicalName();
    }

    public void setTargetClass(final String param) throws ClassNotFoundException, IntrospectionException {
        // Use thread context class loader as SIRS plugins should not be registered in this class ClassLoader.
        binding = Thread.currentThread().getContextClassLoader().loadClass(param);
        bindingProperties = getProperties(binding);
//        GenericImporter old = context.importers.put(binding, this);
//        if (old != null) {
//            SirsCore.LOGGER.log(Level.WARNING, "Importer conflict for class "+binding);
//        }
    }

    public public  importRow(final Row row) throws Exception {
        Object target = binding.newInstance();

        for (final Map.Entry<String, String> entry : attributes.entrySet()) {
            try {
                setField(row.get(entry.getValue()), target, entry.getKey());
            } catch (NoSuchMethodException e) {
                context.reportError(new ErrorReport(e, row, sourceTable, entry.getValue(), target, entry.getKey(), "No accessor available for target field "+entry.getKey(), CorruptionLevel.FIELD));
            }
        }

        for (final Map.Entry<String, Relation> entry : relations.entrySet()) {
            setRelation(row, entry.getValue(), target, entry.getKey());
        }

        if (target instanceof Positionable) {
            context.setGeoPositions(row, (Positionable)target);
        }

        return target;
    }

    /**
     * Import a field of source row into specified property of target object.
     * @param sourceData object to set in the target field.
     * @param target Target object.
     * @param targetFieldName Field to affect in target object.
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException If no method can be found in target object to set wanted field.
     */
    protected void setField(Object sourceData, final Object target, final String targetFieldName) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (sourceData != null) {
            final PropertyDescriptor desc = bindingProperties.get(targetFieldName);
            final Method readMethod = desc.getReadMethod();
            if (readMethod == null) {
                throw new NoSuchMethodException("No method available to read following field : " + targetFieldName);
            }
            final boolean isReference = readMethod.getAnnotation(Reference.class) != null;
            // If target field is a link to another document id, we try to ensure source data is an id.
            if (isReference) {
                if (sourceData instanceof Identifiable)
                    sourceData = ((Identifiable) sourceData).getId();
            }

            /* If we've got a collection, we get it then add source data into it.
             * If source is also a collection or array, we add all of its content
             * into target collection. Otherwise, we just call field setter. If
             * source data class is incompatible with setter type, we try to
             * convert it.
             *
             * TODO : find a better system fo field affectation (field type
             * analysis which would find a correct setter ?)
             */
            if (Collection.class.isAssignableFrom(desc.getPropertyType())) {
                final Collection targetField = (Collection) readMethod.invoke(target);
                if (sourceData instanceof Collection) {
                    targetField.addAll((Collection) sourceData);
                } else if (sourceData.getClass().isArray()) {
                    targetField.addAll(Arrays.asList(sourceData));
                } else {
                    targetField.add(sourceData);
                }
            } else {
                final Method writeMethod = desc.getWriteMethod();
                if (writeMethod == null) {
                    throw new NoSuchMethodException("No method available to set following field : " + targetFieldName);

                } else {
                    if (sourceData.getClass().isAssignableFrom(desc.getPropertyType())) {
                        writeMethod.invoke(target, sourceData);
                    } else {
                        writeMethod.invoke(target, context.convertData(sourceData, desc.getPropertyType()));
                    }
                }
            }
        }
    }

    protected void setRelation(final Row sourceRow, final Relation relation, final Object target, final String targetFieldName) throws IOException, InvocationTargetException, IllegalAccessException {
        final Object sourceData = sourceRow.get(relation.localColumn);
        final Table foreignTable = context.inputDb.getTable(relation.foreignTable);
        final Column foreignColumn = foreignTable.getColumn(relation.foreignColumn);

        Cursor cursor = foreignTable.getDefaultCursor();
        while (cursor.findNextRow(foreignColumn, sourceData)) {
            final Object foreignData;
            if (relation.foreignProperty == null) {
                foreignData = cursor.getCurrentRow().get(relation.foreignColumn);
            } else if (relation.foreignProperty.equals("*")) {
                // TODO : implement following method
                foreignData = context.getBoundTarget(cursor.getCurrentRow());
            } else {
                foreignData = cursor.getCurrentRow().get(relation.foreignProperty);
            }

            try {
                setField(foreignData, target, targetFieldName);
            } catch (NoSuchMethodException e) {
                context.reportError(new ErrorReport(e, sourceRow, sourceTable, relation.localColumn, target, targetFieldName, "No accessor available for target field "+targetFieldName, CorruptionLevel.FIELD));
            }
        }
    }

    /**
     * Retrieve properties of a given class, and sort them to be able to easily retrieve them per name.
     * @param targetClass Class to analyze.
     * @return A map of found property, whose keys are property names, and value their descriptor.
     * @throws IntrospectionException If an error happens while analysing input class.
     */
    public static Map<String, PropertyDescriptor> getProperties(final Class targetClass) throws IntrospectionException {
        final BeanInfo bInfo = Introspector.getBeanInfo(targetClass);
        final PropertyDescriptor[] descriptors = bInfo.getPropertyDescriptors();
        final HashMap<String, PropertyDescriptor> result = new HashMap<>();
        for (final PropertyDescriptor desc : descriptors) {
            result.put(desc.getName(), desc);
            if (desc.getReadMethod() != null) {
                desc.getReadMethod().setAccessible(true);
            }
            if (desc.getWriteMethod() != null) {
                desc.getWriteMethod().setAccessible(true);
            }
        }
        return result;
    }
}
