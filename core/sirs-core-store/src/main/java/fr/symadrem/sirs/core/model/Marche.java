
package fr.symadrem.sirs.core.model;

import com.geomatys.json.LocalDateTimeDeserializer;
import com.geomatys.json.LocalDateTimeSerializer;
import java.time.LocalDateTime;
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
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Marche  extends Document  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for maitre_ouvrage.
    */
    private StringProperty  maitre_ouvrage = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on maitre_ouvrage.
    */
    public  StringProperty maitre_ouvrageProperty() {
       return maitre_ouvrage;
    }
    /**
    * JavaFX property for date_debut.
    */
    private ObjectProperty<LocalDateTime>  date_debut = new SimpleObjectProperty<LocalDateTime>();
    
    /**
    * Getter for JavaFX property on date_debut.
    */
    public  ObjectProperty<LocalDateTime> date_debutProperty() {
       return date_debut;
    }
    /**
    * JavaFX property for date_fin.
    */
    private ObjectProperty<LocalDateTime>  date_fin = new SimpleObjectProperty<LocalDateTime>();
    
    /**
    * Getter for JavaFX property on date_fin.
    */
    public  ObjectProperty<LocalDateTime> date_finProperty() {
       return date_fin;
    }
    /**
    * JavaFX property for montant.
    */
    private FloatProperty  montant = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on montant.
    */
    public  FloatProperty montantProperty() {
       return montant;
    }
    /**
    * JavaFX property for num_operation.
    */
    private IntegerProperty  num_operation = new SimpleIntegerProperty();
    
    /**
    * Getter for JavaFX property on num_operation.
    */
    public  IntegerProperty num_operationProperty() {
       return num_operation;
    }
    //
    // References
    // 
    private ObservableList<String> maitreOeuvreIds = FXCollections.observableArrayList();
 
    private ObservableList<String> financeurIds = FXCollections.observableArrayList();
 
    private StringProperty prestationId = new SimpleStringProperty();
 

    
    public String getMaitre_ouvrage(){
    	return this.maitre_ouvrage.get();
    }
    
    public void setMaitre_ouvrage(String maitre_ouvrage){
    	this.maitre_ouvrage.set(maitre_ouvrage);
    }    

    @JsonSerialize(using=LocalDateTimeSerializer.class)    
    public LocalDateTime getDate_debut(){
    	return this.date_debut.get();
    }

    @JsonDeserialize(using=LocalDateTimeDeserializer.class)    
    public void setDate_debut(LocalDateTime date_debut){
    	this.date_debut.set(date_debut);
    }    

    @JsonSerialize(using=LocalDateTimeSerializer.class)    
    public LocalDateTime getDate_fin(){
    	return this.date_fin.get();
    }

    @JsonDeserialize(using=LocalDateTimeDeserializer.class)    
    public void setDate_fin(LocalDateTime date_fin){
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

    

    public List<String> getMaitreOeuvreIds(){
    	return this.maitreOeuvreIds;
    }


    public void setMaitreOeuvreIds(List<String> maitreOeuvreIds){
        this.maitreOeuvreIds.clear();
    	this.maitreOeuvreIds.addAll(maitreOeuvreIds);
    }
 

    

    public List<String> getFinanceurIds(){
    	return this.financeurIds;
    }


    public void setFinanceurIds(List<String> financeurIds){
        this.financeurIds.clear();
    	this.financeurIds.addAll(financeurIds);
    }
 

    
    public String getPrestationId(){
    	return this.prestationId.get();
    }

    public void setPrestationId(String prestationId){
    	this.prestationId.set( prestationId );
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

