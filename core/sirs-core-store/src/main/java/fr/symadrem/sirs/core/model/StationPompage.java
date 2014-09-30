
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class StationPompage  extends StructureAvecContacts  {


   
        
    public StringProperty  nom = new SimpleStringProperty();
        
    public StringProperty  position_structure = new SimpleStringProperty();
    
 
    //
    // References
    //
    private List<String> pompesIds;
     
    //
    // References
    //
    private String reseau_hydraulique_fermeId;
    
  
  
      
    public String getNom(){
    	return this.nom.get();
    }
    
    public void setNom(String nom){
    	this.nom.set(nom);
    }
        
    public String getPosition_structure(){
    	return this.position_structure.get();
    }
    
    public void setPosition_structure(String position_structure){
    	this.position_structure.set(position_structure);
    }
    

  
  
    
    public List<String> getPompesIds(){
    	return this.pompesIds;
    }
    
    public void setPompesIds(List<String> pompesIds){
    	this.pompesIds = pompesIds;
    }
   
  
    
    public String getReseau_hydraulique_ferme(){
    	return this.reseau_hydraulique_fermeId;
    }
    
    public void setReseau_hydraulique_ferme(String reseau_hydraulique_fermeId){
    	this.reseau_hydraulique_fermeId = reseau_hydraulique_fermeId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[StationPompage ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("position_structure: ");
      builder.append(position_structure.get());
      return builder.toString();
  }


}

