
package fr.symadrem.sirs.core.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class Structure  extends Positionable  {


   
        
    public StringProperty  commentaire = new SimpleStringProperty();
        
    public StringProperty  cote = new SimpleStringProperty();
        
    public ObjectProperty<java.util.Date>  date_debut = new SimpleObjectProperty<java.util.Date>();
        
    public ObjectProperty<java.util.Date>  date_fin = new SimpleObjectProperty<java.util.Date>();
        
    public StringProperty  source = new SimpleStringProperty();
    
 
    //
    // References
    //
    private String tronconId;
     
    //
    // References
    //
    private String documentId;
    
  
  
      
    public String getCommentaire(){
    	return this.commentaire.get();
    }
    
    public void setCommentaire(String commentaire){
    	this.commentaire.set(commentaire);
    }
        
    public String getCote(){
    	return this.cote.get();
    }
    
    public void setCote(String cote){
    	this.cote.set(cote);
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
        
    public String getSource(){
    	return this.source.get();
    }
    
    public void setSource(String source){
    	this.source.set(source);
    }
    

  
  
    
    public String getTroncon(){
    	return this.tronconId;
    }
    
    public void setTroncon(String tronconId){
    	this.tronconId = tronconId;
    }
   
  
    
    public String getDocument(){
    	return this.documentId;
    }
    
    public void setDocument(String documentId){
    	this.documentId = documentId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Structure ");
      builder.append("commentaire: ");
      builder.append(commentaire.get());
      builder.append(", ");
      builder.append("cote: ");
      builder.append(cote.get());
      builder.append(", ");
      builder.append("date_debut: ");
      builder.append(date_debut.get());
      builder.append(", ");
      builder.append("date_fin: ");
      builder.append(date_fin.get());
      builder.append(", ");
      builder.append("source: ");
      builder.append(source.get());
      return builder.toString();
  }


}

