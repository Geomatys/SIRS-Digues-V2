package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.TronconDigue;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.Property;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * 
 * @param <T> The type of the position of document.
 */
public class PositionDocumentPojoTable<T extends AbstractPositionDocument> extends ListenPropertyPojoTable<String> {

    public PositionDocumentPojoTable(Class<T> pojoClass, String title) {
        super(pojoClass, title);
    }
    
    @Override
    protected T createPojo() {
        final TronconDigue premierTroncon = Injector.getSession().getRepositoryForClass(TronconDigue.class).getOne();
        final T position = (T) super.createPojo(premierTroncon);
        
        try {
            ((Property<String>) propertyMethodToListen.invoke(position)).setValue(propertyReference);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PositionDocumentPojoTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        return position;
    }
}
