

package fr.sirs.core.model.report;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.sirs.core.model.AvecLibelle;
import fr.sirs.core.model.Element;
import fr.sirs.util.property.Internal;
import fr.sirs.util.property.Reference;
import java.util.Iterator;
import java.util.UUID;
import javafx.beans.property.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.sis.util.ArgumentChecks;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.text.Paragraph;

@JsonInclude(Include.NON_EMPTY)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@SuppressWarnings("serial")
public abstract class AbstractSectionRapport implements Element , AvecLibelle {

    private String id;

    public final void print(final TextDocument target, final Iterable<Element> sourceData) throws Exception {
        ArgumentChecks.ensureNonNull("Target document", target);
        ArgumentChecks.ensureNonNull("Source data collection", sourceData);

        // TODO : put title ?
        // TODO : apply filter now
        final Paragraph sectionStart = target.addParagraph(libelle.get());
        final Paragraph sectionEnd = target.insertParagraph(sectionStart, sectionStart, false);

        sectionStart.applyHeading(true, 2);

        printSection(target, sectionStart, sectionEnd, sourceData.iterator());
    }

    protected abstract void printSection(final TextDocument target, final Paragraph sectionStart, final Paragraph sectionEnd, final Iterator<Element> dataIt) throws Exception;

    @Override
    @Internal
    @JsonProperty("id")
    public String getId(){
        if(this.id==null)
          this.id = UUID.randomUUID().toString();
        return id;
    }

    @JsonProperty("id")
    public void setId(String id){
        this.id = id;
    }

    @Override
    @Internal
    @JsonIgnore
    public String getDocumentId(){
        if(documentId != null)
            return documentId;
        if(parent == null )
            return null;
        if(parent.get()==null)
            return null;
        return parent.get().getDocumentId();
    }

    /**
     * @return the parent {@link Element} of the current object, or itself if its a CouchDB document root node.
     * Can be null for newly created objects which has not been saved in database yet.
     */
    @Override
    @Internal
    @JsonIgnore
    public Element getCouchDBDocument(){
        if(parent == null )
            return null;
        if(parent.get()==null)
            return null;
        return parent.get().getCouchDBDocument();
    }

    private String documentId;

    @JsonProperty(required=false)
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    protected final ObjectProperty<Element> parent =  new SimpleObjectProperty<>();

    /**
     * @return the parent {@link Element} of the current element, or null if no parent is set.
     * Note that no CouchDB document has a parent property. Only contained elements have one.
     */
    @Override
    public ObjectProperty<Element> parentProperty() {
        return parent;
    }

    /**
     * @return the parent {@link Element} of the current element, or null if no parent is set.
     * Note that no CouchDB document has a parent property. Only contained elements have one.
     */
    @Override
    public Element getParent(){
       return parent.get();
    }

    @Override
    @JsonBackReference("parent")
    public void setParent(Element parent){
       this.parent.set(parent);
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

    private final StringProperty  designation = new SimpleStringProperty();
    @Override
    public StringProperty designationProperty() {
       return designation;
    }

    private final StringProperty  libelle = new SimpleStringProperty();
    @Override
    public StringProperty libelleProperty() {
       return libelle;
    }


    private final StringProperty  requeteId = new SimpleStringProperty();
    public StringProperty requeteIdProperty() {
       return requeteId;
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
    public String getDesignation(){
        return this.designation.get();
    }


    @Override
    public void setDesignation(String designation){
        this.designation.set(designation);
    }

    @Override
    public String getLibelle(){
        return this.libelle.get();
    }


    @Override
    public void setLibelle(String libelle){
        this.libelle.set(libelle);
    }

    @Reference(ref=fr.sirs.core.model.SQLQuery.class)
    public String getRequeteId(){
        return this.requeteId.get();
    }


    public void setRequeteId(String requeteId){
        this.requeteId.set(requeteId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        AbstractSectionRapport other = (AbstractSectionRapport) obj;
        if (id != null) {
            return id.equals(other.id); // TODO : check revision ?
        } else {
            return contentBasedEquals(other);
        }
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder("[RapportSectionObligationReglementaire ");
        builder.append("author: ");
        builder.append(getAuthor());
        builder.append(", ");
        builder.append("valid: ");
        builder.append(getValid());
        builder.append(", ");
        builder.append("designation: ");
        builder.append(getDesignation());
        builder.append(", ");
        builder.append("libelle: ");
        builder.append(getLibelle());
        builder.append(", ");
        builder.append("requeteId: ");
        builder.append(getRequeteId());
        return builder.toString();
    }

    @Override
    public boolean contentBasedEquals(Element element) {
        if(element instanceof AbstractSectionRapport) {
            final AbstractSectionRapport other = (AbstractSectionRapport) element;
            if (getId() == null) {
                if (other.getId() != null) return false;
            } else if (!getId().equals(other.getId())) return false;
            if ((this.getAuthor()==null ^ other.getAuthor()==null) || ( (this.getAuthor()!=null && other.getAuthor()!=null) && !this.getAuthor().equals(other.getAuthor()))) return false;
            if (this.getValid() != other.getValid()) return false;
            if ((this.getDesignation()==null ^ other.getDesignation()==null) || ( (this.getDesignation()!=null && other.getDesignation()!=null) && !this.getDesignation().equals(other.getDesignation()))) return false;
            if ((this.getLibelle()==null ^ other.getLibelle()==null) || ( (this.getLibelle()!=null && other.getLibelle()!=null) && !this.getLibelle().equals(other.getLibelle()))) return false;
            if ((this.getRequeteId()==null ^ other.getRequeteId()==null) || ( (this.getRequeteId()!=null && other.getRequeteId()!=null) && !this.getRequeteId().equals(other.getRequeteId()))) return false;
            return true;
        }
        return false;
    }

    /**
     * Put current object attributes in given one.
     * @param target The object to set attribute values.
     */
    protected void copy(final AbstractSectionRapport target) {
        target.setAuthor(getAuthor());
        target.setValid(getValid());
        target.setDesignation(getDesignation());
        target.setLibelle(getLibelle());
        target.setRequeteId(getRequeteId());
    }
}

