
package fr.symadrem.sirs.core.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class OuvrageVoirie  extends StructureAvecContacts  {
    //
    // Attributes.
    //      
    public StringProperty  nom = new SimpleStringProperty();
        
    public StringProperty  type_ouvrage = new SimpleStringProperty();
        
    public StringProperty  position_structure = new SimpleStringProperty();
    
    //
    // References
    //
      
    public String getNom(){
    	return this.nom.get();
    }
    
    public void setNom(String nom){
    	this.nom.set(nom);
    }
        
    public String getType_ouvrage(){
    	return this.type_ouvrage.get();
    }
    
    public void setType_ouvrage(String type_ouvrage){
    	this.type_ouvrage.set(type_ouvrage);
    }
        
    public String getPosition_structure(){
    	return this.position_structure.get();
    }
    
    public void setPosition_structure(String position_structure){
    	this.position_structure.set(position_structure);
    }
    

 
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[OuvrageVoirie ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("type_ouvrage: ");
      builder.append(type_ouvrage.get());
      builder.append(", ");
      builder.append("position_structure: ");
      builder.append(position_structure.get());
      return builder.toString();
  }


}

