
package fr.symadrem.sirs.core.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class ReseauTelecomEnergie  extends StructureAvecContacts  {
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
    * JavaFX property for type_reseau.
    */
    private StringProperty  type_reseau = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on type_reseau.
    */
    public  StringProperty type_reseauProperty() {
       return type_reseau;
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
    * JavaFX property for implantation.
    */
    private StringProperty  implantation = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on implantation.
    */
    public  StringProperty implantationProperty() {
       return implantation;
    }
    /**
    * JavaFX property for hauteur.
    */
    private FloatProperty  hauteur = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on hauteur.
    */
    public  FloatProperty hauteurProperty() {
       return hauteur;
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
    
    public String getType_reseau(){
    	return this.type_reseau.get();
    }
    
    public void setType_reseau(String type_reseau){
    	this.type_reseau.set(type_reseau);
    }    
    
    public String getPosition_structure(){
    	return this.position_structure.get();
    }
    
    public void setPosition_structure(String position_structure){
    	this.position_structure.set(position_structure);
    }    
    
    public String getImplantation(){
    	return this.implantation.get();
    }
    
    public void setImplantation(String implantation){
    	this.implantation.set(implantation);
    }    
    
    public float getHauteur(){
    	return this.hauteur.get();
    }
    
    public void setHauteur(float hauteur){
    	this.hauteur.set(hauteur);
    }    
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[ReseauTelecomEnergie ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("type_reseau: ");
      builder.append(type_reseau.get());
      builder.append(", ");
      builder.append("position_structure: ");
      builder.append(position_structure.get());
      builder.append(", ");
      builder.append("implantation: ");
      builder.append(implantation.get());
      builder.append(", ");
      builder.append("hauteur: ");
      builder.append(hauteur.get());
      return builder.toString();
  }


}

