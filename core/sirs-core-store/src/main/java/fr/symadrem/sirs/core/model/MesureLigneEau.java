
package fr.symadrem.sirs.core.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class MesureLigneEau  extends Positionable  {


   
        
    public FloatProperty  hauteur = new SimpleFloatProperty();
    

  
  
      
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

