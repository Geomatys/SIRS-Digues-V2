
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
public class OuvrageRevanche  extends Structure  {
    //
    // Attributes.
    //      
    public FloatProperty  hauteur_murette = new SimpleFloatProperty();
        
    public FloatProperty  largeur = new SimpleFloatProperty();
        
    public StringProperty  materiau_bas = new SimpleStringProperty();
        
    public StringProperty  materiau_haut = new SimpleStringProperty();
        
    public StringProperty  position_structure = new SimpleStringProperty();
    
    //
    // References
    // 
    private List<String> ouverture_batardableIds;
    
      
    public float getHauteur_murette(){
    	return this.hauteur_murette.get();
    }
    
    public void setHauteur_murette(float hauteur_murette){
    	this.hauteur_murette.set(hauteur_murette);
    }
        
    public float getLargeur(){
    	return this.largeur.get();
    }
    
    public void setLargeur(float largeur){
    	this.largeur.set(largeur);
    }
        
    public String getMateriau_bas(){
    	return this.materiau_bas.get();
    }
    
    public void setMateriau_bas(String materiau_bas){
    	this.materiau_bas.set(materiau_bas);
    }
        
    public String getMateriau_haut(){
    	return this.materiau_haut.get();
    }
    
    public void setMateriau_haut(String materiau_haut){
    	this.materiau_haut.set(materiau_haut);
    }
        
    public String getPosition_structure(){
    	return this.position_structure.get();
    }
    
    public void setPosition_structure(String position_structure){
    	this.position_structure.set(position_structure);
    }
    

  
  
    
    public List<String> getOuverture_batardableIds(){
    	return this.ouverture_batardableIds;
    }
    
    public void setOuverture_batardableIds(List<String> ouverture_batardableIds){
    	this.ouverture_batardableIds = ouverture_batardableIds;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[OuvrageRevanche ");
      builder.append("hauteur_murette: ");
      builder.append(hauteur_murette.get());
      builder.append(", ");
      builder.append("largeur: ");
      builder.append(largeur.get());
      builder.append(", ");
      builder.append("materiau_bas: ");
      builder.append(materiau_bas.get());
      builder.append(", ");
      builder.append("materiau_haut: ");
      builder.append(materiau_haut.get());
      builder.append(", ");
      builder.append("position_structure: ");
      builder.append(position_structure.get());
      return builder.toString();
  }


}

