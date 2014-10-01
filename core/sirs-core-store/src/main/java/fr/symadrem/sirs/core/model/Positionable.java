
package fr.symadrem.sirs.core.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Positionable  extends CouchDbDocument  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for borne_debut.
    */
    private IntegerProperty  borne_debut = new SimpleIntegerProperty();
    
    /**
    * Getter for JavaFX property on borne_debut.
    */
    public  IntegerProperty borne_debutProperty() {
       return borne_debut;
    }
    /**
    * JavaFX property for borne_debut_aval.
    */
    private BooleanProperty  borne_debut_aval = new SimpleBooleanProperty();
    
    /**
    * Getter for JavaFX property on borne_debut_aval.
    */
    public  BooleanProperty borne_debut_avalProperty() {
       return borne_debut_aval;
    }
    /**
    * JavaFX property for borne_debut_distance.
    */
    private FloatProperty  borne_debut_distance = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on borne_debut_distance.
    */
    public  FloatProperty borne_debut_distanceProperty() {
       return borne_debut_distance;
    }
    /**
    * JavaFX property for borne_fin.
    */
    private IntegerProperty  borne_fin = new SimpleIntegerProperty();
    
    /**
    * Getter for JavaFX property on borne_fin.
    */
    public  IntegerProperty borne_finProperty() {
       return borne_fin;
    }
    /**
    * JavaFX property for borne_fin_aval.
    */
    private BooleanProperty  borne_fin_aval = new SimpleBooleanProperty();
    
    /**
    * Getter for JavaFX property on borne_fin_aval.
    */
    public  BooleanProperty borne_fin_avalProperty() {
       return borne_fin_aval;
    }
    /**
    * JavaFX property for borne_fin_distance.
    */
    private FloatProperty  borne_fin_distance = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on borne_fin_distance.
    */
    public  FloatProperty borne_fin_distanceProperty() {
       return borne_fin_distance;
    }
    /**
    * JavaFX property for position.
    */
    private StringProperty  position = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on position.
    */
    public  StringProperty positionProperty() {
       return position;
    }
    /**
    * JavaFX property for PR_debut.
    */
    private FloatProperty  PR_debut = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on PR_debut.
    */
    public  FloatProperty PR_debutProperty() {
       return PR_debut;
    }
    /**
    * JavaFX property for PR_fin.
    */
    private FloatProperty  PR_fin = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on PR_fin.
    */
    public  FloatProperty PR_finProperty() {
       return PR_fin;
    }
    /**
    * JavaFX property for systeme_rep_id.
    */
    private IntegerProperty  systeme_rep_id = new SimpleIntegerProperty();
    
    /**
    * Getter for JavaFX property on systeme_rep_id.
    */
    public  IntegerProperty systeme_rep_idProperty() {
       return systeme_rep_id;
    }
    //
    // References
    //
      
    public int getBorne_debut(){
    	return this.borne_debut.get();
    }
    
    public void setBorne_debut(int borne_debut){
    	this.borne_debut.set(borne_debut);
    }
        
    public boolean getBorne_debut_aval(){
    	return this.borne_debut_aval.get();
    }
    
    public void setBorne_debut_aval(boolean borne_debut_aval){
    	this.borne_debut_aval.set(borne_debut_aval);
    }
        
    public float getBorne_debut_distance(){
    	return this.borne_debut_distance.get();
    }
    
    public void setBorne_debut_distance(float borne_debut_distance){
    	this.borne_debut_distance.set(borne_debut_distance);
    }
        
    public int getBorne_fin(){
    	return this.borne_fin.get();
    }
    
    public void setBorne_fin(int borne_fin){
    	this.borne_fin.set(borne_fin);
    }
        
    public boolean getBorne_fin_aval(){
    	return this.borne_fin_aval.get();
    }
    
    public void setBorne_fin_aval(boolean borne_fin_aval){
    	this.borne_fin_aval.set(borne_fin_aval);
    }
        
    public float getBorne_fin_distance(){
    	return this.borne_fin_distance.get();
    }
    
    public void setBorne_fin_distance(float borne_fin_distance){
    	this.borne_fin_distance.set(borne_fin_distance);
    }
        
    public String getPosition(){
    	return this.position.get();
    }
    
    public void setPosition(String position){
    	this.position.set(position);
    }
        
    public float getPR_debut(){
    	return this.PR_debut.get();
    }
    
    public void setPR_debut(float PR_debut){
    	this.PR_debut.set(PR_debut);
    }
        
    public float getPR_fin(){
    	return this.PR_fin.get();
    }
    
    public void setPR_fin(float PR_fin){
    	this.PR_fin.set(PR_fin);
    }
        
    public int getSysteme_rep_id(){
    	return this.systeme_rep_id.get();
    }
    
    public void setSysteme_rep_id(int systeme_rep_id){
    	this.systeme_rep_id.set(systeme_rep_id);
    }
    

 
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Positionable ");
      builder.append("borne_debut: ");
      builder.append(borne_debut.get());
      builder.append(", ");
      builder.append("borne_debut_aval: ");
      builder.append(borne_debut_aval.get());
      builder.append(", ");
      builder.append("borne_debut_distance: ");
      builder.append(borne_debut_distance.get());
      builder.append(", ");
      builder.append("borne_fin: ");
      builder.append(borne_fin.get());
      builder.append(", ");
      builder.append("borne_fin_aval: ");
      builder.append(borne_fin_aval.get());
      builder.append(", ");
      builder.append("borne_fin_distance: ");
      builder.append(borne_fin_distance.get());
      builder.append(", ");
      builder.append("position: ");
      builder.append(position.get());
      builder.append(", ");
      builder.append("PR_debut: ");
      builder.append(PR_debut.get());
      builder.append(", ");
      builder.append("PR_fin: ");
      builder.append(PR_fin.get());
      builder.append(", ");
      builder.append("systeme_rep_id: ");
      builder.append(systeme_rep_id.get());
      return builder.toString();
  }


}

