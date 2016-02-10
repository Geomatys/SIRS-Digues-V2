package fr.sirs.plugins.synchro;

import fr.sirs.Session;
import org.ektorp.CouchDbConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class PhotoImportSession extends Session {

    @Autowired
    public PhotoImportSession(final CouchDbConnector connector) {
        super(connector);
    }
}
