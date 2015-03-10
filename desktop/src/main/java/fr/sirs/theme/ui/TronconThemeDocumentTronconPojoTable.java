package fr.sirs.theme.ui;

import fr.sirs.core.model.DocumentTroncon;
import fr.sirs.core.model.Role;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.AbstractTronconTheme;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import org.apache.sis.util.logging.Logging;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TronconThemeDocumentTronconPojoTable extends TronconThemePojoTable<DocumentTroncon>{

    public TronconThemeDocumentTronconPojoTable(AbstractTronconTheme.ThemeGroup group) {
        super(group);
    }

    @Override
    protected DocumentTroncon createPojo() {
        DocumentTroncon pojo = null;
        try {
            final TronconDigue trc = troncon.get();
            final Constructor pojoConstructor = pojoClass.getConstructor();
            pojo = (DocumentTroncon) pojoConstructor.newInstance();
            trc.getDocumentTroncon().add(pojo);
            pojo.setParent(trc);
            pojo.setAuthor(session.getUtilisateur().getId());
            pojo.setValid(!(session.getRole().equals(Role.EXTERN)));
            session.getTronconDigueRepository().update(trc);
        } catch (Exception ex) {
            Logging.getLogger(TronconThemePojoTable.class).log(Level.WARNING, null, ex);
        }
        return pojo;
    }
    
}
