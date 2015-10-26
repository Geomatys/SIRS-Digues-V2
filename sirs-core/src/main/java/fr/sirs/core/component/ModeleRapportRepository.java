package fr.sirs.core.component;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.report.ModeleRapport;
import org.ektorp.CouchDbConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ModeleRapportRepository extends AbstractSIRSRepository<ModeleRapport> {

    @Autowired
    public ModeleRapportRepository(CouchDbConnector db) {
        super(ModeleRapport.class, db);
    }

    @Override
    public ModeleRapport create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(type);
    }
}
