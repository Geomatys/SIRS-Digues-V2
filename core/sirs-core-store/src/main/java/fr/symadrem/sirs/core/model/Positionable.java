
package fr.symadrem.sirs.core.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
public class Positionable  extends CouchDbDocument  {


    private static final String type = "Positionable";
    
    public String getType() {
        return type;
    }
    
    void setType(String type){
    }
    
        
    public IntegerProperty  landmarkBegin = new SimpleIntegerProperty();
    

  
  
      
    public int getLandmarkBegin(){
    	return this.landmarkBegin.get();
    }
    
    public void setLandmarkBegin(int landmarkBegin){
    	this.landmarkBegin.set(landmarkBegin);
    }
    

 
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Positionable ");
      builder.append("landmarkBegin: ");
      builder.append(landmarkBegin.get());
      return builder.toString();
  }


}

