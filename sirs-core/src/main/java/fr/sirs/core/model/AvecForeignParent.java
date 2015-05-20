package fr.sirs.core.model;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public interface AvecForeignParent extends Element {
    
    String getForeignParentId();
    
    void setForeignParentId(final String id);
}
