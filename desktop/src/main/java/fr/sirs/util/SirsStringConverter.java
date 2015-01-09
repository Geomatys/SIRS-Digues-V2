
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.PreviewLabel;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.WithLibelle;
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
        if (item instanceof WithLibelle) {
            text = ((WithLibelle)item).getLibelle();
        } else if (item instanceof ElementHit) {
            text = ((ElementHit) item).getLibelle();
        } else if (item instanceof PreviewLabel) {
            text = ((PreviewLabel) item).getLabel();
        } else if (item instanceof Contact) {
            final Contact c = (Contact) item;
            text = c.getNom() + " " + c.getPrenom();
        } else if (item instanceof Organisme) {
            text = ((Organisme)item).getNom();
        } else if (item instanceof String) {
            text = (String) item;
        } else if (item instanceof PreviewLabel) {
            text = ((PreviewLabel) item).getLabel();
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
