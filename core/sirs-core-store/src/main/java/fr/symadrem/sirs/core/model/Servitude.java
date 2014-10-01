
package fr.symadrem.sirs.core.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Servitude  extends CouchDbDocument  {
    //
    // Attributes.
    //      
    public StringProperty  type_servitude = new SimpleStringProperty();
        
    public StringProperty  parcelle = new SimpleStringProperty();
    
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

