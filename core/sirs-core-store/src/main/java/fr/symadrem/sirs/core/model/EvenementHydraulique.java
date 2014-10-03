
package fr.symadrem.sirs.core.model;

import com.geomatys.json.LocalDateTimeDeserializer;
import com.geomatys.json.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.List;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class EvenementHydraulique  extends CouchDbDocument  {
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
    * JavaFX property for type_evenement.
    */
    private StringProperty  type_evenement = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on type_evenement.
    */
    public  StringProperty type_evenementProperty() {
       return type_evenement;
    }
    /**
    * JavaFX property for frequence.
    */
    private StringProperty  frequence = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on frequence.
    */
    public  StringProperty frequenceProperty() {
       return frequence;
    }
    /**
    * JavaFX property for modeleur_hydraulique.
    */
    private StringProperty  modeleur_hydraulique = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on modeleur_hydraulique.
    */
    public  StringProperty modeleur_hydrauliqueProperty() {
       return modeleur_hydraulique;
    }
    /**
    * JavaFX property for vitesse_moy.
    */
    private FloatProperty  vitesse_moy = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on vitesse_moy.
    */
    public  FloatProperty vitesse_moyProperty() {
       return vitesse_moy;
    }
    /**
    * JavaFX property for debit_moy.
    */
    private FloatProperty  debit_moy = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on debit_moy.
    */
    public  FloatProperty debit_moyProperty() {
       return debit_moy;
    }
    /**
    * JavaFX property for date_debut.
    */
    private ObjectProperty<LocalDateTime>  date_debut = new SimpleObjectProperty<LocalDateTime>();
    
    /**
    * Getter for JavaFX property on date_debut.
    */
    public  ObjectProperty<LocalDateTime> date_debutProperty() {
       return date_debut;
    }
    /**
    * JavaFX property for date_fin.
    */
    private ObjectProperty<LocalDateTime>  date_fin = new SimpleObjectProperty<LocalDateTime>();
    
    /**
    * Getter for JavaFX property on date_fin.
    */
    public  ObjectProperty<LocalDateTime> date_finProperty() {
       return date_fin;
    }
    //
    // References
    // 
    private ObservableList<String> evenementMeteoIds = FXCollections.observableArrayList();
 

    
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

    @JsonSerialize(using=LocalDateTimeSerializer.class)    
    public LocalDateTime getDate_debut(){
    	return this.date_debut.get();
    }

    @JsonDeserialize(using=LocalDateTimeDeserializer.class)    
    public void setDate_debut(LocalDateTime date_debut){
    	this.date_debut.set(date_debut);
    }    

    @JsonSerialize(using=LocalDateTimeSerializer.class)    
    public LocalDateTime getDate_fin(){
    	return this.date_fin.get();
    }

    @JsonDeserialize(using=LocalDateTimeDeserializer.class)    
    public void setDate_fin(LocalDateTime date_fin){
    	this.date_fin.set(date_fin);
    }     

    

    public List<String> getEvenementMeteoIds(){
    	return this.evenementMeteoIds;
    }


    public void setEvenementMeteoIds(List<String> evenementMeteoIds){
        this.evenementMeteoIds.clear();
    	this.evenementMeteoIds.addAll(evenementMeteoIds);
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

