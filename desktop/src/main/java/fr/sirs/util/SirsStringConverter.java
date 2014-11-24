
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import javafx.util.StringConverter;

/**
 *
 * @author Johann Sorel
 */
public class SirsStringConverter extends StringConverter{

    @Override
    public String toString(Object item) {
        if(item instanceof SystemeReperageBorne){
            final SystemeReperageBorne srb = (SystemeReperageBorne) item;
            final Session session = Injector.getBean(Session.class);
            item = session.getBorneDigueRepository().get(srb.getBorneId());
        }

        String text = "";
        if(item instanceof Digue){
            text = ((Digue)item).getLibelle();
        }else if(item instanceof TronconDigue){
            text = ((TronconDigue)item).getLibelle();
        }else if(item instanceof BorneDigue){
            text = ((BorneDigue)item).getLibelle();
        }else if(item instanceof SystemeReperage){
            text = ((SystemeReperage)item).getLibelle();
        }else if(item instanceof String){
            text = (String) item;
        }
        
        return text;
    }

    @Override
    public Object fromString(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
