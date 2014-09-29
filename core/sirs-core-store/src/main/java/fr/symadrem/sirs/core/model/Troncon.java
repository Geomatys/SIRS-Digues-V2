
package fr.symadrem.sirs.core.model;

import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
@SuppressWarnings("serial")
public class Troncon  extends Positionable  {


    private static final String type = "Troncon";
    
    public String getType() {
        return type;
    }
    
    void setType(String type){
    }
    
        
    public StringProperty  name = new SimpleStringProperty();
        
    public StringProperty  designation = new SimpleStringProperty();
    
    
    private Set<Structure>  stuctures; 
    //
    // References
    //
    private String digueId;
    
  
  
      
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
    

  
   public Set<Structure> getStuctures(){
    	return this.stuctures;
    }
    
    public void setStuctures(Set<Structure> stuctures){
    	this.stuctures = stuctures;
    }
   
  
    
    public String getDigue(){
    	return this.digueId;
    }
    
    public void setDigue(String digueId){
    	this.digueId = digueId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Troncon ");
      builder.append("name: ");
      builder.append(name.get());
      builder.append(", ");
      builder.append("designation: ");
      builder.append(designation.get());
      return builder.toString();
  }


}

