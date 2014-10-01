
package fr.symadrem.sirs.core.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
@SuppressWarnings("serial")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class TalusDigue  extends Structure  {
    //
    // Attributes.
    //  
    /**
    * JavaFX property for num_couche.
    */
    private IntegerProperty  num_couche = new SimpleIntegerProperty();
    
    /**
    * Getter for JavaFX property on num_couche.
    */
    public  IntegerProperty num_coucheProperty() {
       return num_couche;
    }
    /**
    * JavaFX property for fonction_haut.
    */
    private StringProperty  fonction_haut = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on fonction_haut.
    */
    public  StringProperty fonction_hautProperty() {
       return fonction_haut;
    }
    /**
    * JavaFX property for fonction_bas.
    */
    private StringProperty  fonction_bas = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on fonction_bas.
    */
    public  StringProperty fonction_basProperty() {
       return fonction_bas;
    }
    /**
    * JavaFX property for materiau_haut.
    */
    private StringProperty  materiau_haut = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on materiau_haut.
    */
    public  StringProperty materiau_hautProperty() {
       return materiau_haut;
    }
    /**
    * JavaFX property for materiau_bas.
    */
    private StringProperty  materiau_bas = new SimpleStringProperty();
    
    /**
    * Getter for JavaFX property on materiau_bas.
    */
    public  StringProperty materiau_basProperty() {
       return materiau_bas;
    }
    /**
    * JavaFX property for epaisseur_sommet.
    */
    private FloatProperty  epaisseur_sommet = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on epaisseur_sommet.
    */
    public  FloatProperty epaisseur_sommetProperty() {
       return epaisseur_sommet;
    }
    /**
    * JavaFX property for longueur_rampart_haut.
    */
    private FloatProperty  longueur_rampart_haut = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on longueur_rampart_haut.
    */
    public  FloatProperty longueur_rampart_hautProperty() {
       return longueur_rampart_haut;
    }
    /**
    * JavaFX property for longueur_rampart_bas.
    */
    private FloatProperty  longueur_rampart_bas = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on longueur_rampart_bas.
    */
    public  FloatProperty longueur_rampart_basProperty() {
       return longueur_rampart_bas;
    }
    /**
    * JavaFX property for pente_interieur.
    */
    private FloatProperty  pente_interieur = new SimpleFloatProperty();
    
    /**
    * Getter for JavaFX property on pente_interieur.
    */
    public  FloatProperty pente_interieurProperty() {
       return pente_interieur;
    }
    //
    // References
    //
      
    public int getNum_couche(){
    	return this.num_couche.get();
    }
    
    public void setNum_couche(int num_couche){
    	this.num_couche.set(num_couche);
    }
        
    public String getFonction_haut(){
    	return this.fonction_haut.get();
    }
    
    public void setFonction_haut(String fonction_haut){
    	this.fonction_haut.set(fonction_haut);
    }
        
    public String getFonction_bas(){
    	return this.fonction_bas.get();
    }
    
    public void setFonction_bas(String fonction_bas){
    	this.fonction_bas.set(fonction_bas);
    }
        
    public String getMateriau_haut(){
    	return this.materiau_haut.get();
    }
    
    public void setMateriau_haut(String materiau_haut){
    	this.materiau_haut.set(materiau_haut);
    }
        
    public String getMateriau_bas(){
    	return this.materiau_bas.get();
    }
    
    public void setMateriau_bas(String materiau_bas){
    	this.materiau_bas.set(materiau_bas);
    }
        
    public float getEpaisseur_sommet(){
    	return this.epaisseur_sommet.get();
    }
    
    public void setEpaisseur_sommet(float epaisseur_sommet){
    	this.epaisseur_sommet.set(epaisseur_sommet);
    }
        
    public float getLongueur_rampart_haut(){
    	return this.longueur_rampart_haut.get();
    }
    
    public void setLongueur_rampart_haut(float longueur_rampart_haut){
    	this.longueur_rampart_haut.set(longueur_rampart_haut);
    }
        
    public float getLongueur_rampart_bas(){
    	return this.longueur_rampart_bas.get();
    }
    
    public void setLongueur_rampart_bas(float longueur_rampart_bas){
    	this.longueur_rampart_bas.set(longueur_rampart_bas);
    }
        
    public float getPente_interieur(){
    	return this.pente_interieur.get();
    }
    
    public void setPente_interieur(float pente_interieur){
    	this.pente_interieur.set(pente_interieur);
    }
    

 
  
  @Override
  public String toString(){
      StringBuilder builder = new StringBuilder("[TalusDigue ");
      builder.append("num_couche: ");
      builder.append(num_couche.get());
      builder.append(", ");
      builder.append("fonction_haut: ");
      builder.append(fonction_haut.get());
      builder.append(", ");
      builder.append("fonction_bas: ");
      builder.append(fonction_bas.get());
      builder.append(", ");
      builder.append("materiau_haut: ");
      builder.append(materiau_haut.get());
      builder.append(", ");
      builder.append("materiau_bas: ");
      builder.append(materiau_bas.get());
      builder.append(", ");
      builder.append("epaisseur_sommet: ");
      builder.append(epaisseur_sommet.get());
      builder.append(", ");
      builder.append("longueur_rampart_haut: ");
      builder.append(longueur_rampart_haut.get());
      builder.append(", ");
      builder.append("longueur_rampart_bas: ");
      builder.append(longueur_rampart_bas.get());
      builder.append(", ");
      builder.append("pente_interieur: ");
      builder.append(pente_interieur.get());
      return builder.toString();
  }


}

