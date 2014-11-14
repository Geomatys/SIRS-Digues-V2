package fr.sirs.core.component;

import fr.sirs.core.model.Element;

public interface DocumentListener {

    void documentCreated(Element changed);

    void documentChanged(Element changed);

    void documentDeleted(Element deleteObject);
}
