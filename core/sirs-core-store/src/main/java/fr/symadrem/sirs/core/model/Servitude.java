
package fr.symadrem.sirs.core.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Servitude  extends CouchDbDocument  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for type_servitude.
    */
    private StringProperty  type_servitude = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on type_servitude.
    */
    public  StringProperty type_servitudeProperty() {
       return type_servitude;
    }
    /**
    * JavaFX property for parcelle.
    */
    private StringProperty  parcelle = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on parcelle.
    */
    public  StringProperty parcelleProperty() {
       return parcelle;
    }
    //
    // References
    //
      
    public String getType_servitude(){
    	return this.type_servitude.get();
    }
    
    public void setType_servitude(String type_servitude){
    	this.type_servitude.set(type_servitude);
    }
        
    public String getParcelle(){
    	return this.parcelle.get();
    }
    
    public void setParcelle(String parcelle){
    	this.parcelle.set(parcelle);
    }
    

 
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Servitude ");
      builder.append("type_servitude: ");
      builder.append(type_servitude.get());
      builder.append(", ");
      builder.append("parcelle: ");
      builder.append(parcelle.get());
      return builder.toString();
  }


}

