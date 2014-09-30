
package fr.symadrem.sirs.core.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Fondation  extends Structure  {


   
        
    private IntegerProperty  num_couche = new SimpleIntegerProperty();
        
    private StringProperty  fonction = new SimpleStringProperty();
        
    private StringProperty  materiau = new SimpleStringProperty();
        
    private FloatProperty  epaisseur = new SimpleFloatProperty();
    

  
  
      
    public int getNum_couche(){
    	return this.num_couche.get();
    }
    
    public void setNum_couche(int num_couche){
    	this.num_couche.set(num_couche);
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
        
    public float getEpaisseur(){
    	return this.epaisseur.get();
    }
    
    public void setEpaisseur(float epaisseur){
    	this.epaisseur.set(epaisseur);
    }
    

 
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Fondation ");
      builder.append("num_couche: ");
      builder.append(num_couche.get());
      builder.append(", ");
      builder.append("fonction: ");
      builder.append(fonction.get());
      builder.append(", ");
      builder.append("materiau: ");
      builder.append(materiau.get());
      builder.append(", ");
      builder.append("epaisseur: ");
      builder.append(epaisseur.get());
      return builder.toString();
  }


}

