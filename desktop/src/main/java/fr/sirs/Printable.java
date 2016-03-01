
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
     * Un titre court placé dans le bouton d'impression.
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
     * peut être invoquée pour récupérer la liste des élements potentiellement imprimables
     *
     * @return true si l'objet c'est imprimé.
     */
    default boolean print() {
        return false;
    }


    /**
     * Recuperer la liste des element pouvant etre imprimés.
     * Type possible :
     * - Element ou liste d'éléments
     * - Feature ou collection de features
     *
     * @return Un objet ou une collection d'objets à imprimer.
     */
    ObjectProperty getPrintableElements();

}
