
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
public class LaisseCrue  extends Structure  {
    //
    // Attributes.
    //      
    public FloatProperty  hauteur = new SimpleFloatProperty();
        
    public StringProperty  reference_hauteur = new SimpleStringProperty();
        
    public StringProperty  position_laisse = new SimpleStringProperty();
    
    //
    // References
    // 
    private String evenementhydrauliqueId;
     
    private String observateurId;
     
    private List<String> articles_journauxIds;
    
      
    public float getHauteur(){
    	return this.hauteur.get();
    }
    
    public void setHauteur(float hauteur){
    	this.hauteur.set(hauteur);
    }
        
    public String getReference_hauteur(){
    	return this.reference_hauteur.get();
    }
    
    public void setReference_hauteur(String reference_hauteur){
    	this.reference_hauteur.set(reference_hauteur);
    }
        
    public String getPosition_laisse(){
    	return this.position_laisse.get();
    }
    
    public void setPosition_laisse(String position_laisse){
    	this.position_laisse.set(position_laisse);
    }
    

  
  
    
    public String getEvenementhydraulique(){
    	return this.evenementhydrauliqueId;
    }
    
    public void setEvenementhydraulique(String evenementhydrauliqueId){
    	this.evenementhydrauliqueId = evenementhydrauliqueId;
    }
   
  
    
    public String getObservateur(){
    	return this.observateurId;
    }
    
    public void setObservateur(String observateurId){
    	this.observateurId = observateurId;
    }
   
  
    
    public List<String> getArticles_journauxIds(){
    	return this.articles_journauxIds;
    }
    
    public void setArticles_journauxIds(List<String> articles_journauxIds){
    	this.articles_journauxIds = articles_journauxIds;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[LaisseCrue ");
      builder.append("hauteur: ");
      builder.append(hauteur.get());
      builder.append(", ");
      builder.append("reference_hauteur: ");
      builder.append(reference_hauteur.get());
      builder.append(", ");
      builder.append("position_laisse: ");
      builder.append(position_laisse.get());
      return builder.toString();
  }


}

