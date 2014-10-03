
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class StructureAvecContacts  extends Structure  {
    //
    // Attributes.
    //  
    //
    // References
    // 
    private ObservableList<String> proprietairesIds = FXCollections.observableArrayList();
 
    private StringProperty gestionnaires = new SimpleStringProperty();
 
    private StringProperty gardiens = new SimpleStringProperty();
 
    private ObservableList<String> conventionsIds = FXCollections.observableArrayList();
 
 

    

    public List<String> getProprietairesIds(){
    	return this.proprietairesIds;
    }


    public void setProprietairesIds(List<String> proprietairesIds){
        this.proprietairesIds.clear();
    	this.proprietairesIds.addAll(proprietairesIds);
    }
 

    
    public String getGestionnaires(){
    	return this.gestionnaires.get();
    }

    public void setGestionnaires(String gestionnaires){
    	this.gestionnaires.set( gestionnaires );
    }
 

    
    public String getGardiens(){
    	return this.gardiens.get();
    }

    public void setGardiens(String gardiens){
    	this.gardiens.set( gardiens );
    }
 

    

    public List<String> getConventionsIds(){
    	return this.conventionsIds;
    }


    public void setConventionsIds(List<String> conventionsIds){
        this.conventionsIds.clear();
    	this.conventionsIds.addAll(conventionsIds);
    }

  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[StructureAvecContacts ");
      return builder.toString();
  }


}

