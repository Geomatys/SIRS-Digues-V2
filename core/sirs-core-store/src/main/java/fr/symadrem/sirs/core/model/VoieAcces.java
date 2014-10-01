
package fr.symadrem.sirs.core.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class VoieAcces  extends StructureAvecContacts  {
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
    * JavaFX property for nature.
    */
    private StringProperty  nature = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on nature.
    */
    public  StringProperty natureProperty() {
       return nature;
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
    * JavaFX property for numéro_secteur.
    */
    private IntegerProperty  numéro_secteur = new SimpleIntegerProperty();
    
    /**
    * Getter for JavaFX property on numéro_secteur.
    */
    public  IntegerProperty numéro_secteurProperty() {
       return numéro_secteur;
    }
    //
    // References
    // 

    
    public String getNom(){
    	return this.nom.get();
    }
    
    public void setNom(String nom){
    	this.nom.set(nom);
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
    
    public String getNature(){
    	return this.nature.get();
    }
    
    public void setNature(String nature){
    	this.nature.set(nature);
    }    
    
    public String getUsage(){
    	return this.usage.get();
    }
    
    public void setUsage(String usage){
    	this.usage.set(usage);
    }    
    
    public int getNuméro_secteur(){
    	return this.numéro_secteur.get();
    }
    
    public void setNuméro_secteur(int numéro_secteur){
    	this.numéro_secteur.set(numéro_secteur);
    }    
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[VoieAcces ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("largeur: ");
      builder.append(largeur.get());
      builder.append(", ");
      builder.append("position_structure: ");
      builder.append(position_structure.get());
      builder.append(", ");
      builder.append("nature: ");
      builder.append(nature.get());
      builder.append(", ");
      builder.append("usage: ");
      builder.append(usage.get());
      builder.append(", ");
      builder.append("numéro_secteur: ");
      builder.append(numéro_secteur.get());
      return builder.toString();
  }


}

