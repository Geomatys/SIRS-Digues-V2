
package fr.symadrem.sirs.core.model;

import java.time.Instant;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Organisme  extends CouchDbDocument  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for adresse.
    */
    private StringProperty  adresse = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on adresse.
    */
    public  StringProperty adresseProperty() {
       return adresse;
    }
    /**
    * JavaFX property for code_postal.
    */
    private StringProperty  code_postal = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on code_postal.
    */
    public  StringProperty code_postalProperty() {
       return code_postal;
    }
    /**
    * JavaFX property for date_debut.
    */
    private ObjectProperty<Instant>  date_debut = new SimpleObjectProperty<Instant>();
    
    /**
    * Getter for JavaFX property on date_debut.
    */
    public  ObjectProperty<Instant> date_debutProperty() {
       return date_debut;
    }
    /**
    * JavaFX property for date_fin.
    */
    private ObjectProperty<Instant>  date_fin = new SimpleObjectProperty<Instant>();
    
    /**
    * Getter for JavaFX property on date_fin.
    */
    public  ObjectProperty<Instant> date_finProperty() {
       return date_fin;
    }
    /**
    * JavaFX property for email.
    */
    private StringProperty  email = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on email.
    */
    public  StringProperty emailProperty() {
       return email;
    }
    /**
    * JavaFX property for fax.
    */
    private StringProperty  fax = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on fax.
    */
    public  StringProperty faxProperty() {
       return fax;
    }
    /**
    * JavaFX property for localite.
    */
    private StringProperty  localite = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on localite.
    */
    public  StringProperty localiteProperty() {
       return localite;
    }
    /**
    * JavaFX property for mobile.
    */
    private StringProperty  mobile = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on mobile.
    */
    public  StringProperty mobileProperty() {
       return mobile;
    }
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
    * JavaFX property for pays.
    */
    private StringProperty  pays = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on pays.
    */
    public  StringProperty paysProperty() {
       return pays;
    }
    /**
    * JavaFX property for statut_juridique.
    */
    private StringProperty  statut_juridique = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on statut_juridique.
    */
    public  StringProperty statut_juridiqueProperty() {
       return statut_juridique;
    }
    /**
    * JavaFX property for telephone.
    */
    private StringProperty  telephone = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on telephone.
    */
    public  StringProperty telephoneProperty() {
       return telephone;
    }
    //
    // References
    // 
    private List<String> contactIds;
    
      
    public String getAdresse(){
    	return this.adresse.get();
    }
    
    public void setAdresse(String adresse){
    	this.adresse.set(adresse);
    }
        
    public String getCode_postal(){
    	return this.code_postal.get();
    }
    
    public void setCode_postal(String code_postal){
    	this.code_postal.set(code_postal);
    }
        
    public Instant getDate_debut(){
    	return this.date_debut.get();
    }
    
    public void setDate_debut(Instant date_debut){
    	this.date_debut.set(date_debut);
    }
        
    public Instant getDate_fin(){
    	return this.date_fin.get();
    }
    
    public void setDate_fin(Instant date_fin){
    	this.date_fin.set(date_fin);
    }
        
    public String getEmail(){
    	return this.email.get();
    }
    
    public void setEmail(String email){
    	this.email.set(email);
    }
        
    public String getFax(){
    	return this.fax.get();
    }
    
    public void setFax(String fax){
    	this.fax.set(fax);
    }
        
    public String getLocalite(){
    	return this.localite.get();
    }
    
    public void setLocalite(String localite){
    	this.localite.set(localite);
    }
        
    public String getMobile(){
    	return this.mobile.get();
    }
    
    public void setMobile(String mobile){
    	this.mobile.set(mobile);
    }
        
    public String getNom(){
    	return this.nom.get();
    }
    
    public void setNom(String nom){
    	this.nom.set(nom);
    }
        
    public String getPays(){
    	return this.pays.get();
    }
    
    public void setPays(String pays){
    	this.pays.set(pays);
    }
        
    public String getStatut_juridique(){
    	return this.statut_juridique.get();
    }
    
    public void setStatut_juridique(String statut_juridique){
    	this.statut_juridique.set(statut_juridique);
    }
        
    public String getTelephone(){
    	return this.telephone.get();
    }
    
    public void setTelephone(String telephone){
    	this.telephone.set(telephone);
    }
    

  
  
    
    public List<String> getContactIds(){
    	return this.contactIds;
    }
    
    public void setContactIds(List<String> contactIds){
    	this.contactIds = contactIds;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Organisme ");
      builder.append("adresse: ");
      builder.append(adresse.get());
      builder.append(", ");
      builder.append("code_postal: ");
      builder.append(code_postal.get());
      builder.append(", ");
      builder.append("date_debut: ");
      builder.append(date_debut.get());
      builder.append(", ");
      builder.append("date_fin: ");
      builder.append(date_fin.get());
      builder.append(", ");
      builder.append("email: ");
      builder.append(email.get());
      builder.append(", ");
      builder.append("fax: ");
      builder.append(fax.get());
      builder.append(", ");
      builder.append("localite: ");
      builder.append(localite.get());
      builder.append(", ");
      builder.append("mobile: ");
      builder.append(mobile.get());
      builder.append(", ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("pays: ");
      builder.append(pays.get());
      builder.append(", ");
      builder.append("statut_juridique: ");
      builder.append(statut_juridique.get());
      builder.append(", ");
      builder.append("telephone: ");
      builder.append(telephone.get());
      return builder.toString();
  }


}

