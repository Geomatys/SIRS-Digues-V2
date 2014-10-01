
package fr.symadrem.sirs.core.model;

import com.geomatys.json.InstantDeserializer;
import com.geomatys.json.InstantSerializer;
import java.time.Instant;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Visite  extends Positionable  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for date.
    */
    private ObjectProperty<Instant>  date = new SimpleObjectProperty<Instant>();
    
    /**
    * Getter for JavaFX property on date.
    */
    public  ObjectProperty<Instant> dateProperty() {
       return date;
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
    //
    // References
    // 


    @JsonSerialize(using=InstantSerializer.class)    
    public Instant getDate(){
    	return this.date.get();
    }

    @JsonDeserialize(using=InstantDeserializer.class)    
    public void setDate(Instant date){
    	this.date.set(date);
    }    
    
    public String getCommentaire(){
    	return this.commentaire.get();
    }
    
    public void setCommentaire(String commentaire){
    	this.commentaire.set(commentaire);
    }    
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Visite ");
      builder.append("date: ");
      builder.append(date.get());
      builder.append(", ");
      builder.append("commentaire: ");
      builder.append(commentaire.get());
      return builder.toString();
  }


}

