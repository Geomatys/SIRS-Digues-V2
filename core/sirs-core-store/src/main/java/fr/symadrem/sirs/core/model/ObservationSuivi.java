
package fr.symadrem.sirs.core.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class ObservationSuivi  extends CouchDbDocument  {


   
        
    public ObjectProperty<java.util.Date>  date_observation = new SimpleObjectProperty<java.util.Date>();
        
    public IntegerProperty  nombre_desordres = new SimpleIntegerProperty();
        
    public StringProperty  urgence = new SimpleStringProperty();
        
    public StringProperty  evolution = new SimpleStringProperty();
        
    public StringProperty  suite = new SimpleStringProperty();
    
 
    //
    // References
    //
    private String observateurId;
    
  
  
      
    public java.util.Date getDate_observation(){
    	return this.date_observation.get();
    }
    
    public void setDate_observation(java.util.Date date_observation){
    	this.date_observation.set(date_observation);
    }
        
    public int getNombre_desordres(){
    	return this.nombre_desordres.get();
    }
    
    public void setNombre_desordres(int nombre_desordres){
    	this.nombre_desordres.set(nombre_desordres);
    }
        
    public String getUrgence(){
    	return this.urgence.get();
    }
    
    public void setUrgence(String urgence){
    	this.urgence.set(urgence);
    }
        
    public String getEvolution(){
    	return this.evolution.get();
    }
    
    public void setEvolution(String evolution){
    	this.evolution.set(evolution);
    }
        
    public String getSuite(){
    	return this.suite.get();
    }
    
    public void setSuite(String suite){
    	this.suite.set(suite);
    }
    

  
  
    
    public String getObservateur(){
    	return this.observateurId;
    }
    
    public void setObservateur(String observateurId){
    	this.observateurId = observateurId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[ObservationSuivi ");
      builder.append("date_observation: ");
      builder.append(date_observation.get());
      builder.append(", ");
      builder.append("nombre_desordres: ");
      builder.append(nombre_desordres.get());
      builder.append(", ");
      builder.append("urgence: ");
      builder.append(urgence.get());
      builder.append(", ");
      builder.append("evolution: ");
      builder.append(evolution.get());
      builder.append(", ");
      builder.append("suite: ");
      builder.append(suite.get());
      return builder.toString();
  }


}

