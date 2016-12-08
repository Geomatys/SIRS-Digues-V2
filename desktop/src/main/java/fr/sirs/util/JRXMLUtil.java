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

import static fr.sirs.util.AbstractJDomWriter.NULL_REPLACEMENT;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Classe utilitaire réservée aux méthodes utilisées depuis les fichiers JRXML.
 * 
 * Afin de ne pas modifier involontairement le résultat des fiches imprimées avec Jasperreport, il est préférable de
 * ne pas utiliser ces méthodes dans du code Java.
 * 
 * Attention toutefois : ces méthodes, dont les signatures sont écrites en dur dans les fichiers JRXML, sont susceptibles
 * d'une double utilisation.
 * 
 * 1- Elles peuvent être utilisées de manière statique dans les fichiers JRXML écrits en dur.
 * 
 * 2- Elles peuvent également être utilisées indirectement (écrites en dur sous forme de chaînes de caractères) par les 
 * méthodes modifiant dynamiquement les patrons JRXML (telles que par exemple dans la classe 
 * {@link AbstractJDomWriterSingleSpecificSheet}). Ceci expose à un risque d'éparpillement d'écriture en dur des signatures
 * de es méthodes. Pour limiter le risque d'utilisation de ces noms de fonction à l'extérieur de cette classe, il est 
 * recommandé de passer par des méthodes spécifiques ({@link JRXMLUtil#dynamicDisplayLabel(java.lang.String)} , 
 * {@link JRXMLUtil#dynamicDisplayLabels(java.lang.String) }…).
 * 
 * @author Samuel Andrés (Geomatys) <samuel.andres at geomatys.com>
 */
public class JRXMLUtil {
    
    private JRXMLUtil(){}
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // MÉTHODES CŒUR DES FONCTIONNALITÉS.                                                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    static String extractDesignation(final String input){
        return input.substring(input.indexOf(SirsStringConverter.DESIGNATION_SEPARATOR)+3, input.indexOf(SirsStringConverter.LABEL_SEPARATOR));
    }
    
    static String extractLabel(final String input){
        return input.substring(input.indexOf(SirsStringConverter.LABEL_SEPARATOR)+3);
    }
    
    /**
     * Affichage d'un champ récupéré comme {@link PrintableArrayList}.
     * 
     * @param inputList
     * @param ordered
     * @param startIndex
     * @return 
     */
    static String extractLabels(final String inputList, final boolean ordered, final int startIndex){
        
        if(inputList==null || inputList.isEmpty()) return NULL_REPLACEMENT;
        
        final String[] split = inputList.split("\n");
        final Collection<String> output = new ArrayList<>();
        for(int i = 0; i<split.length; i++){
            output.add(extractLabel(split[i]));
        }
        return PrinterUtilities.printList(output, ordered, startIndex);
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // MÉTHODES DESTINÉES À UNE ÉCRITURE STATIQUE DANS LES FICHIERS JRXML.                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static String displayField(final String input){
        return input==null ? NULL_REPLACEMENT : input;
    }
    
    /**
     * 
     * @param input
     * @return
     * 
     * @see JRXMLUtil#dynamicDisplayLabel(java.lang.String) Pour écrire dynamiquement cette méthode dans les patrons JRXML.
     */
    public static String displayLabel(final String input){
        return input==null ? NULL_REPLACEMENT : extractLabel(input);
    }
    
    /**
     * Affichage d'un champ récupéré comme {@link PrintableArrayList}.
     * 
     * @param inputList
     * @param ordered
     * @param startIndex
     * @return
     * 
     * @see JRXMLUtil#dynamicDisplayLabels(java.lang.String) Pour écrire dynamiquement cette méthode dans les patrons JRXML.
     */
    public static String displayLabels(final String inputList, final Boolean ordered, final Integer startIndex){
        return inputList==null ? NULL_REPLACEMENT : extractLabels(inputList, ordered, startIndex);
    }
    
    public static String displayDesignation(final String input){
        return input==null ? NULL_REPLACEMENT : extractDesignation(input);
    }
    
    public static String displayAmontAval(final Boolean input){
        return input==null ? NULL_REPLACEMENT : input ? "Amont" : "Aval";
    }
    
    public static String displayDistance(final Double distance){
        return distance==null ? NULL_REPLACEMENT : (distance + " m");
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // MÉTHODES À UTILISER POUR ÉCRIRE DYNAMIQUEMENT DANS LES PATRONS JRXML AFIN DE CENTRALISER LES MODIFICATIONS LE  //
    // CAS ÉCHÉANT.                                                                                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Affichage d'un champ récupéré comme {@link PrintableArrayList}.
     * 
     * @param fieldName
     * @return 
     * @see JRXMLUtil#displayLabels(java.lang.String, java.lang.Boolean, java.lang.Integer) 
     */
    public static String dynamicDisplayLabels(final String fieldName){
        return "($F{"+fieldName+"}==null) ? \""+NULL_REPLACEMENT+"\" : fr.sirs.util.JRXMLUtil.displayLabels($F{"+fieldName+"}.toString(), true, 1)";
    }
    
    /**
     * 
     * @param fieldName
     * @return
     * @see JRXMLUtil#displayLabel(java.lang.String) 
     */
    public static String dynamicDisplayLabel(final String fieldName){
        return "fr.sirs.util.JRXMLUtil.displayLabel($F{"+fieldName+"})";
    }
}
