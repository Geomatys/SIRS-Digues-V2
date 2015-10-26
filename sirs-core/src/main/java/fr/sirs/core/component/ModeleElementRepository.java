package fr.sirs.core.component;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.report.ModeleElement;
import org.ektorp.CouchDbConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ModeleElementRepository extends AbstractSIRSRepository<ModeleElement> {

    @Autowired
    public ModeleElementRepository(CouchDbConnector db) {
        super(ModeleElement.class, db);
    }

    @Override
    public ModeleElement create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(type);
    }
}
