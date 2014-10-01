
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
public class DocumentGrandeEchelle  extends Document  {


   
        
    public StringProperty  type_documentGE = new SimpleStringProperty();
        
    public StringProperty  reference_numerique = new SimpleStringProperty();
        
    public StringProperty  reference_papier = new SimpleStringProperty();
        
    public StringProperty  reference_calque = new SimpleStringProperty();
    
 
    //
    // References
    //
    private String prestationId;
    
  
  
      
    public String getType_documentGE(){
    	return this.type_documentGE.get();
    }
    
    public void setType_documentGE(String type_documentGE){
    	this.type_documentGE.set(type_documentGE);
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
        
    public String getReference_calque(){
    	return this.reference_calque.get();
    }
    
    public void setReference_calque(String reference_calque){
    	this.reference_calque.set(reference_calque);
    }
    

  
  
    
    public String getPrestation(){
    	return this.prestationId;
    }
    
    public void setPrestation(String prestationId){
    	this.prestationId = prestationId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[DocumentGrandeEchelle ");
      builder.append("type_documentGE: ");
      builder.append(type_documentGE.get());
      builder.append(", ");
      builder.append("reference_numerique: ");
      builder.append(reference_numerique.get());
      builder.append(", ");
      builder.append("reference_papier: ");
      builder.append(reference_papier.get());
      builder.append(", ");
      builder.append("reference_calque: ");
      builder.append(reference_calque.get());
      return builder.toString();
  }


}

