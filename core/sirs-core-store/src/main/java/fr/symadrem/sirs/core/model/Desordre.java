
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Desordre  extends Structure  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for intitule.
    */
    private StringProperty  intitule = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on intitule.
    */
    public  StringProperty intituleProperty() {
       return intitule;
    }
    /**
    * JavaFX property for type_prestation.
    */
    private StringProperty  type_prestation = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on type_prestation.
    */
    public  StringProperty type_prestationProperty() {
       return type_prestation;
    }
    /**
    * JavaFX property for position_structure.
    */
    private StringProperty  position_structure = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on position_structure.
    */
    public  StringProperty position_structureProperty() {
       return position_structure;
    }
    /**
    * JavaFX property for realisation_interne.
    */
    private BooleanProperty  realisation_interne = new SimpleBooleanProperty();
    
    /**
    * Getter for JavaFX property on realisation_interne.
    */
    public  BooleanProperty realisation_interneProperty() {
       return realisation_interne;
    }
    /**
    * JavaFX property for cout_metre.
    */
    private FloatProperty  cout_metre = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on cout_metre.
    */
    public  FloatProperty cout_metreProperty() {
       return cout_metre;
    }
    /**
    * JavaFX property for cout_global.
    */
    private FloatProperty  cout_global = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on cout_global.
    */
    public  FloatProperty cout_globalProperty() {
       return cout_global;
    }
    //
    // References
    // 
    private List<String> articles_journauxIds;
     
    private String prestationId;
     
    private List<String> observationsIds;
     
    private String evenement_hydraulique_origineId;
    
      
    public String getIntitule(){
    	return this.intitule.get();
    }
    
    public void setIntitule(String intitule){
    	this.intitule.set(intitule);
    }
        
    public String getType_prestation(){
    	return this.type_prestation.get();
    }
    
    public void setType_prestation(String type_prestation){
    	this.type_prestation.set(type_prestation);
    }
        
    public String getPosition_structure(){
    	return this.position_structure.get();
    }
    
    public void setPosition_structure(String position_structure){
    	this.position_structure.set(position_structure);
    }
        
    public boolean getRealisation_interne(){
    	return this.realisation_interne.get();
    }
    
    public void setRealisation_interne(boolean realisation_interne){
    	this.realisation_interne.set(realisation_interne);
    }
        
    public float getCout_metre(){
    	return this.cout_metre.get();
    }
    
    public void setCout_metre(float cout_metre){
    	this.cout_metre.set(cout_metre);
    }
        
    public float getCout_global(){
    	return this.cout_global.get();
    }
    
    public void setCout_global(float cout_global){
    	this.cout_global.set(cout_global);
    }
    

  
  
    
    public List<String> getArticles_journauxIds(){
    	return this.articles_journauxIds;
    }
    
    public void setArticles_journauxIds(List<String> articles_journauxIds){
    	this.articles_journauxIds = articles_journauxIds;
    }
   
  
    
    public String getPrestation(){
    	return this.prestationId;
    }
    
    public void setPrestation(String prestationId){
    	this.prestationId = prestationId;
    }
   
  
    
    public List<String> getObservationsIds(){
    	return this.observationsIds;
    }
    
    public void setObservationsIds(List<String> observationsIds){
    	this.observationsIds = observationsIds;
    }
   
  
    
    public String getEvenement_hydraulique_origine(){
    	return this.evenement_hydraulique_origineId;
    }
    
    public void setEvenement_hydraulique_origine(String evenement_hydraulique_origineId){
    	this.evenement_hydraulique_origineId = evenement_hydraulique_origineId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Desordre ");
      builder.append("intitule: ");
      builder.append(intitule.get());
      builder.append(", ");
      builder.append("type_prestation: ");
      builder.append(type_prestation.get());
      builder.append(", ");
      builder.append("position_structure: ");
      builder.append(position_structure.get());
      builder.append(", ");
      builder.append("realisation_interne: ");
      builder.append(realisation_interne.get());
      builder.append(", ");
      builder.append("cout_metre: ");
      builder.append(cout_metre.get());
      builder.append(", ");
      builder.append("cout_global: ");
      builder.append(cout_global.get());
      return builder.toString();
  }


}

