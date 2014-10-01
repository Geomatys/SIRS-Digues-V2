
package fr.symadrem.sirs.core.model;

import java.util.Date;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Digue  extends CouchDbDocument  {
    //
    // Attributes.
    //      
    public StringProperty  libelle = new SimpleStringProperty();
        
    public StringProperty  commentaire = new SimpleStringProperty();
        
    public ObjectProperty<Date>  date_maj = new SimpleObjectProperty<Date>();
    
    //
    // References
    // 
    private List<String> tronconsIds;
     
    private String documentId;
    
      
    public String getLibelle(){
    	return this.libelle.get();
    }
    
    public void setLibelle(String libelle){
    	this.libelle.set(libelle);
    }
        
    public String getCommentaire(){
    	return this.commentaire.get();
    }
    
    public void setCommentaire(String commentaire){
    	this.commentaire.set(commentaire);
    }
        
    public Date getDate_maj(){
    	return this.date_maj.get();
    }
    
    public void setDate_maj(Date date_maj){
    	this.date_maj.set(date_maj);
    }
    

  
  
    
    public List<String> getTronconsIds(){
    	return this.tronconsIds;
    }
    
    public void setTronconsIds(List<String> tronconsIds){
    	this.tronconsIds = tronconsIds;
    }
   
  
    
    public String getDocument(){
    	return this.documentId;
    }
    
    public void setDocument(String documentId){
    	this.documentId = documentId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Digue ");
      builder.append("libelle: ");
      builder.append(libelle.get());
      builder.append(", ");
      builder.append("commentaire: ");
      builder.append(commentaire.get());
      builder.append(", ");
      builder.append("date_maj: ");
      builder.append(date_maj.get());
      return builder.toString();
  }


}

