
package fr.sirs.core.model;

import fr.sirs.plugin.document.ui.ModelParagraphePane;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public enum SectionTypeDocument {
    TABLE,
    FICHE,
    DOCUMENT;
    
    public static String toBox(SectionTypeDocument value) {
        switch(value) {
            case TABLE     : return ModelParagraphePane.GENERATE_TAB;
            case FICHE     : return ModelParagraphePane.GENERATE_SHEET;
            case DOCUMENT  : return ModelParagraphePane.SELECT_DOC;
        }
        return null;
    }
}
