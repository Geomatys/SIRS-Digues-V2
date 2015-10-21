
package fr.sirs;

import javafx.beans.property.ObjectProperty;

/**
 * L'interface Printable permet de savoir si un objet a des capacités d'impressions.
 *
 *
 * @author Johann Sorel (Geomatys)
 */
public interface Printable {

    /**
     * Un titre tres court placé dans le bouton d'impression.
     * 
     * @return titre ou null
     */
    default String getPrintTitle(){
        return null;
    }

    /**
     * Demander à l'objet de s'imprimer.
     * L'objet est responsable de toutes les opérations.
     * Si la valeur retourné est 'false' alors la methode {@link #getPrintableElements() }
     *
     * @return true si l'objet c'est imprimé.
     */
    default boolean print() {
        return false;
    }


    /**
     * Recuperer la liste des element pouvant etre imprimés.
     * Type possible :
     * - Element
     * - Feature
     * - List<Feature>
     * - FeatureCollection
     *
     * @return
     */
    ObjectProperty getPrintableElements();

}
