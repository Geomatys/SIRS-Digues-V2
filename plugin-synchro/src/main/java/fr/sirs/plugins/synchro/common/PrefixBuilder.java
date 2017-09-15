package fr.sirs.plugins.synchro.common;

import fr.sirs.core.model.SIRSFileReference;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PrefixBuilder implements Function<SIRSFileReference, String> {

    final List<PropertyDescriptor> prefixes;
    final String separator;

    public PrefixBuilder(List<PropertyDescriptor> prefixes, String separator) {
        this.prefixes = prefixes;
        this.separator = separator;
    }

    @Override
    public String apply(SIRSFileReference doc) {
        if (doc.getChemin() == null) {
            throw new IllegalArgumentException("Cannot build a prefix for a document without attached file.");
        }

        final String fileName = Paths.get(doc.getChemin()).getFileName().toString();
        if (prefixes == null || prefixes.isEmpty()) {
            return fileName;
        }

        final StringJoiner joiner = new StringJoiner(separator, "", fileName);
        final StringBuilder nameBuilder = new StringBuilder();
        Object prefixValue;
        for (final PropertyDescriptor desc : prefixes) {
            Method readMethod = desc.getReadMethod();
            if (readMethod != null) {
                try {
                    prefixValue = readMethod.invoke(doc);
                    if (prefixValue != null) {
                        nameBuilder.append(prefixValue);
                    }
                } catch (ReflectiveOperationException ex) {
                    throw new RuntimeException("Cannot get value for prefix " + desc.getName(), ex);
                }
            }
        }

        return joiner.toString();
    }
}
