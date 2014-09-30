
package fr.symadrem.sirs.core.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class PiedDigue  extends Structure  {


   
        
    public StringProperty  fonction = new SimpleStringProperty();
        
    public StringProperty  materiau = new SimpleStringProperty();
    

  
  
      
    public String getFonction(){
    	return this.fonction.get();
    }
    
    public void setFonction(String fonction){
    	this.fonction.set(fonction);
    }
        
    public String getMateriau(){
    	return this.materiau.get();
    }
    
    public void setMateriau(String materiau){
    	this.materiau.set(materiau);
    }
    

 
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[PiedDigue ");
      builder.append("fonction: ");
      builder.append(fonction.get());
      builder.append(", ");
      builder.append("materiau: ");
      builder.append(materiau.get());
      return builder.toString();
  }


}

