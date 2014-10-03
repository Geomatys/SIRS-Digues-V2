
package fr.symadrem.sirs.core.model;

import com.geomatys.json.LocalDateTimeDeserializer;
import com.geomatys.json.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class MesureMonteeEaux  extends CouchDbDocument  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for date.
    */
    private ObjectProperty<LocalDateTime>  date = new SimpleObjectProperty<LocalDateTime>();
    
    /**
    * Getter for JavaFX property on date.
    */
    public  ObjectProperty<LocalDateTime> dateProperty() {
       return date;
    }
    /**
    * JavaFX property for reference_hauteur.
    */
    private StringProperty  reference_hauteur = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on reference_hauteur.
    */
    public  StringProperty reference_hauteurProperty() {
       return reference_hauteur;
    }
    /**
    * JavaFX property for hauteur.
    */
    private FloatProperty  hauteur = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on hauteur.
    */
    public  FloatProperty hauteurProperty() {
       return hauteur;
    }
    /**
    * JavaFX property for debit_max.
    */
    private FloatProperty  debit_max = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on debit_max.
    */
    public  FloatProperty debit_maxProperty() {
       return debit_max;
    }
    //
    // References
    // 
    private StringProperty observateurs = new SimpleStringProperty();
 


    @JsonSerialize(using=LocalDateTimeSerializer.class)    
    public LocalDateTime getDate(){
    	return this.date.get();
    }

    @JsonDeserialize(using=LocalDateTimeDeserializer.class)    
    public void setDate(LocalDateTime date){
    	this.date.set(date);
    }    
    
    public String getReference_hauteur(){
    	return this.reference_hauteur.get();
    }
    
    public void setReference_hauteur(String reference_hauteur){
    	this.reference_hauteur.set(reference_hauteur);
    }    
    
    public float getHauteur(){
    	return this.hauteur.get();
    }
    
    public void setHauteur(float hauteur){
    	this.hauteur.set(hauteur);
    }    
    
    public float getDebit_max(){
    	return this.debit_max.get();
    }
    
    public void setDebit_max(float debit_max){
    	this.debit_max.set(debit_max);
    }     

    
    public String getObservateurs(){
    	return this.observateurs.get();
    }

    public void setObservateurs(String observateurs){
    	this.observateurs.set( observateurs );
    }

  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[MesureMonteeEaux ");
      builder.append("date: ");
      builder.append(date.get());
      builder.append(", ");
      builder.append("reference_hauteur: ");
      builder.append(reference_hauteur.get());
      builder.append(", ");
      builder.append("hauteur: ");
      builder.append(hauteur.get());
      builder.append(", ");
      builder.append("debit_max: ");
      builder.append(debit_max.get());
      return builder.toString();
  }


}

