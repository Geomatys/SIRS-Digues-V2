
package fr.symadrem.sirs.core.model;

import java.util.List;
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
    private ObservableList<String> proprietaireIds = FXCollections.observableArrayList();
 
    private ObservableList<String> gestionnaireIds = FXCollections.observableArrayList();
 
    private ObservableList<String> gardienIds = FXCollections.observableArrayList();
 
    private ObservableList<String> conventionIds = FXCollections.observableArrayList();
 
 

    

    public List<String> getProprietaireIds(){
    	return this.proprietaireIds;
    }


    public void setProprietaireIds(List<String> proprietaireIds){
        this.proprietaireIds.clear();
    	this.proprietaireIds.addAll(proprietaireIds);
    }
 

    

    public List<String> getGestionnaireIds(){
    	return this.gestionnaireIds;
    }


    public void setGestionnaireIds(List<String> gestionnaireIds){
        this.gestionnaireIds.clear();
    	this.gestionnaireIds.addAll(gestionnaireIds);
    }
 

    

    public List<String> getGardienIds(){
    	return this.gardienIds;
    }


    public void setGardienIds(List<String> gardienIds){
        this.gardienIds.clear();
    	this.gardienIds.addAll(gardienIds);
    }
 

    

    public List<String> getConventionIds(){
    	return this.conventionIds;
    }


    public void setConventionIds(List<String> conventionIds){
        this.conventionIds.clear();
    	this.conventionIds.addAll(conventionIds);
    }

  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[StructureAvecContacts ");
      return builder.toString();
  }


}

