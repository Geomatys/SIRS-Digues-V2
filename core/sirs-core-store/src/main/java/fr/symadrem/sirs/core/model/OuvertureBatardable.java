
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
public class OuvertureBatardable  extends StructureAvecContacts  {
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
    * JavaFX property for type_glissiere.
    */
    private StringProperty  type_glissiere = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on type_glissiere.
    */
    public  StringProperty type_glissiereProperty() {
       return type_glissiere;
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
    * JavaFX property for z_du_seuil.
    */
    private FloatProperty  z_du_seuil = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on z_du_seuil.
    */
    public  FloatProperty z_du_seuilProperty() {
       return z_du_seuil;
    }
    /**
    * JavaFX property for type_seuil.
    */
    private StringProperty  type_seuil = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on type_seuil.
    */
    public  StringProperty type_seuilProperty() {
       return type_seuil;
    }
    /**
    * JavaFX property for nature_batardeaux.
    */
    private StringProperty  nature_batardeaux = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on nature_batardeaux.
    */
    public  StringProperty nature_batardeauxProperty() {
       return nature_batardeaux;
    }
    /**
    * JavaFX property for nombre_batardeaux.
    */
    private IntegerProperty  nombre_batardeaux = new SimpleIntegerProperty();
    
    /**
    * Getter for JavaFX property on nombre_batardeaux.
    */
    public  IntegerProperty nombre_batardeauxProperty() {
       return nombre_batardeaux;
    }
    /**
    * JavaFX property for poids_unitaires_batardeaux.
    */
    private FloatProperty  poids_unitaires_batardeaux = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on poids_unitaires_batardeaux.
    */
    public  FloatProperty poids_unitaires_batardeauxProperty() {
       return poids_unitaires_batardeaux;
    }
    /**
    * JavaFX property for moyen_manipulation_batardeaux.
    */
    private StringProperty  moyen_manipulation_batardeaux = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on moyen_manipulation_batardeaux.
    */
    public  StringProperty moyen_manipulation_batardeauxProperty() {
       return moyen_manipulation_batardeaux;
    }
    /**
    * JavaFX property for organisme_manipulateur_batardeaux.
    */
    private StringProperty  organisme_manipulateur_batardeaux = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on organisme_manipulateur_batardeaux.
    */
    public  StringProperty organisme_manipulateur_batardeauxProperty() {
       return organisme_manipulateur_batardeaux;
    }
    /**
    * JavaFX property for organisme_stockant_batardeaux.
    */
    private StringProperty  organisme_stockant_batardeaux = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on organisme_stockant_batardeaux.
    */
    public  StringProperty organisme_stockant_batardeauxProperty() {
       return organisme_stockant_batardeaux;
    }
    /**
    * JavaFX property for intervenant_manipulateur_batardeaux.
    */
    private StringProperty  intervenant_manipulateur_batardeaux = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on intervenant_manipulateur_batardeaux.
    */
    public  StringProperty intervenant_manipulateur_batardeauxProperty() {
       return intervenant_manipulateur_batardeaux;
    }
    //
    // References
    // 
    private StringProperty intervenants_manupulateurs = new SimpleStringProperty();
 
    private StringProperty organismes_manipulateurs = new SimpleStringProperty();
 
    private StringProperty organismes_stockants = new SimpleStringProperty();
 
    private StringProperty ouvrageRevancheId = new SimpleStringProperty();
 

    
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
    	return this.intervenants_manupulateurs.get();
    }

    public void setIntervenants_manupulateurs(String intervenants_manupulateurs){
    	this.intervenants_manupulateurs.set( intervenants_manupulateurs );
    }
 

    
    public String getOrganismes_manipulateurs(){
    	return this.organismes_manipulateurs.get();
    }

    public void setOrganismes_manipulateurs(String organismes_manipulateurs){
    	this.organismes_manipulateurs.set( organismes_manipulateurs );
    }
 

    
    public String getOrganismes_stockants(){
    	return this.organismes_stockants.get();
    }

    public void setOrganismes_stockants(String organismes_stockants){
    	this.organismes_stockants.set( organismes_stockants );
    }
 

    
    public String getOuvrageRevancheId(){
    	return this.ouvrageRevancheId.get();
    }

    public void setOuvrageRevancheId(String ouvrageRevancheId){
    	this.ouvrageRevancheId.set( ouvrageRevancheId );
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

