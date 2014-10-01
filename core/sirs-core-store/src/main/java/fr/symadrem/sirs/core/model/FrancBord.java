
package fr.symadrem.sirs.core.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class FrancBord  extends Structure  {
    //
    // Attributes.
    //  
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
    //
    // References
    //
      
    public float getLargeur(){
    	return this.largeur.get();
    }
    
    public void setLargeur(float largeur){
    	this.largeur.set(largeur);
    }
    

 
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[FrancBord ");
      builder.append("largeur: ");
      builder.append(largeur.get());
      return builder.toString();
  }


}

