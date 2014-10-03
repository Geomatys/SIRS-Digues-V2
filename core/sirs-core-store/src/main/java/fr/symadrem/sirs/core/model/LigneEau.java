
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class LigneEau  extends Structure  {
    //
    // Attributes.
    //  
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
    //
    // References
    // 
    private ObservableList<String> articles_journauxIds = FXCollections.observableArrayList();
 
    private ObservableList<String> mesuresIds = FXCollections.observableArrayList();
 
    private StringProperty evenementId = new SimpleStringProperty();
 

    
    public String getReference_hauteur(){
    	return this.reference_hauteur.get();
    }
    
    public void setReference_hauteur(String reference_hauteur){
    	this.reference_hauteur.set(reference_hauteur);
    }     

    

    public List<String> getArticles_journauxIds(){
    	return this.articles_journauxIds;
    }


    public void setArticles_journauxIds(List<String> articles_journauxIds){
        this.articles_journauxIds.clear();
    	this.articles_journauxIds.addAll(articles_journauxIds);
    }
 

    

    public List<String> getMesuresIds(){
    	return this.mesuresIds;
    }


    public void setMesuresIds(List<String> mesuresIds){
        this.mesuresIds.clear();
    	this.mesuresIds.addAll(mesuresIds);
    }
 

    
    public String getEvenementId(){
    	return this.evenementId.get();
    }

    public void setEvenementId(String evenementId){
    	this.evenementId.set( evenementId );
    }

  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[LigneEau ");
      builder.append("reference_hauteur: ");
      builder.append(reference_hauteur.get());
      return builder.toString();
  }


}

