
package fr.symadrem.sirs.core.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class BorneDigue  extends Positionable  {


   
        
    public StringProperty  commentaire = new SimpleStringProperty();
        
    public ObjectProperty<java.util.Date>  date_debut = new SimpleObjectProperty<java.util.Date>();
        
    public ObjectProperty<java.util.Date>  date_fin = new SimpleObjectProperty<java.util.Date>();
        
    public BooleanProperty  fictive = new SimpleBooleanProperty();
        
    public StringProperty  nom = new SimpleStringProperty();
    

  
  
      
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
        
    public boolean getFictive(){
    	return this.fictive.get();
    }
    
    public void setFictive(boolean fictive){
    	this.fictive.set(fictive);
    }
        
    public String getNom(){
    	return this.nom.get();
    }
    
    public void setNom(String nom){
    	this.nom.set(nom);
    }
    

 
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[BorneDigue ");
      builder.append("commentaire: ");
      builder.append(commentaire.get());
      builder.append(", ");
      builder.append("date_debut: ");
      builder.append(date_debut.get());
      builder.append(", ");
      builder.append("date_fin: ");
      builder.append(date_fin.get());
      builder.append(", ");
      builder.append("fictive: ");
      builder.append(fictive.get());
      builder.append(", ");
      builder.append("nom: ");
      builder.append(nom.get());
      return builder.toString();
  }


}

