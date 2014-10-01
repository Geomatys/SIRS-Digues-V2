
package fr.symadrem.sirs.core.model;

import java.util.Date;
import java.util.List;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class EvenementHydraulique  extends CouchDbDocument  {
    //
    // Attributes.
    //      
    public StringProperty  nom = new SimpleStringProperty();
        
    public StringProperty  type_evenement = new SimpleStringProperty();
        
    public StringProperty  frequence = new SimpleStringProperty();
        
    public StringProperty  modeleur_hydraulique = new SimpleStringProperty();
        
    public FloatProperty  vitesse_moy = new SimpleFloatProperty();
        
    public FloatProperty  debit_moy = new SimpleFloatProperty();
        
    public ObjectProperty<Date>  date_debut = new SimpleObjectProperty<Date>();
        
    public ObjectProperty<Date>  date_fin = new SimpleObjectProperty<Date>();
    
    //
    // References
    // 
    private String laissecrueId;
     
    private List<String> evenements_meteoIds;
     
    private String monteeeauxId;
     
    private String ligneeauId;
    
      
    public String getNom(){
    	return this.nom.get();
    }
    
    public void setNom(String nom){
    	this.nom.set(nom);
    }
        
    public String getType_evenement(){
    	return this.type_evenement.get();
    }
    
    public void setType_evenement(String type_evenement){
    	this.type_evenement.set(type_evenement);
    }
        
    public String getFrequence(){
    	return this.frequence.get();
    }
    
    public void setFrequence(String frequence){
    	this.frequence.set(frequence);
    }
        
    public String getModeleur_hydraulique(){
    	return this.modeleur_hydraulique.get();
    }
    
    public void setModeleur_hydraulique(String modeleur_hydraulique){
    	this.modeleur_hydraulique.set(modeleur_hydraulique);
    }
        
    public float getVitesse_moy(){
    	return this.vitesse_moy.get();
    }
    
    public void setVitesse_moy(float vitesse_moy){
    	this.vitesse_moy.set(vitesse_moy);
    }
        
    public float getDebit_moy(){
    	return this.debit_moy.get();
    }
    
    public void setDebit_moy(float debit_moy){
    	this.debit_moy.set(debit_moy);
    }
        
    public Date getDate_debut(){
    	return this.date_debut.get();
    }
    
    public void setDate_debut(Date date_debut){
    	this.date_debut.set(date_debut);
    }
        
    public Date getDate_fin(){
    	return this.date_fin.get();
    }
    
    public void setDate_fin(Date date_fin){
    	this.date_fin.set(date_fin);
    }
    

  
  
    
    public String getLaissecrue(){
    	return this.laissecrueId;
    }
    
    public void setLaissecrue(String laissecrueId){
    	this.laissecrueId = laissecrueId;
    }
   
  
    
    public List<String> getEvenements_meteoIds(){
    	return this.evenements_meteoIds;
    }
    
    public void setEvenements_meteoIds(List<String> evenements_meteoIds){
    	this.evenements_meteoIds = evenements_meteoIds;
    }
   
  
    
    public String getMonteeeaux(){
    	return this.monteeeauxId;
    }
    
    public void setMonteeeaux(String monteeeauxId){
    	this.monteeeauxId = monteeeauxId;
    }
   
  
    
    public String getLigneeau(){
    	return this.ligneeauId;
    }
    
    public void setLigneeau(String ligneeauId){
    	this.ligneeauId = ligneeauId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[EvenementHydraulique ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("type_evenement: ");
      builder.append(type_evenement.get());
      builder.append(", ");
      builder.append("frequence: ");
      builder.append(frequence.get());
      builder.append(", ");
      builder.append("modeleur_hydraulique: ");
      builder.append(modeleur_hydraulique.get());
      builder.append(", ");
      builder.append("vitesse_moy: ");
      builder.append(vitesse_moy.get());
      builder.append(", ");
      builder.append("debit_moy: ");
      builder.append(debit_moy.get());
      builder.append(", ");
      builder.append("date_debut: ");
      builder.append(date_debut.get());
      builder.append(", ");
      builder.append("date_fin: ");
      builder.append(date_fin.get());
      return builder.toString();
  }


}

