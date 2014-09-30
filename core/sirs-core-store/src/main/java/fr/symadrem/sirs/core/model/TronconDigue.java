
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class TronconDigue  extends Positionable  {


   
        
    private StringProperty  libelle = new SimpleStringProperty();
        
    private StringProperty  commentaire = new SimpleStringProperty();
        
    private ObjectProperty<java.util.Date>  date_debut = new SimpleObjectProperty<java.util.Date>();
        
    private ObjectProperty<java.util.Date>  date_fin = new SimpleObjectProperty<java.util.Date>();
        
    private ObjectProperty<java.util.Date>  date_maj = new SimpleObjectProperty<java.util.Date>();
        
    private StringProperty  rive = new SimpleStringProperty();
        
    private StringProperty  systeme_reperage_defaut = new SimpleStringProperty();
    
 
    public ObservableList<Structure>  stuctures =  FXCollections.observableArrayList() ; 
    //
    // References
    //
    private String digueId;
     
    //
    // References
    //
    private List<String> systeme_reperageIds;
     
    public ObjectProperty<SystemeReperage>  systeme_rep_defaut = new SimpleObjectProperty<>() ; 
    //
    // References
    //
    private String contactId;
     
    public ObservableList<BorneDigue>  bornes =  FXCollections.observableArrayList() ; 
    //
    // References
    //
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
        
    public java.util.Date getDate_debut(){
    	return this.date_debut.get();
    }
    
    public void setDate_debut(java.util.Date date_debut){
    	this.date_debut.set(date_debut);
    }
        
    public java.util.Date getDate_fin(){
    	return this.date_fin.get();
    }
    
    public void setDate_fin(java.util.Date date_fin){
    	this.date_fin.set(date_fin);
    }
        
    public java.util.Date getDate_maj(){
    	return this.date_maj.get();
    }
    
    public void setDate_maj(java.util.Date date_maj){
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

