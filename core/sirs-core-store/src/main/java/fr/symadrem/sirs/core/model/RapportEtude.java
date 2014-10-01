
package fr.symadrem.sirs.core.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class RapportEtude  extends Document  {
    //
    // Attributes.
    //      
    public StringProperty  auteur = new SimpleStringProperty();
        
    public StringProperty  type_rapport = new SimpleStringProperty();
        
    public StringProperty  reference_numerique = new SimpleStringProperty();
        
    public StringProperty  reference_papier = new SimpleStringProperty();
    
    //
    // References
    // 
    private String prestationId;
    
      
    public String getAuteur(){
    	return this.auteur.get();
    }
    
    public void setAuteur(String auteur){
    	this.auteur.set(auteur);
    }
        
    public String getType_rapport(){
    	return this.type_rapport.get();
    }
    
    public void setType_rapport(String type_rapport){
    	this.type_rapport.set(type_rapport);
    }
        
    public String getReference_numerique(){
    	return this.reference_numerique.get();
    }
    
    public void setReference_numerique(String reference_numerique){
    	this.reference_numerique.set(reference_numerique);
    }
        
    public String getReference_papier(){
    	return this.reference_papier.get();
    }
    
    public void setReference_papier(String reference_papier){
    	this.reference_papier.set(reference_papier);
    }
    

  
  
    
    public String getPrestation(){
    	return this.prestationId;
    }
    
    public void setPrestation(String prestationId){
    	this.prestationId = prestationId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[RapportEtude ");
      builder.append("auteur: ");
      builder.append(auteur.get());
      builder.append(", ");
      builder.append("type_rapport: ");
      builder.append(type_rapport.get());
      builder.append(", ");
      builder.append("reference_numerique: ");
      builder.append(reference_numerique.get());
      builder.append(", ");
      builder.append("reference_papier: ");
      builder.append(reference_papier.get());
      return builder.toString();
  }


}

