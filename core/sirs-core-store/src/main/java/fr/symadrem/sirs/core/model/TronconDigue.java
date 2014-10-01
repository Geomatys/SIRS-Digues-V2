
package fr.symadrem.sirs.core.model;

import com.geomatys.json.InstantDeserializer;
import com.geomatys.json.InstantSerializer;
import java.time.Instant;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class TronconDigue  extends Positionable  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for libelle.
    */
    private StringProperty  libelle = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on libelle.
    */
    public  StringProperty libelleProperty() {
       return libelle;
    }
    /**
    * JavaFX property for commentaire.
    */
    private StringProperty  commentaire = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on commentaire.
    */
    public  StringProperty commentaireProperty() {
       return commentaire;
    }
    /**
    * JavaFX property for date_debut.
    */
    private ObjectProperty<Instant>  date_debut = new SimpleObjectProperty<Instant>();
    
    /**
    * Getter for JavaFX property on date_debut.
    */
    public  ObjectProperty<Instant> date_debutProperty() {
       return date_debut;
    }
    /**
    * JavaFX property for date_fin.
    */
    private ObjectProperty<Instant>  date_fin = new SimpleObjectProperty<Instant>();
    
    /**
    * Getter for JavaFX property on date_fin.
    */
    public  ObjectProperty<Instant> date_finProperty() {
       return date_fin;
    }
    /**
    * JavaFX property for date_maj.
    */
    private ObjectProperty<Instant>  date_maj = new SimpleObjectProperty<Instant>();
    
    /**
    * Getter for JavaFX property on date_maj.
    */
    public  ObjectProperty<Instant> date_majProperty() {
       return date_maj;
    }
    /**
    * JavaFX property for rive.
    */
    private StringProperty  rive = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on rive.
    */
    public  StringProperty riveProperty() {
       return rive;
    }
    /**
    * JavaFX property for systeme_reperage_defaut.
    */
    private StringProperty  systeme_reperage_defaut = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on systeme_reperage_defaut.
    */
    public  StringProperty systeme_reperage_defautProperty() {
       return systeme_reperage_defaut;
    }
    //
    // References
    // 
    public ObservableList<Structure>  stuctures =  FXCollections.observableArrayList() ; 
    private String digueId;
 
    private List<String> systeme_reperageIds;
 
    public ObjectProperty<SystemeReperage>  systeme_rep_defaut = new SimpleObjectProperty<>() ; 
    private String contactId;
 
    public ObservableList<BorneDigue>  bornes =  FXCollections.observableArrayList() ; 
    private String documentId;
 

    
    public String getLibelle(){
    	return this.libelle.get();
    }
    
    public void setLibelle(String libelle){
    	this.libelle.set(libelle);
    }    
    
    public String getCommentaire(){
    	return this.commentaire.get();
    }
    
    public void setCommentaire(String commentaire){
    	this.commentaire.set(commentaire);
    }    

    @JsonSerialize(using=InstantSerializer.class)    
    public Instant getDate_debut(){
    	return this.date_debut.get();
    }

    @JsonDeserialize(using=InstantDeserializer.class)    
    public void setDate_debut(Instant date_debut){
    	this.date_debut.set(date_debut);
    }    

    @JsonSerialize(using=InstantSerializer.class)    
    public Instant getDate_fin(){
    	return this.date_fin.get();
    }

    @JsonDeserialize(using=InstantDeserializer.class)    
    public void setDate_fin(Instant date_fin){
    	this.date_fin.set(date_fin);
    }    

    @JsonSerialize(using=InstantSerializer.class)    
    public Instant getDate_maj(){
    	return this.date_maj.get();
    }

    @JsonDeserialize(using=InstantDeserializer.class)    
    public void setDate_maj(Instant date_maj){
    	this.date_maj.set(date_maj);
    }    
    
    public String getRive(){
    	return this.rive.get();
    }
    
    public void setRive(String rive){
    	this.rive.set(rive);
    }    
    
    public String getSysteme_reperage_defaut(){
    	return this.systeme_reperage_defaut.get();
    }
    
    public void setSysteme_reperage_defaut(String systeme_reperage_defaut){
    	this.systeme_reperage_defaut.set(systeme_reperage_defaut);
    }     
   public List<Structure> getStuctures(){
    	return this.stuctures;
    }

    public void setStuctures(List<Structure> stuctures){
        this.stuctures.clear();
    	this.stuctures.addAll( stuctures );
    }
 

    
    public String getDigue(){
    	return this.digueId;
    }

    public void setDigue(String digueId){
    	this.digueId = digueId;
    }
 

    
    public List<String> getSysteme_reperageIds(){
    	return this.systeme_reperageIds;
    }

    public void setSysteme_reperageIds(List<String> systeme_reperageIds){
    	this.systeme_reperageIds = systeme_reperageIds;
    }
 
   public SystemeReperage getSysteme_rep_defaut(){
    	return this.systeme_rep_defaut.get();
    }

    public void setSysteme_rep_defaut(SystemeReperage systeme_rep_defaut){
    	this.systeme_rep_defaut.set( systeme_rep_defaut );
    }
 

    
    public String getContact(){
    	return this.contactId;
    }

    public void setContact(String contactId){
    	this.contactId = contactId;
    }
 
   public List<BorneDigue> getBornes(){
    	return this.bornes;
    }

    public void setBornes(List<BorneDigue> bornes){
        this.bornes.clear();
    	this.bornes.addAll( bornes );
    }
 

    
    public String getDocument(){
    	return this.documentId;
    }

    public void setDocument(String documentId){
    	this.documentId = documentId;
    }

  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[TronconDigue ");
      builder.append("libelle: ");
      builder.append(libelle.get());
      builder.append(", ");
      builder.append("commentaire: ");
      builder.append(commentaire.get());
      builder.append(", ");
      builder.append("date_debut: ");
      builder.append(date_debut.get());
      builder.append(", ");
      builder.append("date_fin: ");
      builder.append(date_fin.get());
      builder.append(", ");
      builder.append("date_maj: ");
      builder.append(date_maj.get());
      builder.append(", ");
      builder.append("rive: ");
      builder.append(rive.get());
      builder.append(", ");
      builder.append("systeme_reperage_defaut: ");
      builder.append(systeme_reperage_defaut.get());
      return builder.toString();
  }


}

