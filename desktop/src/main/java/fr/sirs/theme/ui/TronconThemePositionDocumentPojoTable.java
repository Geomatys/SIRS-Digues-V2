package fr.sirs.theme.ui;

import fr.sirs.core.model.PositionDocument;
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
public class TronconThemePositionDocumentPojoTable extends TronconThemePojoTable<PositionDocument>{

    public TronconThemePositionDocumentPojoTable(AbstractTronconTheme.ThemeGroup group) {
        super(group);
    }

    @Override
    protected PositionDocument createPojo() {
        PositionDocument pojo = null;
        try {
            final TronconDigue trc = troncon.get();
            final Constructor pojoConstructor = pojoClass.getConstructor();
            pojo = (PositionDocument) pojoConstructor.newInstance();
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
