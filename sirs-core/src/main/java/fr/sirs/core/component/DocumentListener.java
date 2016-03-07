package fr.sirs.core.component;

import fr.sirs.core.model.Element;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Component receiving notifications for events sent by {@link DocumentChangeEmiter}.
 *
 * @author Alexis Manin (Geomatys)
 */
public interface DocumentListener {

    /**
     * Called when a set of elements have been added in database.
     * @param added Set of added elements, sorted by type.
     */
    void documentCreated(Map<Class, List<Element>> added);

    /**
     * Called when a set of elements have been updated in database.
     * @param changed Set of updated elements, sorted by type.
     */
    void documentChanged(Map<Class, List<Element>> changed);

    /**
     * Called when a set of elements have been deleted from database.
     * @param deleted Ids of removed elements.
     */
    void documentDeleted(Set<String> deleted);
}
