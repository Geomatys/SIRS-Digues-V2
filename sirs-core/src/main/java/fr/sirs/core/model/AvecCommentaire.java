package fr.sirs.core.model;

import javafx.beans.property.StringProperty;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public interface AvecCommentaire {
    
    public String getCommentaire();
    
    public void setCommentaire(String commentaire);
    
    public  StringProperty commentaireProperty();
}
