
package fr.sirs.index;

import fr.sirs.core.SirsCore;
import fr.sirs.core.model.LabelMapper;
import java.util.logging.Level;
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
        return clazz;
    }

    public String getElementClassTitle() {
        try {
            return LabelMapper.get(getElementClass()).mapClassName();
        } catch (Exception e) {
            SirsCore.LOGGER.log(Level.FINE, "No valid type for found element.", e);
            return clazz == null || clazz.isEmpty()? "N/A" : clazz.substring(clazz.lastIndexOf('.')+1);
        }
    }

    public Class getElementClass() throws ClassNotFoundException {
        return Class.forName(clazz, true, Thread.currentThread().getContextClassLoader());
    }

}
