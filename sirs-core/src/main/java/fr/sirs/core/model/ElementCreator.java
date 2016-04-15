/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
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
