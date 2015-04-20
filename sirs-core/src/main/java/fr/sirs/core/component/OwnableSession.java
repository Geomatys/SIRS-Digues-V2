package fr.sirs.core.component;

import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.Utilisateur;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public interface OwnableSession {
    Utilisateur getUtilisateur();
    ElementCreator getElementCreator();
}
