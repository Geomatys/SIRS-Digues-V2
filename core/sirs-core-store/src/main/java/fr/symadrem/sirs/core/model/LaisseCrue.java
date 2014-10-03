
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class LaisseCrue  extends Structure  {
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
    /**
    * JavaFX property for reference_hauteur.
    */
    private StringProperty  reference_hauteur = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on reference_hauteur.
    */
    public  StringProperty reference_hauteurProperty() {
       return reference_hauteur;
    }
    /**
    * JavaFX property for position_laisse.
    */
    private StringProperty  position_laisse = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on position_laisse.
    */
    public  StringProperty position_laisseProperty() {
       return position_laisse;
    }
    //
    // References
    // 
    private StringProperty observateur = new SimpleStringProperty();
 
    private ObservableList<String> articles_journauxIds = FXCollections.observableArrayList();
 
    private StringProperty evenementId = new SimpleStringProperty();
 

    
    public float getHauteur(){
    	return this.hauteur.get();
    }
    
    public void setHauteur(float hauteur){
    	this.hauteur.set(hauteur);
    }    
    
    public String getReference_hauteur(){
    	return this.reference_hauteur.get();
    }
    
    public void setReference_hauteur(String reference_hauteur){
    	this.reference_hauteur.set(reference_hauteur);
    }    
    
    public String getPosition_laisse(){
    	return this.position_laisse.get();
    }
    
    public void setPosition_laisse(String position_laisse){
    	this.position_laisse.set(position_laisse);
    }     

    
    public String getObservateur(){
    	return this.observateur.get();
    }

    public void setObservateur(String observateur){
    	this.observateur.set( observateur );
    }
 

    

    public List<String> getArticles_journauxIds(){
    	return this.articles_journauxIds;
    }


    public void setArticles_journauxIds(List<String> articles_journauxIds){
        this.articles_journauxIds.clear();
    	this.articles_journauxIds.addAll(articles_journauxIds);
    }
 

    
    public String getEvenementId(){
    	return this.evenementId.get();
    }

    public void setEvenementId(String evenementId){
    	this.evenementId.set( evenementId );
    }

  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[LaisseCrue ");
      builder.append("hauteur: ");
      builder.append(hauteur.get());
      builder.append(", ");
      builder.append("reference_hauteur: ");
      builder.append(reference_hauteur.get());
      builder.append(", ");
      builder.append("position_laisse: ");
      builder.append(position_laisse.get());
      return builder.toString();
  }


}

