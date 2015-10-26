package fr.sirs.core.model.report;

import fr.sirs.core.model.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.sirs.util.property.Internal;
import java.util.Arrays;
import javafx.beans.property.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.ektorp.support.*;

/**
 * Template ODT pour l'impression d'une fiche à propos d'un objet précis.
 * @author Alexis Manin (Geomatys)
 */
@JsonInclude(Include.NON_EMPTY)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@SuppressWarnings("serial")
public class ModeleElement extends CouchDbDocument
    implements Element , AvecLibelle {

    /**
     * @return the parent {@link Element} of the current object, or itself if its a CouchDB document root node.
     * Can be null for newly created objects which has not been saved in database yet.
     */
    @Override
    @Internal
    @JsonIgnore
    public Element getCouchDBDocument() {
        return this;
    }

    @Override
    @Internal
    @JsonIgnore
    public String getDocumentId() {
        return getId();
    }

    @Override
    @JsonIgnore
    public void setParent(Element parent){
        //
        // NOP
        //
    }

    /**
     * @return the parent {@link Element} of the current element, or null if no parent is set.
     * Note that no CouchDB document has a parent property. Only contained elements have one.
     */
    @Override
    public ObjectProperty<Element> parentProperty() {
        return null;
    }

    /**
     * @return the parent {@link Element} of the current element, or null if no parent is set.
     * Note that no CouchDB document has a parent property. Only contained elements have one.
     */
    @Override
    public Element getParent(){
       return null;
    }

    private final StringProperty  designation = new SimpleStringProperty();
    @Override
    public StringProperty designationProperty() {
       return designation;
    }

    private final BooleanProperty  valid = new SimpleBooleanProperty();
    @Override
    public BooleanProperty validProperty() {
       return valid;
    }

    private final StringProperty  author = new SimpleStringProperty();
    @Override
    public StringProperty authorProperty() {
       return author;
    }

    private final StringProperty  targetClass = new SimpleStringProperty();
    public StringProperty targetClassProperty() {
       return targetClass;
    }

    private final StringProperty  libelle = new SimpleStringProperty();
    @Override
    public StringProperty libelleProperty() {
       return libelle;
    }

    private final ObjectProperty<byte[]>  odt = new SimpleObjectProperty<byte[]>();
    public ObjectProperty<byte[]> odtProperty() {
       return odt;
    }

    public String getDesignation(){
        return this.designation.get();
    }

    public void setDesignation(String designation){
        this.designation.set(designation);
    }

    public boolean getValid(){
        return this.valid.get();
    }


    public void setValid(boolean valid){
        this.valid.set(valid);
    }

    public String getAuthor(){
        return this.author.get();
    }


    public void setAuthor(String author){
        this.author.set(author);
    }

    public String getTargetClass(){
        return this.targetClass.get();
    }


    public void setTargetClass(String fichier){
        this.targetClass.set(fichier);
    }

    public String getLibelle(){
        return this.libelle.get();
    }


    public void setLibelle(String libelle){
        this.libelle.set(libelle);
    }

    public byte[] getOdt(){
        return this.odt.get();
    }


    public void setOdt(byte[] odt){
        this.odt.set(odt);
    }

    @Override
    public ModeleElement copy() {

        ModeleElement templateOdt = new ModeleElement();

        templateOdt.setDesignation(getDesignation());
        templateOdt.setValid(getValid());
        templateOdt.setAuthor(getAuthor());
        templateOdt.setTargetClass(getTargetClass());
        templateOdt.setLibelle(getLibelle());
        templateOdt.setOdt(getOdt());

        return templateOdt;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ModeleElement other = (ModeleElement) obj;
        if (getId() != null) {
            return getId().equals(other.getId()); // TODO : check revision ?
        } else {
            return contentBasedEquals(other);
        }
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder("[TemplateOdt ");
        builder.append("designation: ");
        builder.append(getDesignation());
        builder.append(", ");
        builder.append("valid: ");
        builder.append(getValid());
        builder.append(", ");
        builder.append("author: ");
        builder.append(getAuthor());
        builder.append(", ");
        builder.append("fichier: ");
        builder.append(getTargetClass());
        builder.append(", ");
        builder.append("libelle: ");
        builder.append(getLibelle());
        builder.append(", ");
        builder.append("odt: ");
        builder.append(getOdt());
        return builder.toString();
    }

    @Override
    public boolean contentBasedEquals(Element element) {
        if(element instanceof ModeleElement) {

            final ModeleElement other = (ModeleElement) element;
            if (getId() == null) {
                if (other.getId() != null) return false;
            } else if (!getId().equals(other.getId())) return false;
            if ((this.getDesignation()==null ^ other.getDesignation()==null) || ( (this.getDesignation()!=null && other.getDesignation()!=null) && !this.getDesignation().equals(other.getDesignation()))) return false;
            if (this.getValid() != other.getValid()) return false;
            if ((this.getAuthor()==null ^ other.getAuthor()==null) || ( (this.getAuthor()!=null && other.getAuthor()!=null) && !this.getAuthor().equals(other.getAuthor()))) return false;
            if ((this.getTargetClass()==null ^ other.getTargetClass()==null) || ( (this.getTargetClass()!=null && other.getTargetClass()!=null) && !this.getTargetClass().equals(other.getTargetClass()))) return false;
            if ((this.getLibelle()==null ^ other.getLibelle()==null) || ( (this.getLibelle()!=null && other.getLibelle()!=null) && !this.getLibelle().equals(other.getLibelle()))) return false;
            return !((this.getOdt()==null ^ other.getOdt()==null) || ( (this.getOdt()!=null && other.getOdt()!=null) && !Arrays.equals(this.getOdt(), other.getOdt())));
        }
        return false;
    }

    @Override
    public boolean removeChild(final Element toRemove) {
        if (toRemove == null) return false;
        return false;
    }

    @Override
    public boolean addChild(final Element toAdd) {
        if (toAdd == null) return false;
        return false;
    }

    @Override
    public Element getChildById(final String toSearch) {
        if (toSearch == null) return null;
        if (getId() != null && getId().equals(toSearch)) return this;
        Element result = null;
        return result;
    }

}

