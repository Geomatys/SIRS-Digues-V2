
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class StructureAvecContacts  extends Structure  {


   
    
 
    //
    // References
    //
    private List<String> proprietairesIds;
     
    //
    // References
    //
    private String gestionnairesId;
     
    //
    // References
    //
    private String gardiensId;
     
    //
    // References
    //
    private List<String> conventionsIds;
    
  
  
  

  
  
    
    public List<String> getProprietairesIds(){
    	return this.proprietairesIds;
    }
    
    public void setProprietairesIds(List<String> proprietairesIds){
    	this.proprietairesIds = proprietairesIds;
    }
   
  
    
    public String getGestionnaires(){
    	return this.gestionnairesId;
    }
    
    public void setGestionnaires(String gestionnairesId){
    	this.gestionnairesId = gestionnairesId;
    }
   
  
    
    public String getGardiens(){
    	return this.gardiensId;
    }
    
    public void setGardiens(String gardiensId){
    	this.gardiensId = gardiensId;
    }
   
  
    
    public List<String> getConventionsIds(){
    	return this.conventionsIds;
    }
    
    public void setConventionsIds(List<String> conventionsIds){
    	this.conventionsIds = conventionsIds;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[StructureAvecContacts ");
      return builder.toString();
  }


}

