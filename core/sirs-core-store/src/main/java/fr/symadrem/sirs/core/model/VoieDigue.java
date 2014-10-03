
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class VoieDigue  extends StructureAvecContacts  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for nom.
    */
    private StringProperty  nom = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on nom.
    */
    public  StringProperty nomProperty() {
       return nom;
    }
    /**
    * JavaFX property for type_voie.
    */
    private StringProperty  type_voie = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on type_voie.
    */
    public  StringProperty type_voieProperty() {
       return type_voie;
    }
    /**
    * JavaFX property for largeur.
    */
    private FloatProperty  largeur = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on largeur.
    */
    public  FloatProperty largeurProperty() {
       return largeur;
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
    * JavaFX property for revetement.
    */
    private StringProperty  revetement = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on revetement.
    */
    public  StringProperty revetementProperty() {
       return revetement;
    }
    /**
    * JavaFX property for usage.
    */
    private StringProperty  usage = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on usage.
    */
    public  StringProperty usageProperty() {
       return usage;
    }
    /**
    * JavaFX property for orientation.
    */
    private StringProperty  orientation = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on orientation.
    */
    public  StringProperty orientationProperty() {
       return orientation;
    }
    //
    // References
    // 
    private ObservableList<String> servitudeIds = FXCollections.observableArrayList();
 

    
    public String getNom(){
    	return this.nom.get();
    }
    
    public void setNom(String nom){
    	this.nom.set(nom);
    }    
    
    public String getType_voie(){
    	return this.type_voie.get();
    }
    
    public void setType_voie(String type_voie){
    	this.type_voie.set(type_voie);
    }    
    
    public float getLargeur(){
    	return this.largeur.get();
    }
    
    public void setLargeur(float largeur){
    	this.largeur.set(largeur);
    }    
    
    public String getPosition_structure(){
    	return this.position_structure.get();
    }
    
    public void setPosition_structure(String position_structure){
    	this.position_structure.set(position_structure);
    }    
    
    public String getRevetement(){
    	return this.revetement.get();
    }
    
    public void setRevetement(String revetement){
    	this.revetement.set(revetement);
    }    
    
    public String getUsage(){
    	return this.usage.get();
    }
    
    public void setUsage(String usage){
    	this.usage.set(usage);
    }    
    
    public String getOrientation(){
    	return this.orientation.get();
    }
    
    public void setOrientation(String orientation){
    	this.orientation.set(orientation);
    }     

    

    public List<String> getServitudeIds(){
    	return this.servitudeIds;
    }


    public void setServitudeIds(List<String> servitudeIds){
        this.servitudeIds.clear();
    	this.servitudeIds.addAll(servitudeIds);
    }

  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[VoieDigue ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("type_voie: ");
      builder.append(type_voie.get());
      builder.append(", ");
      builder.append("largeur: ");
      builder.append(largeur.get());
      builder.append(", ");
      builder.append("position_structure: ");
      builder.append(position_structure.get());
      builder.append(", ");
      builder.append("revetement: ");
      builder.append(revetement.get());
      builder.append(", ");
      builder.append("usage: ");
      builder.append(usage.get());
      builder.append(", ");
      builder.append("orientation: ");
      builder.append(orientation.get());
      return builder.toString();
  }


}

