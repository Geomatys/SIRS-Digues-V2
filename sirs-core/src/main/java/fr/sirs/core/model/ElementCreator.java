package fr.sirs.core.model;

import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCoreRuntimeExecption;
import static fr.sirs.core.model.Role.ADMIN;
import static fr.sirs.core.model.Role.GUEST;
import static fr.sirs.core.model.Role.USER;
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
    
    public <T extends Element> T createElement(final Class<T> clazz){
        try {
            final Constructor<T> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            final T element = constructor.newInstance();
            
            if(ownableSession!=null){
                if(ownableSession.getUtilisateur()!=null){
                    final Utilisateur utilisateur = ownableSession.getUtilisateur();
                    if(utilisateur.getRole()!=null){
                        final Role role = utilisateur.getRole();
                        element.setValid(role==ADMIN||role==USER||role==GUEST);
                    }else{ // Si on a un utilisateur qui n'a pas de role
                        element.setValid(false);
                    }
                    if(utilisateur.getId()!=null){
                        element.setAuthor(utilisateur.getId());
                    }
                }
            }
            
            // Si on n'a pas de session, on crée un élément valide (importateurs)
            else{
                element.setValid(true);
            }
            
            return element;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new SirsCoreRuntimeExecption(ex.getMessage());
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
            throw new SirsCoreRuntimeExecption(ex.getMessage());
        }
    }
    
}
