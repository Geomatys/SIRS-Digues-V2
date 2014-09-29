/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.digue.dto;

import java.util.Calendar;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DigueTry {
    
    private ObjectProperty<Long> idDigue;
    private StringProperty libelleDigue;
    private StringProperty commentaireDigue;
    private ObjectProperty<Calendar> dateDerniereMaj;
    
    public DigueTry(){
        this.idDigue = new SimpleObjectProperty<>();
        this.libelleDigue = new SimpleStringProperty();
        this.commentaireDigue = new SimpleStringProperty();
        this.dateDerniereMaj = new SimpleObjectProperty<>();
    }
    
    
    public ObjectProperty<Long> idDigueProperty(){return idDigue;}
    public StringProperty libelleDigueProperty(){return libelleDigue;}
    public StringProperty commentaireDigueProperty(){return commentaireDigue;}
    public ObjectProperty<Calendar> dateDerniereMajProperty(){return dateDerniereMaj;}
    
    public Long getIdDigue() {return idDigue.get();}
    public String getLibelleDigue(){return libelleDigue.get();}
    public String getCommentaireDigue(){return commentaireDigue.get();}
    public Calendar getDateDerniereMaj(){return dateDerniereMaj.get();}
    
    public void setIdDigue(Long idDigue){this.idDigue.set(idDigue);}
    public void setLibelleDigue(String libelleDigue){this.libelleDigue.set(libelleDigue);}
    public void setCommentaireDigue(String commentaireDigue){this.commentaireDigue.set(commentaireDigue);}
    public void setDateDerniereMaj(Calendar dateDerniereMaj){this.dateDerniereMaj.set(dateDerniereMaj);}
}
