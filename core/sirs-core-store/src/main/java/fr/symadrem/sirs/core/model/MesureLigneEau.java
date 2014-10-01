
package fr.symadrem.sirs.core.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class MesureLigneEau  extends Positionable  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for hauteur.
    */
    private FloatProperty  hauteur = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on hauteur.
    */
    public  FloatProperty hauteurProperty() {
       return hauteur;
    }
    //
    // References
    // 

    
    public float getHauteur(){
    	return this.hauteur.get();
    }
    
    public void setHauteur(float hauteur){
    	this.hauteur.set(hauteur);
    }    
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[MesureLigneEau ");
      builder.append("hauteur: ");
      builder.append(hauteur.get());
      return builder.toString();
  }


}

