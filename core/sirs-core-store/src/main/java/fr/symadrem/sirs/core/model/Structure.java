
package fr.symadrem.sirs.core.model;

import com.geomatys.json.LocalDateTimeDeserializer;
import com.geomatys.json.LocalDateTimeSerializer;
import com.vividsolutions.jts.geom.Geometry;
import java.time.LocalDateTime;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class Structure  extends Positionable  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for commentaire.
    */
    private StringProperty  commentaire = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on commentaire.
    */
    public  StringProperty commentaireProperty() {
       return commentaire;
    }
    /**
    * JavaFX property for cote.
    */
    private StringProperty  cote = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on cote.
    */
    public  StringProperty coteProperty() {
       return cote;
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
    * JavaFX property for source.
    */
    private StringProperty  source = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on source.
    */
    public  StringProperty sourceProperty() {
       return source;
    }
    /**
    * JavaFX property for geometry.
    */
    private ObjectProperty<Geometry>  geometry = new SimpleObjectProperty<Geometry>();
    
    /**
    * Getter for JavaFX property on geometry.
    */
    public  ObjectProperty<Geometry> geometryProperty() {
       return geometry;
    }
    //
    // References
    // 
    private StringProperty troncon = new SimpleStringProperty();
 

    
    public String getCommentaire(){
    	return this.commentaire.get();
    }
    
    public void setCommentaire(String commentaire){
    	this.commentaire.set(commentaire);
    }    
    
    public String getCote(){
    	return this.cote.get();
    }
    
    public void setCote(String cote){
    	this.cote.set(cote);
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
    
    public String getSource(){
    	return this.source.get();
    }
    
    public void setSource(String source){
    	this.source.set(source);
    }    
    
    public Geometry getGeometry(){
    	return this.geometry.get();
    }
    
    public void setGeometry(Geometry geometry){
    	this.geometry.set(geometry);
    }     

    
    public String getTroncon(){
    	return this.troncon.get();
    }

    public void setTroncon(String troncon){
    	this.troncon.set( troncon );
    }

  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[Structure ");
      builder.append("commentaire: ");
      builder.append(commentaire.get());
      builder.append(", ");
      builder.append("cote: ");
      builder.append(cote.get());
      builder.append(", ");
      builder.append("date_debut: ");
      builder.append(date_debut.get());
      builder.append(", ");
      builder.append("date_fin: ");
      builder.append(date_fin.get());
      builder.append(", ");
      builder.append("source: ");
      builder.append(source.get());
      builder.append(", ");
      builder.append("geometry: ");
      builder.append(geometry.get());
      return builder.toString();
  }


}

