/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
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
