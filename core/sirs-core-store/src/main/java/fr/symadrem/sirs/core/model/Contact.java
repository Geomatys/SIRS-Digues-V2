
package fr.symadrem.sirs.core.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Contact  extends CouchDbDocument  {


   
        
    private StringProperty  adresse = new SimpleStringProperty();
        
    private StringProperty  code_postal = new SimpleStringProperty();
        
    private StringProperty  email = new SimpleStringProperty();
        
    private StringProperty  fax = new SimpleStringProperty();
        
    private StringProperty  localite = new SimpleStringProperty();
        
    private StringProperty  mobile = new SimpleStringProperty();
        
    private StringProperty  nom = new SimpleStringProperty();
        
    private StringProperty  pays = new SimpleStringProperty();
        
    private StringProperty  prenom = new SimpleStringProperty();
        
    private StringProperty  telephone = new SimpleStringProperty();
    
 
    //
    // References
    //
    private String organismeId;
    
  
  
      
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
        
    public String getPrenom(){
    	return this.prenom.get();
    }
    
    public void setPrenom(String prenom){
    	this.prenom.set(prenom);
    }
        
    public String getTelephone(){
    	return this.telephone.get();
    }
    
    public void setTelephone(String telephone){
    	this.telephone.set(telephone);
    }
    

  
  
    
    public String getOrganisme(){
    	return this.organismeId;
    }
    
    public void setOrganisme(String organismeId){
    	this.organismeId = organismeId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Contact ");
      builder.append("adresse: ");
      builder.append(adresse.get());
      builder.append(", ");
      builder.append("code_postal: ");
      builder.append(code_postal.get());
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
      builder.append("prenom: ");
      builder.append(prenom.get());
      builder.append(", ");
      builder.append("telephone: ");
      builder.append(telephone.get());
      return builder.toString();
  }


}

