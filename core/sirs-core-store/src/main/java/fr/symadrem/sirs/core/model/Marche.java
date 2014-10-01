
package fr.symadrem.sirs.core.model;

import java.util.List;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
public class Marche  extends Document  {


   
        
    public StringProperty  maitre_ouvrage = new SimpleStringProperty();
        
    public ObjectProperty<java.util.Date>  date_debut = new SimpleObjectProperty<java.util.Date>();
        
    public ObjectProperty<java.util.Date>  date_fin = new SimpleObjectProperty<java.util.Date>();
        
    public FloatProperty  montant = new SimpleFloatProperty();
        
    public IntegerProperty  num_operation = new SimpleIntegerProperty();
    
 
    //
    // References
    //
    private List<String> maitre_oeuvreIds;
     
    //
    // References
    //
    private List<String> financeurIds;
    
  
  
      
    public String getMaitre_ouvrage(){
    	return this.maitre_ouvrage.get();
    }
    
    public void setMaitre_ouvrage(String maitre_ouvrage){
    	this.maitre_ouvrage.set(maitre_ouvrage);
    }
        
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
        
    public float getMontant(){
    	return this.montant.get();
    }
    
    public void setMontant(float montant){
    	this.montant.set(montant);
    }
        
    public int getNum_operation(){
    	return this.num_operation.get();
    }
    
    public void setNum_operation(int num_operation){
    	this.num_operation.set(num_operation);
    }
    

  
  
    
    public List<String> getMaitre_oeuvreIds(){
    	return this.maitre_oeuvreIds;
    }
    
    public void setMaitre_oeuvreIds(List<String> maitre_oeuvreIds){
    	this.maitre_oeuvreIds = maitre_oeuvreIds;
    }
   
  
    
    public List<String> getFinanceurIds(){
    	return this.financeurIds;
    }
    
    public void setFinanceurIds(List<String> financeurIds){
    	this.financeurIds = financeurIds;
    }
  
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Marche ");
      builder.append("maitre_ouvrage: ");
      builder.append(maitre_ouvrage.get());
      builder.append(", ");
      builder.append("date_debut: ");
      builder.append(date_debut.get());
      builder.append(", ");
      builder.append("date_fin: ");
      builder.append(date_fin.get());
      builder.append(", ");
      builder.append("montant: ");
      builder.append(montant.get());
      builder.append(", ");
      builder.append("num_operation: ");
      builder.append(num_operation.get());
      return builder.toString();
  }


}

