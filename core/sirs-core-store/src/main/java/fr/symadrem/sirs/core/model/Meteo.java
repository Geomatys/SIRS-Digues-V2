
package fr.symadrem.sirs.core.model;

import com.geomatys.json.InstantDeserializer;
import com.geomatys.json.InstantSerializer;
import java.time.Instant;
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
public class Meteo  extends CouchDbDocument  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for data_debut.
    */
    private ObjectProperty<Instant>  data_debut = new SimpleObjectProperty<Instant>();
    
    /**
    * Getter for JavaFX property on data_debut.
    */
    public  ObjectProperty<Instant> data_debutProperty() {
       return data_debut;
    }
    /**
    * JavaFX property for data_fin.
    */
    private ObjectProperty<Instant>  data_fin = new SimpleObjectProperty<Instant>();
    
    /**
    * Getter for JavaFX property on data_fin.
    */
    public  ObjectProperty<Instant> data_finProperty() {
       return data_fin;
    }
    /**
    * JavaFX property for vitesse_vent.
    */
    private FloatProperty  vitesse_vent = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on vitesse_vent.
    */
    public  FloatProperty vitesse_ventProperty() {
       return vitesse_vent;
    }
    /**
    * JavaFX property for orientation_vent.
    */
    private StringProperty  orientation_vent = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on orientation_vent.
    */
    public  StringProperty orientation_ventProperty() {
       return orientation_vent;
    }
    /**
    * JavaFX property for pression.
    */
    private FloatProperty  pression = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on pression.
    */
    public  FloatProperty pressionProperty() {
       return pression;
    }
    //
    // References
    // 


    @JsonSerialize(using=InstantSerializer.class)    
    public Instant getData_debut(){
    	return this.data_debut.get();
    }

    @JsonDeserialize(using=InstantDeserializer.class)    
    public void setData_debut(Instant data_debut){
    	this.data_debut.set(data_debut);
    }    

    @JsonSerialize(using=InstantSerializer.class)    
    public Instant getData_fin(){
    	return this.data_fin.get();
    }

    @JsonDeserialize(using=InstantDeserializer.class)    
    public void setData_fin(Instant data_fin){
    	this.data_fin.set(data_fin);
    }    
    
    public float getVitesse_vent(){
    	return this.vitesse_vent.get();
    }
    
    public void setVitesse_vent(float vitesse_vent){
    	this.vitesse_vent.set(vitesse_vent);
    }    
    
    public String getOrientation_vent(){
    	return this.orientation_vent.get();
    }
    
    public void setOrientation_vent(String orientation_vent){
    	this.orientation_vent.set(orientation_vent);
    }    
    
    public float getPression(){
    	return this.pression.get();
    }
    
    public void setPression(float pression){
    	this.pression.set(pression);
    }    
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Meteo ");
      builder.append("data_debut: ");
      builder.append(data_debut.get());
      builder.append(", ");
      builder.append("data_fin: ");
      builder.append(data_fin.get());
      builder.append(", ");
      builder.append("vitesse_vent: ");
      builder.append(vitesse_vent.get());
      builder.append(", ");
      builder.append("orientation_vent: ");
      builder.append(orientation_vent.get());
      builder.append(", ");
      builder.append("pression: ");
      builder.append(pression.get());
      return builder.toString();
  }


}

