
package fr.symadrem.sirs.core.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class ReseauHydrauliqueFerme  extends StructureAvecContacts  {
    //
    // Attributes.
    //      
    public StringProperty  nom = new SimpleStringProperty();
        
    public StringProperty  type_reseau = new SimpleStringProperty();
        
    public StringProperty  position_structure = new SimpleStringProperty();
        
    public StringProperty  utilisation = new SimpleStringProperty();
        
    public StringProperty  implatation = new SimpleStringProperty();
        
    public StringProperty  ecoulement = new SimpleStringProperty();
        
    public BooleanProperty  autorise = new SimpleBooleanProperty();
        
    public FloatProperty  diametre = new SimpleFloatProperty();
    
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

