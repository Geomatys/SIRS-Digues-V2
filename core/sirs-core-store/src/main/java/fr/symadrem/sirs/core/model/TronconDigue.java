
package fr.symadrem.sirs.core.model;

import com.geomatys.json.GeometryDeserializer;
import com.geomatys.json.GeometrySerializer;
import com.geomatys.json.LocalDateTimeDeserializer;
import com.geomatys.json.LocalDateTimeSerializer;
import com.vividsolutions.jts.geom.Geometry;
import java.time.LocalDateTime;
import java.util.List;
import javafx.beans.property.ObjectProperty;
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
public class TronconDigue  extends Positionable  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for libelle.
    */
    private StringProperty  libelle = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on libelle.
    */
    public  StringProperty libelleProperty() {
       return libelle;
    }
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
    * JavaFX property for date_maj.
    */
    private ObjectProperty<LocalDateTime>  date_maj = new SimpleObjectProperty<LocalDateTime>();
    
    /**
    * Getter for JavaFX property on date_maj.
    */
    public  ObjectProperty<LocalDateTime> date_majProperty() {
       return date_maj;
    }
    /**
    * JavaFX property for rive.
    */
    private StringProperty  rive = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on rive.
    */
    public  StringProperty riveProperty() {
       return rive;
    }
    /**
    * JavaFX property for systeme_reperage_defaut.
    */
    private StringProperty  systeme_reperage_defaut = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on systeme_reperage_defaut.
    */
    public  StringProperty systeme_reperage_defautProperty() {
       return systeme_reperage_defaut;
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
    public ObservableList<Structure>  stuctures =  FXCollections.observableArrayList() ; 
    public ObjectProperty<SystemeReperage>  systeme_rep_defaut = new SimpleObjectProperty<>() ; 
    private StringProperty contactId = new SimpleStringProperty();
 
    public ObservableList<BorneDigue>  borneIds =  FXCollections.observableArrayList() ; 
    private StringProperty digueId = new SimpleStringProperty();
 

    
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

    @JsonSerialize(using=LocalDateTimeSerializer.class)    
    public LocalDateTime getDate_maj(){
    	return this.date_maj.get();
    }

    @JsonDeserialize(using=LocalDateTimeDeserializer.class)    
    public void setDate_maj(LocalDateTime date_maj){
    	this.date_maj.set(date_maj);
    }    
    
    public String getRive(){
    	return this.rive.get();
    }
    
    public void setRive(String rive){
    	this.rive.set(rive);
    }    
    
    public String getSysteme_reperage_defaut(){
    	return this.systeme_reperage_defaut.get();
    }
    
    public void setSysteme_reperage_defaut(String systeme_reperage_defaut){
    	this.systeme_reperage_defaut.set(systeme_reperage_defaut);
    }    

    @JsonSerialize(using=GeometrySerializer.class)    
    public Geometry getGeometry(){
    	return this.geometry.get();
    }

    @JsonDeserialize(using=GeometryDeserializer.class)    
    public void setGeometry(Geometry geometry){
    	this.geometry.set(geometry);
    }     
   public List<Structure> getStuctures(){
    	return this.stuctures;
    }

    public void setStuctures(List<Structure> stuctures){
        this.stuctures.clear();
    	this.stuctures.addAll( stuctures );
    }
 
   public SystemeReperage getSysteme_rep_defaut(){
    	return this.systeme_rep_defaut.get();
    }

    public void setSysteme_rep_defaut(SystemeReperage systeme_rep_defaut){
    	this.systeme_rep_defaut.set( systeme_rep_defaut );
    }
 

    
    public String getContactId(){
    	return this.contactId.get();
    }

    public void setContactId(String contactId){
    	this.contactId.set( contactId );
    }
 
   public List<BorneDigue> getBorneIds(){
    	return this.borneIds;
    }

    public void setBorneIds(List<BorneDigue> borneIds){
        this.borneIds.clear();
    	this.borneIds.addAll( borneIds );
    }
 

    
    public String getDigueId(){
    	return this.digueId.get();
    }

    public void setDigueId(String digueId){
    	this.digueId.set( digueId );
    }

  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[TronconDigue ");
      builder.append("libelle: ");
      builder.append(libelle.get());
      builder.append(", ");
      builder.append("commentaire: ");
      builder.append(commentaire.get());
      builder.append(", ");
      builder.append("date_debut: ");
      builder.append(date_debut.get());
      builder.append(", ");
      builder.append("date_fin: ");
      builder.append(date_fin.get());
      builder.append(", ");
      builder.append("date_maj: ");
      builder.append(date_maj.get());
      builder.append(", ");
      builder.append("rive: ");
      builder.append(rive.get());
      builder.append(", ");
      builder.append("systeme_reperage_defaut: ");
      builder.append(systeme_reperage_defaut.get());
      builder.append(", ");
      builder.append("geometry: ");
      builder.append(geometry.get());
      return builder.toString();
  }


}

