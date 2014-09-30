
package fr.symadrem.sirs.core.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class OuvertureBatardable  extends StructureAvecContacts  {


   
        
    public StringProperty  nom = new SimpleStringProperty();
        
    public StringProperty  type_glissiere = new SimpleStringProperty();
        
    public FloatProperty  hauteur = new SimpleFloatProperty();
        
    public FloatProperty  largeur = new SimpleFloatProperty();
        
    public FloatProperty  z_du_seuil = new SimpleFloatProperty();
        
    public StringProperty  type_seuil = new SimpleStringProperty();
        
    public StringProperty  nature_batardeaux = new SimpleStringProperty();
        
    public IntegerProperty  nombre_batardeaux = new SimpleIntegerProperty();
        
    public FloatProperty  poids_unitaires_batardeaux = new SimpleFloatProperty();
        
    public StringProperty  moyen_manipulation_batardeaux = new SimpleStringProperty();
        
    public StringProperty  organisme_manipulateur_batardeaux = new SimpleStringProperty();
        
    public StringProperty  organisme_stockant_batardeaux = new SimpleStringProperty();
        
    public StringProperty  intervenant_manipulateur_batardeaux = new SimpleStringProperty();
    
 
    //
    // References
    //
    private String intervenants_manupulateursId;
     
    //
    // References
    //
    private String organismes_manipulateursId;
     
    //
    // References
    //
    private String organismes_stockantsId;
     
    //
    // References
    //
    private String ouvrage_revancheId;
    
  
  
      
    public String getNom(){
    	return this.nom.get();
    }
    
    public void setNom(String nom){
    	this.nom.set(nom);
    }
        
    public String getType_glissiere(){
    	return this.type_glissiere.get();
    }
    
    public void setType_glissiere(String type_glissiere){
    	this.type_glissiere.set(type_glissiere);
    }
        
    public float getHauteur(){
    	return this.hauteur.get();
    }
    
    public void setHauteur(float hauteur){
    	this.hauteur.set(hauteur);
    }
        
    public float getLargeur(){
    	return this.largeur.get();
    }
    
    public void setLargeur(float largeur){
    	this.largeur.set(largeur);
    }
        
    public float getZ_du_seuil(){
    	return this.z_du_seuil.get();
    }
    
    public void setZ_du_seuil(float z_du_seuil){
    	this.z_du_seuil.set(z_du_seuil);
    }
        
    public String getType_seuil(){
    	return this.type_seuil.get();
    }
    
    public void setType_seuil(String type_seuil){
    	this.type_seuil.set(type_seuil);
    }
        
    public String getNature_batardeaux(){
    	return this.nature_batardeaux.get();
    }
    
    public void setNature_batardeaux(String nature_batardeaux){
    	this.nature_batardeaux.set(nature_batardeaux);
    }
        
    public int getNombre_batardeaux(){
    	return this.nombre_batardeaux.get();
    }
    
    public void setNombre_batardeaux(int nombre_batardeaux){
    	this.nombre_batardeaux.set(nombre_batardeaux);
    }
        
    public float getPoids_unitaires_batardeaux(){
    	return this.poids_unitaires_batardeaux.get();
    }
    
    public void setPoids_unitaires_batardeaux(float poids_unitaires_batardeaux){
    	this.poids_unitaires_batardeaux.set(poids_unitaires_batardeaux);
    }
        
    public String getMoyen_manipulation_batardeaux(){
    	return this.moyen_manipulation_batardeaux.get();
    }
    
    public void setMoyen_manipulation_batardeaux(String moyen_manipulation_batardeaux){
    	this.moyen_manipulation_batardeaux.set(moyen_manipulation_batardeaux);
    }
        
    public String getOrganisme_manipulateur_batardeaux(){
    	return this.organisme_manipulateur_batardeaux.get();
    }
    
    public void setOrganisme_manipulateur_batardeaux(String organisme_manipulateur_batardeaux){
    	this.organisme_manipulateur_batardeaux.set(organisme_manipulateur_batardeaux);
    }
        
    public String getOrganisme_stockant_batardeaux(){
    	return this.organisme_stockant_batardeaux.get();
    }
    
    public void setOrganisme_stockant_batardeaux(String organisme_stockant_batardeaux){
    	this.organisme_stockant_batardeaux.set(organisme_stockant_batardeaux);
    }
        
    public String getIntervenant_manipulateur_batardeaux(){
    	return this.intervenant_manipulateur_batardeaux.get();
    }
    
    public void setIntervenant_manipulateur_batardeaux(String intervenant_manipulateur_batardeaux){
    	this.intervenant_manipulateur_batardeaux.set(intervenant_manipulateur_batardeaux);
    }
    

  
  
    
    public String getIntervenants_manupulateurs(){
    	return this.intervenants_manupulateursId;
    }
    
    public void setIntervenants_manupulateurs(String intervenants_manupulateursId){
    	this.intervenants_manupulateursId = intervenants_manupulateursId;
    }
   
  
    
    public String getOrganismes_manipulateurs(){
    	return this.organismes_manipulateursId;
    }
    
    public void setOrganismes_manipulateurs(String organismes_manipulateursId){
    	this.organismes_manipulateursId = organismes_manipulateursId;
    }
   
  
    
    public String getOrganismes_stockants(){
    	return this.organismes_stockantsId;
    }
    
    public void setOrganismes_stockants(String organismes_stockantsId){
    	this.organismes_stockantsId = organismes_stockantsId;
    }
   
  
    
    public String getOuvrage_revanche(){
    	return this.ouvrage_revancheId;
    }
    
    public void setOuvrage_revanche(String ouvrage_revancheId){
    	this.ouvrage_revancheId = ouvrage_revancheId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[OuvertureBatardable ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("type_glissiere: ");
      builder.append(type_glissiere.get());
      builder.append(", ");
      builder.append("hauteur: ");
      builder.append(hauteur.get());
      builder.append(", ");
      builder.append("largeur: ");
      builder.append(largeur.get());
      builder.append(", ");
      builder.append("z_du_seuil: ");
      builder.append(z_du_seuil.get());
      builder.append(", ");
      builder.append("type_seuil: ");
      builder.append(type_seuil.get());
      builder.append(", ");
      builder.append("nature_batardeaux: ");
      builder.append(nature_batardeaux.get());
      builder.append(", ");
      builder.append("nombre_batardeaux: ");
      builder.append(nombre_batardeaux.get());
      builder.append(", ");
      builder.append("poids_unitaires_batardeaux: ");
      builder.append(poids_unitaires_batardeaux.get());
      builder.append(", ");
      builder.append("moyen_manipulation_batardeaux: ");
      builder.append(moyen_manipulation_batardeaux.get());
      builder.append(", ");
      builder.append("organisme_manipulateur_batardeaux: ");
      builder.append(organisme_manipulateur_batardeaux.get());
      builder.append(", ");
      builder.append("organisme_stockant_batardeaux: ");
      builder.append(organisme_stockant_batardeaux.get());
      builder.append(", ");
      builder.append("intervenant_manipulateur_batardeaux: ");
      builder.append(intervenant_manipulateur_batardeaux.get());
      return builder.toString();
  }


}

