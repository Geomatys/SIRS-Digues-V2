/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.symadrem.sirs.core.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.sis.util.UnsupportedImplementationException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public enum TypeRive {
    DROITE("Droite"), GAUCHE("Gauche"), INDEFINI("Indéfini");
    private final String typeRive;
    private TypeRive(String typeRive){this.typeRive=typeRive;}
    
    @Override
    public String toString(){return this.typeRive;}
    
    public static TypeRive toTypeRive(final String typeRive){
        if(DROITE.toString().equals(typeRive)) return DROITE;
        else if(GAUCHE.toString().equals(typeRive)) return GAUCHE;
        else if(INDEFINI.toString().equals(typeRive)) return INDEFINI;
        else throw new UnsupportedImplementationException("Unknown TypeRive.");
    }
    
    public static List<TypeRive> getTypes(){
        final List<TypeRive> types = new ArrayList<>();
        types.add(DROITE); types.add(GAUCHE); types.add(INDEFINI);
        return types;
    }
}
