package fr.sirs.core.component;

import fr.sirs.core.model.Element;
import java.util.List;
import java.util.Map;

public interface DocumentListener {

    void documentCreated(Map<Class, List<Element>> added);

    void documentChanged(Map<Class, List<Element>> changed);

    void documentDeleted(Map<Class, List<Element>> deletedObject);
}
