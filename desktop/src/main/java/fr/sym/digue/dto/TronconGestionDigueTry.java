/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.digue.dto;

import java.util.Calendar;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TronconGestionDigueTry {
    
    private ObjectProperty<Long> idTronconGestion;
    private ObjectProperty<Long> idOrgGestionnaire;
    private ObjectProperty<Long> idDigue;
    private LongProperty idTypeRive;
    private ObjectProperty<Calendar> dateDebutValTroncon;
    private ObjectProperty<Calendar> dateFinValTroncon;
    private StringProperty nomTronconGestion;
    private StringProperty commentaireTroncon;
    private ObjectProperty<Calendar> dateDebutValGestionnaireD;
    private ObjectProperty<Calendar> dateFinValGestionnaireD;
    private ObjectProperty<Long> idSystemeRepDefaut;
    private StringProperty libelleTronconGestion;
    private ObjectProperty<Calendar> dateDerniereMaj;

    public TronconGestionDigueTry() {
        idTronconGestion = new SimpleObjectProperty<>();
        idOrgGestionnaire = new SimpleObjectProperty<>();
        idDigue = new SimpleObjectProperty<>();
        idTypeRive = new SimpleLongProperty();
        dateDebutValTroncon = new SimpleObjectProperty<>();
        dateFinValTroncon = new SimpleObjectProperty<>();
        nomTronconGestion = new SimpleStringProperty();
        commentaireTroncon = new SimpleStringProperty();
        dateDebutValGestionnaireD = new SimpleObjectProperty<>();
        dateFinValGestionnaireD = new SimpleObjectProperty<>();
        idSystemeRepDefaut = new SimpleObjectProperty<>();
        libelleTronconGestion = new SimpleStringProperty();
        dateDerniereMaj = new SimpleObjectProperty<>();
    }
    
    public ObjectProperty<Long> idTronconGestionProperty(){return idTronconGestion;}
    public ObjectProperty<Long> idOrgGestionnaireProperty(){return idOrgGestionnaire;}
    public ObjectProperty<Long> idDigueProperty(){return idDigue;}
    public LongProperty idTypeRiveProperty(){return idTypeRive;}
    public ObjectProperty<Calendar> dateDebutValTronconProperty(){return dateDebutValTroncon;}
    public ObjectProperty<Calendar> dateFinValTronconProperty(){return dateFinValTroncon;}
    public StringProperty nomTronconGestionProperty(){return nomTronconGestion;}
    public StringProperty commentaireTronconProperty(){return commentaireTroncon;}
    public ObjectProperty<Calendar> dateDebutValGestionnaireDProperty(){return dateDebutValGestionnaireD;}
    public ObjectProperty<Calendar> dateFinValGestionnaireDProperty(){return dateFinValGestionnaireD;}
    public ObjectProperty<Long> idSystemeRepDefautProperty(){return idSystemeRepDefaut;}
    public StringProperty libelleTronconGestionProperty(){return libelleTronconGestion;}
    public ObjectProperty<Calendar> dateDerniereMajProperty(){return dateDerniereMaj;}
    
    public Long getIdTronconGestion(){return idTronconGestion.get();}
    public Long getIdOrgGestionnaire(){return idOrgGestionnaire.get();}
    public Long getIdDigue(){return idDigue.get();}
    public long getIdTypeRive(){return idTypeRive.get();}
    public Calendar getDateDebutValTroncon(){return dateDebutValTroncon.get();}
    public Calendar getDateFinValTroncon(){return dateFinValTroncon.get();}
    public String getNomTronconGestion(){return nomTronconGestion.get();}
    public String getCommentaireTroncon(){return commentaireTroncon.get();}
    public Calendar getDateDebutValGestionnaireD(){return dateDebutValGestionnaireD.get();}
    public Calendar getDateFinValGestionnaireD(){return dateFinValGestionnaireD.get();}
    public Long getIdSystemeRepDefaut(){return idSystemeRepDefaut.get();}
    public String getLibelleTronconGestion(){return libelleTronconGestion.get();}
    public Calendar getDateDerniereMaj(){return dateDerniereMaj.get();}
    
    public void setIdTronconGestion(Long idTronconGestion){this.idTronconGestion.set(idTronconGestion);}
    public void setIdOrgGestionnaire(Long idOrgGestionnaire){this.idOrgGestionnaire.set(idOrgGestionnaire);}
    public void setIdDigue(Long idDigue){this.idDigue.set(idDigue);}
    public void setIdTypeRive(long idTypeRive){this.idTypeRive.set(idTypeRive);}
    public void setDateDebutValTroncon(Calendar dateDebutValTroncon){this.dateDebutValTroncon.set(dateDebutValTroncon);}
    public void setDateFinValTroncon(Calendar dateFinValTroncon){this.dateFinValTroncon.set(dateFinValTroncon);}
    public void setNomTronconGestion(String nomTronconGestion){this.nomTronconGestion.set(nomTronconGestion);}
    public void setCommentaireTroncon(String commentaireTroncon){this.commentaireTroncon.set(commentaireTroncon);}
    public void setDateDebutValGestionnaireD(Calendar dateDebutValGestionnaireD){this.dateDebutValGestionnaireD.set(dateDebutValGestionnaireD);}
    public void setDateFinValGestionnaireD(Calendar dateFinValGestionnaireD){this.dateFinValGestionnaireD.set(dateFinValGestionnaireD);}
    public void setIdSystemeRepDefaut(Long idSystemeRepDefaut){this.idSystemeRepDefaut.set(idSystemeRepDefaut);}
    public void setLibelleTronconGestion(String libelleTronconGestion){this.libelleTronconGestion.set(libelleTronconGestion);}
    public void setDateDerniereMaj(Calendar dateDerniereMaj){this.dateDerniereMaj.set(dateDerniereMaj);}
}
