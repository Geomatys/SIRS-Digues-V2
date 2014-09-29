
package fr.symadrem.sirs.core.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
@SuppressWarnings("serial")
public class Ecluse  extends Structure  {


    private static final String type = "Ecluse";
    
    public String getType() {
        return type;
    }
    
    void setType(String type){
    }
    
        
    public StringProperty  label = new SimpleStringProperty();
    

  
  
      
    public String getLabel(){
    	return this.label.get();
    }
    
    public void setLabel(String label){
    	this.label.set(label);
    }
    

 
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Ecluse ");
      builder.append("label: ");
      builder.append(label.get());
      return builder.toString();
  }


}

