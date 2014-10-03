
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private ObservableList<String> articleIds = FXCollections.observableArrayList();
 
    private ObservableList<String> observationIds = FXCollections.observableArrayList();
 
    private StringProperty evenementId = new SimpleStringProperty();
 

    
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

    

    public List<String> getArticleIds(){
    	return this.articleIds;
    }


    public void setArticleIds(List<String> articleIds){
        this.articleIds.clear();
    	this.articleIds.addAll(articleIds);
    }
 

    

    public List<String> getObservationIds(){
    	return this.observationIds;
    }


    public void setObservationIds(List<String> observationIds){
        this.observationIds.clear();
    	this.observationIds.addAll(observationIds);
    }
 

    
    public String getEvenementId(){
    	return this.evenementId.get();
    }

    public void setEvenementId(String evenementId){
    	this.evenementId.set( evenementId );
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

