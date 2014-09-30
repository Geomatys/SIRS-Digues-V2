
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
public class FrancBord  extends Structure  {


   
        
    public FloatProperty  largeur = new SimpleFloatProperty();
    

  
  
      
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

