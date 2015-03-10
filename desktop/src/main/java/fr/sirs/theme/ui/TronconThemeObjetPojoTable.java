package fr.sirs.theme.ui;

import fr.sirs.core.model.Objet;
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
public class TronconThemeObjetPojoTable extends TronconThemePojoTable<Objet>{

    public TronconThemeObjetPojoTable(AbstractTronconTheme.ThemeGroup group) {
        super(group);
    }

    @Override
    protected Objet createPojo() {
        Objet pojo = null;
        try {
            final TronconDigue trc = troncon.get();
            final Constructor pojoConstructor = pojoClass.getConstructor();
            pojo = (Objet) pojoConstructor.newInstance();
            trc.getStructures().add(pojo);
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
