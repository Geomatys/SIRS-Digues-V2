
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
public class OuvrageFranchissement  extends StructureAvecContacts  {
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
    * JavaFX property for type_ouvrage.
    */
    private StringProperty  type_ouvrage = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on type_ouvrage.
    */
    public  StringProperty type_ouvrageProperty() {
       return type_ouvrage;
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
    * JavaFX property for position_structure_haut.
    */
    private StringProperty  position_structure_haut = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on position_structure_haut.
    */
    public  StringProperty position_structure_hautProperty() {
       return position_structure_haut;
    }
    /**
    * JavaFX property for position_structure_bas.
    */
    private StringProperty  position_structure_bas = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on position_structure_bas.
    */
    public  StringProperty position_structure_basProperty() {
       return position_structure_bas;
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
    private ObservableList<String> crue_submersionIds = FXCollections.observableArrayList();
 

    
    public String getNom(){
    	return this.nom.get();
    }
    
    public void setNom(String nom){
    	this.nom.set(nom);
    }    
    
    public String getType_ouvrage(){
    	return this.type_ouvrage.get();
    }
    
    public void setType_ouvrage(String type_ouvrage){
    	this.type_ouvrage.set(type_ouvrage);
    }    
    
    public float getLargeur(){
    	return this.largeur.get();
    }
    
    public void setLargeur(float largeur){
    	this.largeur.set(largeur);
    }    
    
    public String getPosition_structure_haut(){
    	return this.position_structure_haut.get();
    }
    
    public void setPosition_structure_haut(String position_structure_haut){
    	this.position_structure_haut.set(position_structure_haut);
    }    
    
    public String getPosition_structure_bas(){
    	return this.position_structure_bas.get();
    }
    
    public void setPosition_structure_bas(String position_structure_bas){
    	this.position_structure_bas.set(position_structure_bas);
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

    

    public List<String> getCrue_submersionIds(){
    	return this.crue_submersionIds;
    }


    public void setCrue_submersionIds(List<String> crue_submersionIds){
        this.crue_submersionIds.clear();
    	this.crue_submersionIds.addAll(crue_submersionIds);
    }

  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[OuvrageFranchissement ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("type_ouvrage: ");
      builder.append(type_ouvrage.get());
      builder.append(", ");
      builder.append("largeur: ");
      builder.append(largeur.get());
      builder.append(", ");
      builder.append("position_structure_haut: ");
      builder.append(position_structure_haut.get());
      builder.append(", ");
      builder.append("position_structure_bas: ");
      builder.append(position_structure_bas.get());
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

