
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.query.ElementHit;
import java.util.WeakHashMap;
import javafx.util.StringConverter;

/**
 *
 * @author Johann Sorel
 */
public class SirsStringConverter extends StringConverter {

    private final WeakHashMap<String, Object> fromString = new WeakHashMap<>();
    
    @Override
    public String toString(Object item) {
        if(item instanceof SystemeReperageBorne){
            final SystemeReperageBorne srb = (SystemeReperageBorne) item;
            final Session session = Injector.getBean(Session.class);
            item = session.getBorneDigueRepository().get(srb.getBorneId());
        }

        String text = "";
        if (item instanceof Digue) {
            text = ((Digue) item).getLibelle();
        } else if (item instanceof TronconDigue) {
            text = ((TronconDigue) item).getLibelle();
        } else if (item instanceof BorneDigue) {
            text = ((BorneDigue) item).getLibelle();
        } else if (item instanceof SystemeReperage) {
            text = ((SystemeReperage) item).getLibelle();
        } else if (item instanceof ElementHit) {
            text = ((ElementHit) item).getLibelle();
        } else if (item instanceof Contact) {
            final Contact c = (Contact) item;
            text = c.getNom() + " " + c.getPrenom();
        } else if (item instanceof Organisme) {
            text = ((Organisme)item).getNom();
        } else if (item instanceof String) {
            text = (String) item;
        }
        
        if (text != null && !text.isEmpty()) {
            fromString.put(text, item);
        }
        return text;
    }

    @Override
    public Object fromString(String string) {
        return fromString.get(string);
    }
    
}
