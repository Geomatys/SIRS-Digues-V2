
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class MonteeEaux  extends Structure  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for echelle_limnimetrique.
    */
    private StringProperty  echelle_limnimetrique = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on echelle_limnimetrique.
    */
    public  StringProperty echelle_limnimetriqueProperty() {
       return echelle_limnimetrique;
    }
    //
    // References
    // 
    private ObservableList<String> articles_journauxIds = FXCollections.observableArrayList();
 
    private ObservableList<String> mesuresIds = FXCollections.observableArrayList();
 
    private StringProperty evenementId = new SimpleStringProperty();
 

    
    public String getEchelle_limnimetrique(){
    	return this.echelle_limnimetrique.get();
    }
    
    public void setEchelle_limnimetrique(String echelle_limnimetrique){
    	this.echelle_limnimetrique.set(echelle_limnimetrique);
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
      StringBuilder builder = new StringBuilder("[MonteeEaux ");
      builder.append("echelle_limnimetrique: ");
      builder.append(echelle_limnimetrique.get());
      return builder.toString();
  }


}

