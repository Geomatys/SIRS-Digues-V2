
package fr.symadrem.sirs.core.model;

import java.time.Instant;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class ObservationSuivi  extends CouchDbDocument  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for date_observation.
    */
    private ObjectProperty<Instant>  date_observation = new SimpleObjectProperty<Instant>();
    
    /**
    * Getter for JavaFX property on date_observation.
    */
    public  ObjectProperty<Instant> date_observationProperty() {
       return date_observation;
    }
    /**
    * JavaFX property for nombre_desordres.
    */
    private IntegerProperty  nombre_desordres = new SimpleIntegerProperty();
    
    /**
    * Getter for JavaFX property on nombre_desordres.
    */
    public  IntegerProperty nombre_desordresProperty() {
       return nombre_desordres;
    }
    /**
    * JavaFX property for urgence.
    */
    private StringProperty  urgence = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on urgence.
    */
    public  StringProperty urgenceProperty() {
       return urgence;
    }
    /**
    * JavaFX property for evolution.
    */
    private StringProperty  evolution = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on evolution.
    */
    public  StringProperty evolutionProperty() {
       return evolution;
    }
    /**
    * JavaFX property for suite.
    */
    private StringProperty  suite = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on suite.
    */
    public  StringProperty suiteProperty() {
       return suite;
    }
    //
    // References
    // 
    private String observateurId;
    
      
    public Instant getDate_observation(){
    	return this.date_observation.get();
    }
    
    public void setDate_observation(Instant date_observation){
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

