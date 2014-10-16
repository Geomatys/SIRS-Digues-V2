/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package types;

import org.apache.sis.util.UnsupportedImplementationException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public enum TypeRive {
    DROITE("droite"), GAUCHE("gauche"), INDETERMINE("indeterminé");
    private final String typeRive;
    private TypeRive(String typeRive){this.typeRive=typeRive;}
    @Override
    public String toString(){return this.typeRive;}
    public TypeRive toTypeRive(final String typeRive){
        if(DROITE.toString().equals(typeRive)) return DROITE;
        else if(GAUCHE.toString().equals(typeRive)) return GAUCHE;
        else if(INDETERMINE.toString().equals(typeRive)) return INDETERMINE;
        else throw new UnsupportedImplementationException("Unknown TypeRive.");
    }
}
