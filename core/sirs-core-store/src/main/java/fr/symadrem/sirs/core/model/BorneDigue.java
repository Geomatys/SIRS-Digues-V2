
package fr.symadrem.sirs.core.model;

import com.geomatys.json.InstantDeserializer;
import com.geomatys.json.InstantSerializer;
import com.vividsolutions.jts.geom.Point;
import java.time.Instant;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class BorneDigue  extends Positionable  {
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
    * JavaFX property for fictive.
    */
    private BooleanProperty  fictive = new SimpleBooleanProperty();
    
    /**
    * Getter for JavaFX property on fictive.
    */
    public  BooleanProperty fictiveProperty() {
       return fictive;
    }
    /**
    * JavaFX property for nom.
    */
    private StringProperty  nom = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on nom.
    */
    public  StringProperty nomProperty() {
       return nom;
    }
    /**
    * JavaFX property for positionBorne.
    */
    private ObjectProperty<Point>  positionBorne = new SimpleObjectProperty<Point>();
    
    /**
    * Getter for JavaFX property on positionBorne.
    */
    public  ObjectProperty<Point> positionBorneProperty() {
       return positionBorne;
    }
    //
    // References
    // 

    
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
    
    public Point getPositionBorne(){
    	return this.positionBorne.get();
    }
    
    public void setPositionBorne(Point positionBorne){
    	this.positionBorne.set(positionBorne);
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
      builder.append(", ");
      builder.append("positionBorne: ");
      builder.append(positionBorne.get());
      return builder.toString();
  }


}

