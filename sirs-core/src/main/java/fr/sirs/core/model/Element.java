package fr.sirs.core.model;

import java.io.Serializable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public interface Element extends Identifiable, Serializable {

    /**
     * @return the parent {@link Element} of the current object, or itself if 
     * its a CouchDB document root node.
     * Can be null for newly created objects which has not been saved in database 
     * yet.
     */ 
    Element getCouchDBDocument();

    /**
     * 
     * @return The CouchDb identifier of the element backed by {@linkplain #getCouchDBDocument() }.
     */
    String getDocumentId();
    
    /**
     * @return the parent {@link Element} of the current element, or null if no parent is set.
     * Note that no CouchDB document has a parent property. Only contained elements have one.
     */
    public ObjectProperty<Element> parentProperty();
    
    /**
     * @return the parent {@link Element} of the current element, or null if no parent is set.
     * Note that no CouchDB document has a parent property. Only contained elements have one.
     */
    public Element getParent();
    
    /**
     * Set parent for current element. If the current element is a CouchDb document,
     * No parent can be set, and calling this method has no effect.
     * @param parent an element to set as owner for this one.
     */
    void setParent(Element parent);
    
    /**
     * Create a new Element instance, of the same type than the current one, with
     * the same attributes / references.
     * Note : The newly created element has one significant difference with the 
     * original one : It's not attached to any parent element.
     * @return A new element which is the exact copy of the current one.
     */
    Element copy();
    
    /**
     * Remove the given element from the ones contained into the current document.
     * If the current element is not a CouchDb document, or if it does not contain
     * any complex structure, this method has no effect.
     * @param toRemove The element to dereference from the current element.
     * @return True if we've found and deleted the given element from current 
     * object contained structures. False otherwise.
     */
    boolean removeChild(final Element toRemove);
    
    /**
     * Add an element as child of the current one.
     * @param toAdd The element to be referenced as a child of the current one.
     * @return True if we succeed at referencing given element as a child, false
     * otherwise.
     */
    public boolean addChild(final Element toAdd);
    
    /**
     * Manage the author of an element. This piece of information is used for 
     * validation of documents created by external members.
     * 
     * @return Identifier of this object author.
     */
    String getAuthor();
    StringProperty authorProperty();
    void setAuthor(String author);
    
    /**
     * Manage the validity of an element.
     * @return True if this object is validated, false if it should be validated by an administrator.
     */
    boolean getValid();
    BooleanProperty validProperty();
    void setValid(boolean valid);
    
    /**
     * Manage the designation of an element. To handle with couchDB ids is not 
     * an easy task, then elements can be given an ID, without guarantee of 
     * unicity.
     * 
     * @return A code for the given object.
     */
    String getDesignation();
    StringProperty designationProperty();
    void setDesignation(String designation);
    
    /**
     * Search in this object a contained {@link Element} with the given ID. If 
     * we cannot find an element (which means your element is not referenced here,
     * or just its ID is referenced) a null value is returned.
     * @param toSearch The identifier of the element to find.
     * @return The Element matching input ID, or null if we cannot find 
     * any in child structures.
     */
    Element getChildById(final String toSearch);
    
    /**
     * The implementation of equals() method is based on identifiers, 
     * considering two instances are equals if they describe the same entity.
     * 
     * On the contrary, this method explores the content of the object 
     * attributes in order to determine if their content is equal.
     * 
     * All the attributes are not examined, but only the single valued ones.
     * 
     * @param element The element to compare with this one.
     * @return True if the given element is equal to this one, false otherwise.
     */
    default boolean contentBasedEquals(final Element element) {
        return this.equals(element);
    }
}
