package fr.sirs.importer;

import fr.sirs.core.model.Element;
import fr.sirs.importer.v2.AbstractImporter;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author samuel
 */
abstract class GenericTypeImporter<T extends Element> extends AbstractImporter<T> {

    protected Map<Integer, T> types = null;

    public Map<Integer, T> getTypeReferences() throws IOException, AccessDbImporterException {
        if(types == null) compute();
        return types;
    }
}
