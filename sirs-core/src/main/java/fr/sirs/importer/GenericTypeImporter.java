package fr.sirs.importer;

import fr.sirs.core.model.Element;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author samuel
 */
abstract class GenericTypeImporter<T extends Element> extends DocumentImporter<T> {

    protected Map<Integer, T> types = null;

    public Map<Integer, T> getTypeReferences() throws IOException, AccessDbImporterException {
        if(types == null) compute();
        return types;
    }
}
