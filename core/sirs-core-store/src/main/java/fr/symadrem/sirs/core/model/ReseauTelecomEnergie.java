
package fr.symadrem.sirs.core.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class ReseauTelecomEnergie  extends StructureAvecContacts  {


   
        
    public StringProperty  nom = new SimpleStringProperty();
        
    public StringProperty  type_reseau = new SimpleStringProperty();
        
    public StringProperty  position_structure = new SimpleStringProperty();
        
    public StringProperty  implantation = new SimpleStringProperty();
        
    public FloatProperty  hauteur = new SimpleFloatProperty();
    
 
    //
    // References
    //
    private String ouvrage_telecom_energieId;
    
  
  
      
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
        
    public String getImplantation(){
    	return this.implantation.get();
    }
    
    public void setImplantation(String implantation){
    	this.implantation.set(implantation);
    }
        
    public float getHauteur(){
    	return this.hauteur.get();
    }
    
    public void setHauteur(float hauteur){
    	this.hauteur.set(hauteur);
    }
    

  
  
    
    public String getOuvrage_telecom_energie(){
    	return this.ouvrage_telecom_energieId;
    }
    
    public void setOuvrage_telecom_energie(String ouvrage_telecom_energieId){
    	this.ouvrage_telecom_energieId = ouvrage_telecom_energieId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[ReseauTelecomEnergie ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("type_reseau: ");
      builder.append(type_reseau.get());
      builder.append(", ");
      builder.append("position_structure: ");
      builder.append(position_structure.get());
      builder.append(", ");
      builder.append("implantation: ");
      builder.append(implantation.get());
      builder.append(", ");
      builder.append("hauteur: ");
      builder.append(hauteur.get());
      return builder.toString();
  }


}

