
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
public class ReseauHydroCielOuvert  extends StructureAvecContacts  {


   
        
    private StringProperty  nom = new SimpleStringProperty();
        
    private StringProperty  type_reseau = new SimpleStringProperty();
        
    private StringProperty  position_structure = new SimpleStringProperty();
    
 
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
        
    public String getType_reseau(){
    	return this.type_reseau.get();
    }
    
    public void setType_reseau(String type_reseau){
    	this.type_reseau.set(type_reseau);
    }
        
    public String getPosition_structure(){
    	return this.position_structure.get();
    }
    
    public void setPosition_structure(String position_structure){
    	this.position_structure.set(position_structure);
    }
    

  
  
    
    public String getReseau_hydraulique_ferme(){
    	return this.reseau_hydraulique_fermeId;
    }
    
    public void setReseau_hydraulique_ferme(String reseau_hydraulique_fermeId){
    	this.reseau_hydraulique_fermeId = reseau_hydraulique_fermeId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[ReseauHydroCielOuvert ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("type_reseau: ");
      builder.append(type_reseau.get());
      builder.append(", ");
      builder.append("position_structure: ");
      builder.append(position_structure.get());
      return builder.toString();
  }


}

