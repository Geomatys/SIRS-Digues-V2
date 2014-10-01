
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class MonteeEaux  extends Structure  {
    //
    // Attributes.
    //      
    public StringProperty  echelle_limnimetrique = new SimpleStringProperty();
    
    //
    // References
    // 
    private List<String> articles_journauxIds;
     
    private String evenementhydrauliqueId;
     
    private List<String> mesuresIds;
    
      
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
      StringBuilder builder = new StringBuilder("[MonteeEaux ");
      builder.append("echelle_limnimetrique: ");
      builder.append(echelle_limnimetrique.get());
      return builder.toString();
  }


}

