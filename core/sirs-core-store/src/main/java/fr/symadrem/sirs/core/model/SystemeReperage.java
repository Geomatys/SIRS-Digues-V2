
package fr.symadrem.sirs.core.model;

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
public class SystemeReperage  extends CouchDbDocument  {


   
        
    public ObjectProperty<java.util.Date>  date_debut = new SimpleObjectProperty<java.util.Date>();
        
    public ObjectProperty<java.util.Date>  date_fin = new SimpleObjectProperty<java.util.Date>();
        
    public StringProperty  lineaire = new SimpleStringProperty();
        
    public StringProperty  nom = new SimpleStringProperty();
    
 
    //
    // References
    //
    private String troncon_digueId;
    
  
  
      
    public java.util.Date getDate_debut(){
    	return this.date_debut.get();
    }
    
    public void setDate_debut(java.util.Date date_debut){
    	this.date_debut.set(date_debut);
    }
        
    public java.util.Date getDate_fin(){
    	return this.date_fin.get();
    }
    
    public void setDate_fin(java.util.Date date_fin){
    	this.date_fin.set(date_fin);
    }
        
    public String getLineaire(){
    	return this.lineaire.get();
    }
    
    public void setLineaire(String lineaire){
    	this.lineaire.set(lineaire);
    }
        
    public String getNom(){
    	return this.nom.get();
    }
    
    public void setNom(String nom){
    	this.nom.set(nom);
    }
    

  
  
    
    public String getTroncon_digue(){
    	return this.troncon_digueId;
    }
    
    public void setTroncon_digue(String troncon_digueId){
    	this.troncon_digueId = troncon_digueId;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[SystemeReperage ");
      builder.append("date_debut: ");
      builder.append(date_debut.get());
      builder.append(", ");
      builder.append("date_fin: ");
      builder.append(date_fin.get());
      builder.append(", ");
      builder.append("lineaire: ");
      builder.append(lineaire.get());
      builder.append(", ");
      builder.append("nom: ");
      builder.append(nom.get());
      return builder.toString();
  }


}

