
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class VoieDigue  extends StructureAvecContacts  {
    //
    // Attributes.
    //      
    public StringProperty  nom = new SimpleStringProperty();
        
    public StringProperty  type_voie = new SimpleStringProperty();
        
    public FloatProperty  largeur = new SimpleFloatProperty();
        
    public StringProperty  position_structure = new SimpleStringProperty();
        
    public StringProperty  revetement = new SimpleStringProperty();
        
    public StringProperty  usage = new SimpleStringProperty();
        
    public StringProperty  orientation = new SimpleStringProperty();
    
    //
    // References
    // 
    private List<String> servitudesIds;
    
      
    public String getNom(){
    	return this.nom.get();
    }
    
    public void setNom(String nom){
    	this.nom.set(nom);
    }
        
    public String getType_voie(){
    	return this.type_voie.get();
    }
    
    public void setType_voie(String type_voie){
    	this.type_voie.set(type_voie);
    }
        
    public float getLargeur(){
    	return this.largeur.get();
    }
    
    public void setLargeur(float largeur){
    	this.largeur.set(largeur);
    }
        
    public String getPosition_structure(){
    	return this.position_structure.get();
    }
    
    public void setPosition_structure(String position_structure){
    	this.position_structure.set(position_structure);
    }
        
    public String getRevetement(){
    	return this.revetement.get();
    }
    
    public void setRevetement(String revetement){
    	this.revetement.set(revetement);
    }
        
    public String getUsage(){
    	return this.usage.get();
    }
    
    public void setUsage(String usage){
    	this.usage.set(usage);
    }
        
    public String getOrientation(){
    	return this.orientation.get();
    }
    
    public void setOrientation(String orientation){
    	this.orientation.set(orientation);
    }
    

  
  
    
    public List<String> getServitudesIds(){
    	return this.servitudesIds;
    }
    
    public void setServitudesIds(List<String> servitudesIds){
    	this.servitudesIds = servitudesIds;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[VoieDigue ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("type_voie: ");
      builder.append(type_voie.get());
      builder.append(", ");
      builder.append("largeur: ");
      builder.append(largeur.get());
      builder.append(", ");
      builder.append("position_structure: ");
      builder.append(position_structure.get());
      builder.append(", ");
      builder.append("revetement: ");
      builder.append(revetement.get());
      builder.append(", ");
      builder.append("usage: ");
      builder.append(usage.get());
      builder.append(", ");
      builder.append("orientation: ");
      builder.append(orientation.get());
      return builder.toString();
  }


}

