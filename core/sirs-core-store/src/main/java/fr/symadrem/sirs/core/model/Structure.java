
package fr.symadrem.sirs.core.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class Structure  extends CouchDbDocument  {


        
    public StringProperty  name = new SimpleStringProperty();
        
    public StringProperty  designation = new SimpleStringProperty();
    
 
    //
    // References
    //
    private String tronconId;
    
  
  
      
    public String getName(){
    	return this.name.get();
    }
    
    public void setName(String name){
    	this.name.set(name);
    }
        
    public String getDesignation(){
    	return this.designation.get();
    }
    
    public void setDesignation(String designation){
    	this.designation.set(designation);
    }
    

  
  
    
    public String getTroncon(){
    	return this.tronconId;
    }
    
    public void setTroncon(String tronconId){
    	this.tronconId = tronconId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Structure ");
      builder.append("name: ");
      builder.append(name.get());
      builder.append(", ");
      builder.append("designation: ");
      builder.append(designation.get());
      return builder.toString();
  }


}

