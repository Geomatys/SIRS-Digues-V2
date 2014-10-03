
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
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Convention  extends Document  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for type_conventions.
    */
    private StringProperty  type_conventions = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on type_conventions.
    */
    public  StringProperty type_conventionsProperty() {
       return type_conventions;
    }
    /**
    * JavaFX property for reference_papier.
    */
    private StringProperty  reference_papier = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on reference_papier.
    */
    public  StringProperty reference_papierProperty() {
       return reference_papier;
    }
    /**
    * JavaFX property for reference_numerique.
    */
    private StringProperty  reference_numerique = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on reference_numerique.
    */
    public  StringProperty reference_numeriqueProperty() {
       return reference_numerique;
    }
    /**
    * JavaFX property for date_debut.
    */
    private ObjectProperty<LocalDateTime>  date_debut = new SimpleObjectProperty<LocalDateTime>();
    
    /**
    * Getter for JavaFX property on date_debut.
    */
    public  ObjectProperty<LocalDateTime> date_debutProperty() {
       return date_debut;
    }
    /**
    * JavaFX property for date_fin.
    */
    private ObjectProperty<LocalDateTime>  date_fin = new SimpleObjectProperty<LocalDateTime>();
    
    /**
    * Getter for JavaFX property on date_fin.
    */
    public  ObjectProperty<LocalDateTime> date_finProperty() {
       return date_fin;
    }
    //
    // References
    // 

    
    public String getType_conventions(){
    	return this.type_conventions.get();
    }
    
    public void setType_conventions(String type_conventions){
    	this.type_conventions.set(type_conventions);
    }    
    
    public String getReference_papier(){
    	return this.reference_papier.get();
    }
    
    public void setReference_papier(String reference_papier){
    	this.reference_papier.set(reference_papier);
    }    
    
    public String getReference_numerique(){
    	return this.reference_numerique.get();
    }
    
    public void setReference_numerique(String reference_numerique){
    	this.reference_numerique.set(reference_numerique);
    }    

    @JsonSerialize(using=LocalDateTimeSerializer.class)    
    public LocalDateTime getDate_debut(){
    	return this.date_debut.get();
    }

    @JsonDeserialize(using=LocalDateTimeDeserializer.class)    
    public void setDate_debut(LocalDateTime date_debut){
    	this.date_debut.set(date_debut);
    }    

    @JsonSerialize(using=LocalDateTimeSerializer.class)    
    public LocalDateTime getDate_fin(){
    	return this.date_fin.get();
    }

    @JsonDeserialize(using=LocalDateTimeDeserializer.class)    
    public void setDate_fin(LocalDateTime date_fin){
    	this.date_fin.set(date_fin);
    }    
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Convention ");
      builder.append("type_conventions: ");
      builder.append(type_conventions.get());
      builder.append(", ");
      builder.append("reference_papier: ");
      builder.append(reference_papier.get());
      builder.append(", ");
      builder.append("reference_numerique: ");
      builder.append(reference_numerique.get());
      builder.append(", ");
      builder.append("date_debut: ");
      builder.append(date_debut.get());
      builder.append(", ");
      builder.append("date_fin: ");
      builder.append(date_fin.get());
      return builder.toString();
  }


}

