
package fr.sirs.index;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class ElementHit {
    private String elementId = "";
    private String clazz = "";
    private String libelle = "";

    public ElementHit(SearchHit hit) {
        elementId = hit.getId();
        final SearchHitField fieldClass = hit.field("@class");
        final SearchHitField fieldLibelle = hit.field("libelle");
        if (fieldClass != null) {
            clazz = fieldClass.getValue();
        }
        if (fieldLibelle != null) {
            libelle = fieldLibelle.getValue();
        }
    }

    public String getDocumentId() {
        return elementId;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getElementClassName() {
        final String[] parts = clazz.split("\\.");
        return parts[parts.length-1];
    }

    public Class geteElementClass() throws ClassNotFoundException {
        return Class.forName(clazz, true, Thread.currentThread().getContextClassLoader());
    }
    
}
