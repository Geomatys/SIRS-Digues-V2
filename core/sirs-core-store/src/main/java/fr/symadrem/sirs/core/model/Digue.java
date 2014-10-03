
package fr.symadrem.sirs.core.model;

import com.geomatys.json.LocalDateTimeDeserializer;
import com.geomatys.json.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Digue  extends CouchDbDocument  {
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
    * JavaFX property for date_maj.
    */
    private ObjectProperty<LocalDateTime>  date_maj = new SimpleObjectProperty<LocalDateTime>();
    
    /**
    * Getter for JavaFX property on date_maj.
    */
    public  ObjectProperty<LocalDateTime> date_majProperty() {
       return date_maj;
    }
    /**
    * JavaFX property for classement.
    */
    private StringProperty  classement = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on classement.
    */
    public  StringProperty classementProperty() {
       return classement;
    }
    //
    // References
    // 

    
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

    @JsonSerialize(using=LocalDateTimeSerializer.class)    
    public LocalDateTime getDate_maj(){
    	return this.date_maj.get();
    }

    @JsonDeserialize(using=LocalDateTimeDeserializer.class)    
    public void setDate_maj(LocalDateTime date_maj){
    	this.date_maj.set(date_maj);
    }    
    
    public String getClassement(){
    	return this.classement.get();
    }
    
    public void setClassement(String classement){
    	this.classement.set(classement);
    }    
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Digue ");
      builder.append("libelle: ");
      builder.append(libelle.get());
      builder.append(", ");
      builder.append("commentaire: ");
      builder.append(commentaire.get());
      builder.append(", ");
      builder.append("date_maj: ");
      builder.append(date_maj.get());
      builder.append(", ");
      builder.append("classement: ");
      builder.append(classement.get());
      return builder.toString();
  }


}

