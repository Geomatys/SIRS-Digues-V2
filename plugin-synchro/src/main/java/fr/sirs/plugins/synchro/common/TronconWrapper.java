package fr.sirs.plugins.synchro.common;

import fr.sirs.core.model.Objet;
import java.util.Optional;
import org.apache.sis.util.ArgumentChecks;

/**
 *
 * @author Matthieu Bastianelli (Geomatys)
 */

class TronconWrapper {
    private final Object document;
    protected final Optional<String> tronconId;

    TronconWrapper(final Object document) {
        ArgumentChecks.ensureNonNull("document", document);
        this.document = document;
         this.tronconId = tryFindTronçon(document);
    }

    TronconWrapper(final Object document, final Optional<String> tronconId) {
        ArgumentChecks.ensureNonNull("document", document);
        this.document = document;
         this.tronconId = tronconId;
    }

    TronconWrapper(final TronconWrapper tronconWrapper) {
        this.document = tronconWrapper.getDocument();
        this.tronconId = tronconWrapper.getTronconId();
    }

    static Optional<String> tryFindTronçon(final Object document) {
        if (document instanceof Objet) {
           return Optional.of( ((Objet) document).getForeignParentId() );
        } else {
           return Optional.empty();
        }
    }

    public final Optional<String> getTronconId() {
        return tronconId;
    }

    public final Object getDocument() {
        return document;
    }



}
