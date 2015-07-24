package fr.sirs.core.model;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public enum PhotoChoiceDocument {
    AUCUNE, DERNIERE, TOUTE;
    
    public static PhotoChoiceDocument fromBox(String value) {
        switch(value) {
            case "Photos"            : return null;
            case "Pas de photos"     : return AUCUNE;
            case "Dernière photo"    : return DERNIERE;
            case "Toutes les photos" : return TOUTE;
        }
        return null;
    }
    
    public static String toBox(PhotoChoiceDocument value) {
        switch(value) {
            case AUCUNE   : return "Pas de photos";
            case DERNIERE : return "Dernière photo";
            case TOUTE    : return "Toutes les photos";
        }
        return null;
    }
}
