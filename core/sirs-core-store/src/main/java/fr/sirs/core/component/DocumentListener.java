package fr.sirs.core.component;

import fr.sirs.core.model.Element;

public interface DocumentListener {

	Element documentCreated(Element changed);

	Element documentChanged(Element changed);

	Element documentDeleted(Element deleteObject);
}
