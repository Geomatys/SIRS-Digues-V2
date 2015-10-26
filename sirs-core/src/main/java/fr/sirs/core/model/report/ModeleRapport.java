

package fr.sirs.core.model.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.sirs.core.model.AvecLibelle;
import fr.sirs.core.model.Element;
import fr.sirs.util.property.Internal;
import java.util.List;
import javafx.beans.property.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.*;
import org.ektorp.support.*;

@JsonInclude(Include.NON_EMPTY)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@SuppressWarnings("serial")
public class ModeleRapport  extends CouchDbDocument
    implements Element, AvecLibelle {

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

    private final StringProperty  author = new SimpleStringProperty();
    @Override
    public StringProperty authorProperty() {
       return author;
    }

    private final BooleanProperty  valid = new SimpleBooleanProperty();
    @Override
    public BooleanProperty validProperty() {
       return valid;
    }

    private final StringProperty  libelle = new SimpleStringProperty();
    @Override
    public StringProperty libelleProperty() {
       return libelle;
    }

    public ObservableList<AbstractSectionRapport>  sections = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    {
        sections.addListener(new ListChangeListener<AbstractSectionRapport>() {

            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends AbstractSectionRapport> event) {
                while(event.next()){
                    for (AbstractSectionRapport element : event.getAddedSubList()) {
                        element.setParent(ModeleRapport.this);
                    }
                }
            }
        });
    }


    @Override
    public String getDesignation(){
        return this.designation.get();
    }


    @Override
    public void setDesignation(String designation){
        this.designation.set(designation);
    }

    @Override
    public String getAuthor(){
        return this.author.get();
    }


    @Override
    public void setAuthor(String author){
        this.author.set(author);
    }

    @Override
    public boolean getValid(){
        return this.valid.get();
    }


    @Override
    public void setValid(boolean valid){
        this.valid.set(valid);
    }

    @Override
    public String getLibelle(){
        return this.libelle.get();
    }


    @Override
    public void setLibelle(String libelle){
        this.libelle.set(libelle);
    }

    @Internal
    @JsonManagedReference("parent")
    public ObservableList<AbstractSectionRapport> getSections() {
        return this.sections;
    }

    public void setSections(List<AbstractSectionRapport> section) {
        this.sections.clear();
        this.sections.addAll(section);
    }

    @Override
    public ModeleRapport copy() {

        ModeleRapport copy = new ModeleRapport();

        copy.setDesignation(getDesignation());
        copy.setAuthor(getAuthor());
        copy.setValid(getValid());
        copy.setLibelle(getLibelle());

        ObservableList<AbstractSectionRapport> list = FXCollections.observableArrayList();
        for(AbstractSectionRapport element: getSections()){
            list.add((AbstractSectionRapport)element.copy());
        }
        copy.setSections(list);

        return copy;
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
        ModeleRapport other = (ModeleRapport) obj;
        if (getId() != null) {
            return getId().equals(other.getId()); // TODO : check revision ?
        } else {
            return contentBasedEquals(other);
        }
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder("[RapportModeleObligationReglementaire ");
        builder.append("designation: ");
        builder.append(getDesignation());
        builder.append(", ");
        builder.append("author: ");
        builder.append(getAuthor());
        builder.append(", ");
        builder.append("valid: ");
        builder.append(getValid());
        builder.append(", ");
        builder.append("libelle: ");
        builder.append(getLibelle());
        return builder.toString();
    }

    @Override
    public boolean contentBasedEquals(Element element) {
        if(element instanceof ModeleRapport) {

            final ModeleRapport other = (ModeleRapport) element;
            if (getId() == null) {
                if (other.getId() != null) return false;
            } else if (!getId().equals(other.getId())) return false;
            if ((this.getDesignation()==null ^ other.getDesignation()==null) || ( (this.getDesignation()!=null && other.getDesignation()!=null) && !this.getDesignation().equals(other.getDesignation()))) return false;
            if ((this.getAuthor()==null ^ other.getAuthor()==null) || ( (this.getAuthor()!=null && other.getAuthor()!=null) && !this.getAuthor().equals(other.getAuthor()))) return false;
            if (this.getValid() != other.getValid()) return false;
            if ((this.getLibelle()==null ^ other.getLibelle()==null) || ( (this.getLibelle()!=null && other.getLibelle()!=null) && !this.getLibelle().equals(other.getLibelle()))) return false;
            return true;
        }
        return false;
    }

    @Override
    public boolean removeChild(final Element toRemove) {
        if (toRemove == null) return false;
        if (getSections().remove(toRemove)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean addChild(final Element toAdd) {
        if (toAdd == null) return false;
        if (toAdd instanceof AbstractSectionRapport) {
            getSections().add((AbstractSectionRapport) toAdd);
            return true;
        }
        return false;
    }

    @Override
    public Element getChildById(final String toSearch) {
        if (toSearch == null) return null;
        if (getId() != null && getId().equals(toSearch)) return this;
        Element result = null;
        for (final AbstractSectionRapport e : sections) {
            if ((result = e.getChildById(toSearch)) != null) return result;
        }
        return result;
    }

}

