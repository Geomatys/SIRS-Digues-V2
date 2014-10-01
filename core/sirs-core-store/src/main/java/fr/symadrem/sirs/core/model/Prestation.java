
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Prestation  extends Structure  {


   
        
    public StringProperty  intitule = new SimpleStringProperty();
        
    public StringProperty  type_prestation = new SimpleStringProperty();
        
    public StringProperty  position_structure = new SimpleStringProperty();
        
    public BooleanProperty  realisation_interne = new SimpleBooleanProperty();
        
    public FloatProperty  cout_metre = new SimpleFloatProperty();
        
    public FloatProperty  cout_global = new SimpleFloatProperty();
    
 
    //
    // References
    //
    private String marcheId;
     
    //
    // References
    //
    private List<String> intervenantsIds;
     
    //
    // References
    //
    private String desordreId;
     
    //
    // References
    //
    private String evenement_hydraulique_associeId;
     
    //
    // References
    //
    private String rapportetudeId;
     
    //
    // References
    //
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

