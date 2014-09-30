
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
import org.ektorp.support.CouchDbDocument;
@SuppressWarnings("serial")
@JsonIgnoreProperties
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Photo  extends CouchDbDocument  {


   
        
    public StringProperty  cote = new SimpleStringProperty();
        
    public StringProperty  orientation = new SimpleStringProperty();
        
    public StringProperty  photographe = new SimpleStringProperty();
    
 
    public ObservableList<Document>  photo =  FXCollections.observableArrayList() ;
  
  
      
    public String getCote(){
    	return this.cote.get();
    }
    
    public void setCote(String cote){
    	this.cote.set(cote);
    }
        
    public String getOrientation(){
    	return this.orientation.get();
    }
    
    public void setOrientation(String orientation){
    	this.orientation.set(orientation);
    }
        
    public String getPhotographe(){
    	return this.photographe.get();
    }
    
    public void setPhotographe(String photographe){
    	this.photographe.set(photographe);
    }
    

  
   public List<Document> getPhoto(){
    	return this.photo;
    }
    
    public void setPhoto(List<Document> photo){
        this.photo.clear();
    	this.photo.addAll( photo );
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Photo ");
      builder.append("cote: ");
      builder.append(cote.get());
      builder.append(", ");
      builder.append("orientation: ");
      builder.append(orientation.get());
      builder.append(", ");
      builder.append("photographe: ");
      builder.append(photographe.get());
      return builder.toString();
  }


}

