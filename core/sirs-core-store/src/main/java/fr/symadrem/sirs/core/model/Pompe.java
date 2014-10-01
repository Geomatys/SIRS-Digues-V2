
package fr.symadrem.sirs.core.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Pompe  extends CouchDbDocument  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for marque.
    */
    private StringProperty  marque = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on marque.
    */
    public  StringProperty marqueProperty() {
       return marque;
    }
    /**
    * JavaFX property for puissance.
    */
    private FloatProperty  puissance = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on puissance.
    */
    public  FloatProperty puissanceProperty() {
       return puissance;
    }
    /**
    * JavaFX property for debit.
    */
    private FloatProperty  debit = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on debit.
    */
    public  FloatProperty debitProperty() {
       return debit;
    }
    /**
    * JavaFX property for hauteur_refoulement.
    */
    private FloatProperty  hauteur_refoulement = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on hauteur_refoulement.
    */
    public  FloatProperty hauteur_refoulementProperty() {
       return hauteur_refoulement;
    }
    //
    // References
    //
      
    public String getMarque(){
    	return this.marque.get();
    }
    
    public void setMarque(String marque){
    	this.marque.set(marque);
    }
        
    public float getPuissance(){
    	return this.puissance.get();
    }
    
    public void setPuissance(float puissance){
    	this.puissance.set(puissance);
    }
        
    public float getDebit(){
    	return this.debit.get();
    }
    
    public void setDebit(float debit){
    	this.debit.set(debit);
    }
        
    public float getHauteur_refoulement(){
    	return this.hauteur_refoulement.get();
    }
    
    public void setHauteur_refoulement(float hauteur_refoulement){
    	this.hauteur_refoulement.set(hauteur_refoulement);
    }
    

 
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Pompe ");
      builder.append("marque: ");
      builder.append(marque.get());
      builder.append(", ");
      builder.append("puissance: ");
      builder.append(puissance.get());
      builder.append(", ");
      builder.append("debit: ");
      builder.append(debit.get());
      builder.append(", ");
      builder.append("hauteur_refoulement: ");
      builder.append(hauteur_refoulement.get());
      return builder.toString();
  }


}

