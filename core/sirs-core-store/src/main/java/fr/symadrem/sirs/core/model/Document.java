
package fr.symadrem.sirs.core.model;

import com.geomatys.json.InstantDeserializer;
import com.geomatys.json.InstantSerializer;
import java.time.Instant;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Document  extends Positionable  {
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
    * JavaFX property for taille.
    */
    private FloatProperty  taille = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on taille.
    */
    public  FloatProperty tailleProperty() {
       return taille;
    }
    /**
    * JavaFX property for description.
    */
    private StringProperty  description = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on description.
    */
    public  StringProperty descriptionProperty() {
       return description;
    }
    /**
    * JavaFX property for type.
    */
    private StringProperty  type = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on type.
    */
    public  StringProperty typeProperty() {
       return type;
    }
    /**
    * JavaFX property for chemin.
    */
    private StringProperty  chemin = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on chemin.
    */
    public  StringProperty cheminProperty() {
       return chemin;
    }
    /**
    * JavaFX property for contenu.
    */
    private StringProperty  contenu = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on contenu.
    */
    public  StringProperty contenuProperty() {
       return contenu;
    }
    /**
    * JavaFX property for date_document.
    */
    private ObjectProperty<Instant>  date_document = new SimpleObjectProperty<Instant>();
    
    /**
    * Getter for JavaFX property on date_document.
    */
    public  ObjectProperty<Instant> date_documentProperty() {
       return date_document;
    }
    //
    // References
    // 
    private StringProperty digueID = new SimpleStringProperty();
 
    private StringProperty IDAssocie = new SimpleStringProperty();
 
    private StringProperty structureId = new SimpleStringProperty();
 

    
    public String getNom(){
    	return this.nom.get();
    }
    
    public void setNom(String nom){
    	this.nom.set(nom);
    }    
    
    public float getTaille(){
    	return this.taille.get();
    }
    
    public void setTaille(float taille){
    	this.taille.set(taille);
    }    
    
    public String getDescription(){
    	return this.description.get();
    }
    
    public void setDescription(String description){
    	this.description.set(description);
    }    
    
    public String getType(){
    	return this.type.get();
    }
    
    public void setType(String type){
    	this.type.set(type);
    }    
    
    public String getChemin(){
    	return this.chemin.get();
    }
    
    public void setChemin(String chemin){
    	this.chemin.set(chemin);
    }    
    
    public String getContenu(){
    	return this.contenu.get();
    }
    
    public void setContenu(String contenu){
    	this.contenu.set(contenu);
    }    

    @JsonSerialize(using=InstantSerializer.class)    
    public Instant getDate_document(){
    	return this.date_document.get();
    }

    @JsonDeserialize(using=InstantDeserializer.class)    
    public void setDate_document(Instant date_document){
    	this.date_document.set(date_document);
    }     

    
    public String getDigueID(){
    	return this.digueID.get();
    }

    public void setDigueID(String digueID){
    	this.digueID.set( digueID );
    }
 

    
    public String getIDAssocie(){
    	return this.IDAssocie.get();
    }

    public void setIDAssocie(String IDAssocie){
    	this.IDAssocie.set( IDAssocie );
    }
 

    
    public String getStructureId(){
    	return this.structureId.get();
    }

    public void setStructureId(String structureId){
    	this.structureId.set( structureId );
    }

  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Document ");
      builder.append("nom: ");
      builder.append(nom.get());
      builder.append(", ");
      builder.append("taille: ");
      builder.append(taille.get());
      builder.append(", ");
      builder.append("description: ");
      builder.append(description.get());
      builder.append(", ");
      builder.append("type: ");
      builder.append(type.get());
      builder.append(", ");
      builder.append("chemin: ");
      builder.append(chemin.get());
      builder.append(", ");
      builder.append("contenu: ");
      builder.append(contenu.get());
      builder.append(", ");
      builder.append("date_document: ");
      builder.append(date_document.get());
      return builder.toString();
  }


}

