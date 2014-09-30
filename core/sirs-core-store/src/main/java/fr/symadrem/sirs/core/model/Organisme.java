
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Organisme  extends CouchDbDocument  {


   
        
    public StringProperty  adresse = new SimpleStringProperty();
        
    public StringProperty  code_postal = new SimpleStringProperty();
        
    public ObjectProperty<java.util.Date>  date_debut = new SimpleObjectProperty<java.util.Date>();
        
    public ObjectProperty<java.util.Date>  date_fin = new SimpleObjectProperty<java.util.Date>();
        
    public StringProperty  email = new SimpleStringProperty();
        
    public StringProperty  fax = new SimpleStringProperty();
        
    public StringProperty  localite = new SimpleStringProperty();
        
    public StringProperty  mobile = new SimpleStringProperty();
        
    public StringProperty  nom = new SimpleStringProperty();
        
    public StringProperty  pays = new SimpleStringProperty();
        
    public StringProperty  statut_juridique = new SimpleStringProperty();
        
    public StringProperty  telephone = new SimpleStringProperty();
    
 
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
        
    public java.util.Date getDate_debut(){
    	return this.date_debut.get();
    }
    
    public void setDate_debut(java.util.Date date_debut){
    	this.date_debut.set(date_debut);
    }
        
    public java.util.Date getDate_fin(){
    	return this.date_fin.get();
    }
    
    public void setDate_fin(java.util.Date date_fin){
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

