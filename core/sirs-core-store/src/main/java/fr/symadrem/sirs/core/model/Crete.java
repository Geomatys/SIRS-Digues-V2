
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
public class Crete  extends Structure  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for epaisseur.
    */
    private FloatProperty  epaisseur = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on epaisseur.
    */
    public  FloatProperty epaisseurProperty() {
       return epaisseur;
    }
    /**
    * JavaFX property for fonction.
    */
    private StringProperty  fonction = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on fonction.
    */
    public  StringProperty fonctionProperty() {
       return fonction;
    }
    /**
    * JavaFX property for materiau.
    */
    private StringProperty  materiau = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on materiau.
    */
    public  StringProperty materiauProperty() {
       return materiau;
    }
    /**
    * JavaFX property for num_couche.
    */
    private IntegerProperty  num_couche = new SimpleIntegerProperty();
    
    /**
    * Getter for JavaFX property on num_couche.
    */
    public  IntegerProperty num_coucheProperty() {
       return num_couche;
    }
    //
    // References
    //
      
    public float getEpaisseur(){
    	return this.epaisseur.get();
    }
    
    public void setEpaisseur(float epaisseur){
    	this.epaisseur.set(epaisseur);
    }
        
    public String getFonction(){
    	return this.fonction.get();
    }
    
    public void setFonction(String fonction){
    	this.fonction.set(fonction);
    }
        
    public String getMateriau(){
    	return this.materiau.get();
    }
    
    public void setMateriau(String materiau){
    	this.materiau.set(materiau);
    }
        
    public int getNum_couche(){
    	return this.num_couche.get();
    }
    
    public void setNum_couche(int num_couche){
    	this.num_couche.set(num_couche);
    }
    

 
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Crete ");
      builder.append("epaisseur: ");
      builder.append(epaisseur.get());
      builder.append(", ");
      builder.append("fonction: ");
      builder.append(fonction.get());
      builder.append(", ");
      builder.append("materiau: ");
      builder.append(materiau.get());
      builder.append(", ");
      builder.append("num_couche: ");
      builder.append(num_couche.get());
      return builder.toString();
  }


}

