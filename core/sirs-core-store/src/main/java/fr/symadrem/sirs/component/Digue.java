/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.symadrem.sirs.component;

import java.util.Date;
import org.ektorp.support.CouchDbDocument;
import org.ektorp.support.TypeDiscriminator;

/**
 *
 * @author samuel
 */
public class Digue extends CouchDbDocument {
     
    @TypeDiscriminator
    private String digueId;
    private String libelle;
    private String commentaire;
    private Date dateMaj;
    
    public String getDigueId() {
        return digueId;
    }

    public void setDigueId(String digueId) {
        this.digueId = digueId;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    /**
     * @return the dateMaj
     */
    public Date getDateMaj() {
        return dateMaj;
    }

    /**
     * @param dateMaj the dateMaj to set
     */
    public void setDateMaj(Date dateMaj) {
        this.dateMaj = dateMaj;
    }
    
    
}
