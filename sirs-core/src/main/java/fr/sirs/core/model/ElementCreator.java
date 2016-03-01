package fr.sirs.core.model;

import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCoreRuntimeException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ElementCreator {
    
    private final SessionCore ownableSession;
    
    public ElementCreator(final SessionCore ownableSession){
        this.ownableSession = ownableSession;
    }
    
    public ElementCreator(){
        this(null);
    }
    
    /**
     * Create a new element of type T. 
     * 
     * If possible, this method sets the correct validity and author dependant 
     * on user's privileges of the session.
     * 
     * Do not add the element to the database.
     * 
     * @param <T> Type of the object to create.
     * @param clazz Type of the object to create.
     * @return A new, empty element of queried class.
     */
    public <T extends Element> T createElement(final Class<T> clazz){
        try {
            final Constructor<T> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            final T element = constructor.newInstance();
            
            if(ownableSession!=null) {
                element.setValid(!ownableSession.needValidationProperty().get());
                final Utilisateur utilisateur = ownableSession.getUtilisateur();
                if(ownableSession.getUtilisateur()!=null) {
                    element.setAuthor(utilisateur.getId());
                }
            }
            
            // Si on n'a pas de session, on crée un élément valide (importateurs)
            else{
                element.setValid(true);
            }
            
            return element;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new SirsCoreRuntimeException(ex.getMessage());
        }
    }
    
    public static <T extends Element> T createAnonymValidElement(final Class<T> clazz){
        try{
            final Constructor<T> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            final T element = constructor.newInstance();
            element.setValid(true);
            return element;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new SirsCoreRuntimeException(ex.getMessage());
        }
    }
}
