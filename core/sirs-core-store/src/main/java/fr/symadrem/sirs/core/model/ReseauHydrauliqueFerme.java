
package fr.symadrem.sirs.core.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class ReseauHydrauliqueFerme  extends StructureAvecContacts  {
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
    * JavaFX property for utilisation.
    */
    private StringProperty  utilisation = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on utilisation.
    */
    public  StringProperty utilisationProperty() {
       return utilisation;
    }
    /**
    * JavaFX property for implatation.
    */
    private StringProperty  implatation = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on implatation.
    */
    public  StringProperty implatationProperty() {
       return implatation;
    }
    /**
    * JavaFX property for ecoulement.
    */
    private StringProperty  ecoulement = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on ecoulement.
    */
    public  StringProperty ecoulementProperty() {
       return ecoulement;
    }
    /**
    * JavaFX property for autorise.
    */
    private BooleanProperty  autorise = new SimpleBooleanProperty();
    
    /**
    * Getter for JavaFX property on autorise.
    */
    public  BooleanProperty autoriseProperty() {
       return autorise;
    }
    /**
    * JavaFX property for diametre.
    */
    private FloatProperty  diametre = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on diametre.
    */
    public  FloatProperty diametreProperty() {
       return diametre;
    }
    //
    // References
    // 
    private String station_pompageId;
 
    private String ouvrage_hydraulique_associeId;
 
    private String reseau_hydro_ciel_ouvertId;
 

    
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
    
    public String getUtilisation(){
    	return this.utilisation.get();
    }
    
    public void setUtilisation(String utilisation){
    	this.utilisation.set(utilisation);
    }    
    
    public String getImplatation(){
    	return this.implatation.get();
    }
    
    public void setImplatation(String implatation){
    	this.implatation.set(implatation);
    }    
    
    public String getEcoulement(){
    	return this.ecoulement.get();
    }
    
    public void setEcoulement(String ecoulement){
    	this.ecoulement.set(ecoulement);
    }    
    
    public boolean getAutorise(){
    	return this.autorise.get();
    }
    
    public void setAutorise(boolean autorise){
    	this.autorise.set(autorise);
    }    
    
    public float getDiametre(){
    	return this.diametre.get();
    }
    
    public void setDiametre(float diametre){
    	this.diametre.set(diametre);
    }     

    
    public String getStation_pompage(){
    	return this.station_pompageId;
    }

    public void setStation_pompage(String station_pompageId){
    	this.station_pompageId = station_pompageId;
    }
 

    
    public String getOuvrage_hydraulique_associe(){
    	return this.ouvrage_hydraulique_associeId;
    }

    public void setOuvrage_hydraulique_associe(String ouvrage_hydraulique_associeId){
    	this.ouvrage_hydraulique_associeId = ouvrage_hydraulique_associeId;
    }
 

    
    public String getReseau_hydro_ciel_ouvert(){
    	return this.reseau_hydro_ciel_ouvertId;
    }

    public void setReseau_hydro_ciel_ouvert(String reseau_hydro_ciel_ouvertId){
    	this.reseau_hydro_ciel_ouvertId = reseau_hydro_ciel_ouvertId;
    }

  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[ReseauHydrauliqueFerme ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("type_reseau: ");
      builder.append(type_reseau.get());
      builder.append(", ");
      builder.append("position_structure: ");
      builder.append(position_structure.get());
      builder.append(", ");
      builder.append("utilisation: ");
      builder.append(utilisation.get());
      builder.append(", ");
      builder.append("implatation: ");
      builder.append(implatation.get());
      builder.append(", ");
      builder.append("ecoulement: ");
      builder.append(ecoulement.get());
      builder.append(", ");
      builder.append("autorise: ");
      builder.append(autorise.get());
      builder.append(", ");
      builder.append("diametre: ");
      builder.append(diametre.get());
      return builder.toString();
  }


}

