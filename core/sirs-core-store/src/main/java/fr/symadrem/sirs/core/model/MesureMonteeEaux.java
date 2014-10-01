
package fr.symadrem.sirs.core.model;

import java.util.Date;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class MesureMonteeEaux  extends CouchDbDocument  {
    //
    // Attributes.
    //      
    public ObjectProperty<Date>  date = new SimpleObjectProperty<Date>();
        
    public StringProperty  reference_hauteur = new SimpleStringProperty();
        
    public FloatProperty  hauteur = new SimpleFloatProperty();
        
    public FloatProperty  debit_max = new SimpleFloatProperty();
    
    //
    // References
    // 
    private String observateursId;
    
      
    public Date getDate(){
    	return this.date.get();
    }
    
    public void setDate(Date date){
    	this.date.set(date);
    }
        
    public String getReference_hauteur(){
    	return this.reference_hauteur.get();
    }
    
    public void setReference_hauteur(String reference_hauteur){
    	this.reference_hauteur.set(reference_hauteur);
    }
        
    public float getHauteur(){
    	return this.hauteur.get();
    }
    
    public void setHauteur(float hauteur){
    	this.hauteur.set(hauteur);
    }
        
    public float getDebit_max(){
    	return this.debit_max.get();
    }
    
    public void setDebit_max(float debit_max){
    	this.debit_max.set(debit_max);
    }
    

  
  
    
    public String getObservateurs(){
    	return this.observateursId;
    }
    
    public void setObservateurs(String observateursId){
    	this.observateursId = observateursId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[MesureMonteeEaux ");
      builder.append("date: ");
      builder.append(date.get());
      builder.append(", ");
      builder.append("reference_hauteur: ");
      builder.append(reference_hauteur.get());
      builder.append(", ");
      builder.append("hauteur: ");
      builder.append(hauteur.get());
      builder.append(", ");
      builder.append("debit_max: ");
      builder.append(debit_max.get());
      return builder.toString();
  }


}

