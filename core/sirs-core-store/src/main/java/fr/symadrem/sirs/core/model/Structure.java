
package fr.symadrem.sirs.core.model;

import java.time.Instant;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class Structure  extends Positionable  {
    //
    // Attributes.
    //  
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
    * JavaFX property for cote.
    */
    private StringProperty  cote = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on cote.
    */
    public  StringProperty coteProperty() {
       return cote;
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
    * JavaFX property for source.
    */
    private StringProperty  source = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on source.
    */
    public  StringProperty sourceProperty() {
       return source;
    }
    //
    // References
    // 
    private String tronconId;
     
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
        
    public Instant getDate_debut(){
    	return this.date_debut.get();
    }
    
    public void setDate_debut(Instant date_debut){
    	this.date_debut.set(date_debut);
    }
        
    public Instant getDate_fin(){
    	return this.date_fin.get();
    }
    
    public void setDate_fin(Instant date_fin){
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

