
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class LigneEau  extends Structure  {


   
        
    public StringProperty  reference_hauteur = new SimpleStringProperty();
    
 
    //
    // References
    //
    private List<String> articles_journauxIds;
     
    //
    // References
    //
    private String evenementhydrauliqueId;
     
    //
    // References
    //
    private List<String> mesuresIds;
    
  
  
      
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
    	this.articles_journauxIds = articles_journauxIds;
    }
   
  
    
    public String getEvenementhydraulique(){
    	return this.evenementhydrauliqueId;
    }
    
    public void setEvenementhydraulique(String evenementhydrauliqueId){
    	this.evenementhydrauliqueId = evenementhydrauliqueId;
    }
   
  
    
    public List<String> getMesuresIds(){
    	return this.mesuresIds;
    }
    
    public void setMesuresIds(List<String> mesuresIds){
    	this.mesuresIds = mesuresIds;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[LigneEau ");
      builder.append("reference_hauteur: ");
      builder.append(reference_hauteur.get());
      return builder.toString();
  }


}

