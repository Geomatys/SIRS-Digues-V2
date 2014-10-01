
package fr.symadrem.sirs.core.model;

import java.util.Date;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Meteo  extends CouchDbDocument  {
    //
    // Attributes.
    //      
    public ObjectProperty<Date>  data_debut = new SimpleObjectProperty<Date>();
        
    public ObjectProperty<Date>  data_fin = new SimpleObjectProperty<Date>();
        
    public FloatProperty  vitesse_vent = new SimpleFloatProperty();
        
    public StringProperty  orientation_vent = new SimpleStringProperty();
        
    public FloatProperty  pression = new SimpleFloatProperty();
    
    //
    // References
    //
      
    public Date getData_debut(){
    	return this.data_debut.get();
    }
    
    public void setData_debut(Date data_debut){
    	this.data_debut.set(data_debut);
    }
        
    public Date getData_fin(){
    	return this.data_fin.get();
    }
    
    public void setData_fin(Date data_fin){
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

