
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
public class Prestation  extends Structure  {
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
    private String marcheId;
     
    private List<String> intervenantsIds;
     
    private String desordreId;
     
    private String evenement_hydraulique_associeId;
     
    private String rapportetudeId;
     
    private String documentgrandeechelleId;
    
      
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
    

  
  
    
    public String getMarche(){
    	return this.marcheId;
    }
    
    public void setMarche(String marcheId){
    	this.marcheId = marcheId;
    }
   
  
    
    public List<String> getIntervenantsIds(){
    	return this.intervenantsIds;
    }
    
    public void setIntervenantsIds(List<String> intervenantsIds){
    	this.intervenantsIds = intervenantsIds;
    }
   
  
    
    public String getDesordre(){
    	return this.desordreId;
    }
    
    public void setDesordre(String desordreId){
    	this.desordreId = desordreId;
    }
   
  
    
    public String getEvenement_hydraulique_associe(){
    	return this.evenement_hydraulique_associeId;
    }
    
    public void setEvenement_hydraulique_associe(String evenement_hydraulique_associeId){
    	this.evenement_hydraulique_associeId = evenement_hydraulique_associeId;
    }
   
  
    
    public String getRapportetude(){
    	return this.rapportetudeId;
    }
    
    public void setRapportetude(String rapportetudeId){
    	this.rapportetudeId = rapportetudeId;
    }
   
  
    
    public String getDocumentgrandeechelle(){
    	return this.documentgrandeechelleId;
    }
    
    public void setDocumentgrandeechelle(String documentgrandeechelleId){
    	this.documentgrandeechelleId = documentgrandeechelleId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Prestation ");
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

