
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
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
public class Document  extends Positionable  {


   
        
    private StringProperty  nom = new SimpleStringProperty();
        
    private FloatProperty  taille = new SimpleFloatProperty();
        
    private StringProperty  description = new SimpleStringProperty();
        
    private StringProperty  type = new SimpleStringProperty();
        
    private StringProperty  chemin = new SimpleStringProperty();
        
    private StringProperty  contenu = new SimpleStringProperty();
        
    private ObjectProperty<java.util.Date>  date_document = new SimpleObjectProperty<java.util.Date>();
    
 
    //
    // References
    //
    private String digueId;
     
    //
    // References
    //
    private String troncon_digueId;
     
    //
    // References
    //
    private String structureId;
     
    //
    // References
    //
    private List<String> maitre_oeuvreIds;
     
    //
    // References
    //
    private List<String> financeurIds;
    
  
  
      
    public String getNom(){
    	return this.nom.get();
    }
    
    public void setNom(String nom){
    	this.nom.set(nom);
    }
        
    public float getTaille(){
    	return this.taille.get();
    }
    
    public void setTaille(float taille){
    	this.taille.set(taille);
    }
        
    public String getDescription(){
    	return this.description.get();
    }
    
    public void setDescription(String description){
    	this.description.set(description);
    }
        
    public String getType(){
    	return this.type.get();
    }
    
    public void setType(String type){
    	this.type.set(type);
    }
        
    public String getChemin(){
    	return this.chemin.get();
    }
    
    public void setChemin(String chemin){
    	this.chemin.set(chemin);
    }
        
    public String getContenu(){
    	return this.contenu.get();
    }
    
    public void setContenu(String contenu){
    	this.contenu.set(contenu);
    }
        
    public java.util.Date getDate_document(){
    	return this.date_document.get();
    }
    
    public void setDate_document(java.util.Date date_document){
    	this.date_document.set(date_document);
    }
    

  
  
    
    public String getDigue(){
    	return this.digueId;
    }
    
    public void setDigue(String digueId){
    	this.digueId = digueId;
    }
   
  
    
    public String getTroncon_digue(){
    	return this.troncon_digueId;
    }
    
    public void setTroncon_digue(String troncon_digueId){
    	this.troncon_digueId = troncon_digueId;
    }
   
  
    
    public String getStructure(){
    	return this.structureId;
    }
    
    public void setStructure(String structureId){
    	this.structureId = structureId;
    }
   
  
    
    public List<String> getMaitre_oeuvreIds(){
    	return this.maitre_oeuvreIds;
    }
    
    public void setMaitre_oeuvreIds(List<String> maitre_oeuvreIds){
    	this.maitre_oeuvreIds = maitre_oeuvreIds;
    }
   
  
    
    public List<String> getFinanceurIds(){
    	return this.financeurIds;
    }
    
    public void setFinanceurIds(List<String> financeurIds){
    	this.financeurIds = financeurIds;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Document ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("taille: ");
      builder.append(taille.get());
      builder.append(", ");
      builder.append("description: ");
      builder.append(description.get());
      builder.append(", ");
      builder.append("type: ");
      builder.append(type.get());
      builder.append(", ");
      builder.append("chemin: ");
      builder.append(chemin.get());
      builder.append(", ");
      builder.append("contenu: ");
      builder.append(contenu.get());
      builder.append(", ");
      builder.append("date_document: ");
      builder.append(date_document.get());
      return builder.toString();
  }


}

