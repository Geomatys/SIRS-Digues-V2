package fr.symadrem.sirs.core.component;

import fr.symadrem.sirs.core.model.Element;

public interface DocumentListener {

	Element documentCreated(Element changed);

	Element documentChanged(Element changed);

	Element documentDeleted(Element deleteObject);
}
