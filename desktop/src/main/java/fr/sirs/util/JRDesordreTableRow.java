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
package fr.sirs.util;

import fr.sirs.core.model.RefUrgence;
import fr.sirs.util.property.Reference;
import java.time.LocalDate;

/**
 * Synthèse d'information d'observation de désordre spécifique aux réseaux hydrauliques fermés.
 * 
 * @author Samuel Andrés (Geomatys) <samuel.andres at geomatys.com>
 */
public class JRDesordreTableRow implements Comparable<JRDesordreTableRow> {
    
    private LocalDate observationDate;
    private String desordreDesignation;
    private String observationDesignation;
    private String observationUrgence;
    private String desordreDescription;
    private boolean desordreEnded; // Information sur le traitement du désordre, déduite de la présence d'une date de fin dans le cas d'un désordre traité (spécifié par Jordan Perrin dans la demande SYM-1613 du 18/05/2017 à 10:02). 
    
    public JRDesordreTableRow(final LocalDate observationDate, final String desordreDesignation, final String observationDesignation, 
            final String observationUrgence, final String desordreDescription, final boolean desordreEnded){
        this.observationDate = observationDate;
        this.desordreDesignation = desordreDesignation;
        this.observationDesignation = observationDesignation;
        this.observationUrgence = observationUrgence;
        this.desordreDescription = desordreDescription;
        this.desordreEnded = desordreEnded;
    }
    
    public void setDesordreDesignation(final String desordreDesignation){
        this.desordreDesignation = desordreDesignation;
    }
    
    public String getDesordreDesignation(){
        return desordreDesignation;
    }

    public LocalDate getObservationDate() {
        return observationDate;
    }

    public void setObservationDate(LocalDate observationDate) {
        this.observationDate = observationDate;
    }

    public String getObservationDesignation() {
        return observationDesignation;
    }

    public void setObservationDesignation(String observationDesignation) {
        this.observationDesignation = observationDesignation;
    }

    @Reference(ref = RefUrgence.class) // Annotation nécessaire pour pointer sur la référence SI on souhaite récupérer de l'information.
    public String getObservationUrgence() {
        return observationUrgence;
    }

    public void setObservationUrgence(String observationUrgence) {
        this.observationUrgence = observationUrgence;
    }

    public String getDesordreDescription() {
        return desordreDescription;
    }

    public void setDesordreDescription(String desordreDescription) {
        this.desordreDescription = desordreDescription;
    }
    
    public boolean getDesordreEnded(){
        return desordreEnded;
    }
    
    public void setDesordreEnded(boolean desordreEnded){
        this.desordreEnded = desordreEnded;
    }

    @Override
    public int compareTo(JRDesordreTableRow other) {
        final LocalDate date1 = this.getObservationDate();
        final LocalDate date2 = other.getObservationDate();
        if(date1==null && date2==null) return 0;
        else if(date1==null || date2==null) return (date1==null) ? 1 : -1;
        else return -date1.compareTo(date2); // Par date décroissante : la plus récente en tête.
    }
    
}
