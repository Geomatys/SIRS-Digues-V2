/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.symadrem.sirs.component;

import org.ektorp.support.CouchDbDocument;
import org.ektorp.support.TypeDiscriminator;

/**
 *
 * @author samuel
 */
public class Digue extends CouchDbDocument {
     
    @TypeDiscriminator
    private String digueId;

    
    private int longueur;

    public int getLongueur() {
        return longueur;
    }
    public String getDigueId() {
        return digueId;
    }

    public void setDigueId(String digueId) {
        this.digueId = digueId;
    }

    public void setLongueur(int longueur) {
        this.longueur = longueur;
    }

    public String getCommune() {
        return commune;
    }

    public void setCommune(String commune) {
        this.commune = commune;
    }
    private String commune;
    
    
}
