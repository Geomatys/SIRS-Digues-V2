

package fr.sirs.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.sirs.Injector;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.util.ReferenceTableCell;
import fr.sirs.util.property.Internal;
import fr.sirs.util.property.Reference;
import java.util.*;
import javafx.beans.property.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.ektorp.*;
import org.ektorp.support.*;
import org.ektorp.util.*;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(Include.NON_EMPTY)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@SuppressWarnings("serial")
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ObligationReglementaire  
    implements Element , AvecLibelle, AvecCommentaire   {
    /**
    * @deprecated Please avoid to use this constructor from application because 
    * of validity/author control. If the element you want to create is a 
    * couchDB document, you can use the create() method from the corresponding 
    * repo. On the contrary, use the T createElement(Class clazz) method
    * of the ElementCreator available in the Session.
    */
    @Deprecated public ObligationReglementaire() {super();}

    //
    // BEGIN-DUP This code is duplicated from org.ektorp.support.CouchDbDocument
    //
    public static final String ATTACHMENTS_NAME = "_attachments";

    private String id;
    private String revision;
    private Map<String, Attachment> attachments;
    private List<String> conflicts;
    private Revisions revisions;

    @Internal
    @JsonProperty("_id")
    public String getId() { 
        return id;
    }

    @JsonProperty("_id")
    public void setId(String s) {
        Assert.hasText(s, "id must have a value");
        if (id != null && id.equals(s)) {
            return;
        }
        if (id != null) {
            throw new IllegalStateException("cannot set id, id already set");
        }
        id = s;
    }
    
    @Internal
    @JsonProperty("_rev")
    public String getRevision() {
        return revision;
    }

    @JsonProperty("_rev")
    public void setRevision(String s) {
        // no empty strings thanks
        if (s != null && s.length() == 0) {
            return;
        }
        this.revision = s;
    }

    @Internal
    @JsonIgnore
    public boolean isNew() {
        return revision == null;
    }

    @Internal
    @JsonProperty(ATTACHMENTS_NAME)
    public Map<String, Attachment> getAttachments() {
        return attachments;
    }
    
    @JsonProperty(ATTACHMENTS_NAME)
    void setAttachments(Map<String, Attachment> attachments) {
        this.attachments = attachments;
    }

    @JsonProperty("_conflicts")
    void setConflicts(List<String> conflicts) {
        this.conflicts = conflicts;
    }

    @JsonProperty("_revisions")
    void setRevisions(Revisions r) {
        this.revisions = r;
    }
    
    /**
     * Note: Will only be populated if this document has been loaded with the revisions option = true.
     * @return Known revisions.
     */
    @Internal
    @JsonIgnore
    public Revisions getRevisions() {
        return revisions;
    }

    /**
     *
     * @return a list of conflicting revisions. Note: Will only be populated if this document has been loaded through the CouchDbConnector.getWithConflicts method.
     */
    @Internal
    @JsonIgnore
    public List<String> getConflicts() {
        return conflicts;
    }
    /**
     *
     * @return true if this document has a conflict. Note: Will only give a correct value if this document has been loaded through the CouchDbConnector.getWithConflicts method.
     */
    public boolean hasConflict() {
        return conflicts != null && !conflicts.isEmpty();
    }

    protected void removeAttachment(String id) {
        Assert.hasText(id, "id may not be null or emtpy");
        if (attachments != null) {
            attachments.remove(id);
        }
    }

    protected void addInlineAttachment(Attachment a) {
        Assert.notNull(a, "attachment may not be null");
        Assert.hasText(a.getDataBase64(), "attachment must have data base64-encoded");
        if (attachments == null) {
            attachments = new HashMap<>();
        }
        attachments.put(a.getId(), a);
    }
        
    //
    // END-DUP
    //
    
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

    /**
    * JavaFX property for libelle.
    */
    private final StringProperty  libelle = new SimpleStringProperty();
    /**
    * @return JavaFX observable property for libelle.
    */
    public StringProperty libelleProperty() {
       return libelle;
    }
    /**
    * JavaFX property for commentaire.
    */
    private final StringProperty  commentaire = new SimpleStringProperty();
    /**
    * @return JavaFX observable property for commentaire.
    */
    public StringProperty commentaireProperty() {
       return commentaire;
    }
    /**
    * JavaFX property for designation.
    */
    private final StringProperty  designation = new SimpleStringProperty();
    /**
    * @return JavaFX observable property for designation.
    */
    public StringProperty designationProperty() {
       return designation;
    }
    /**
    * JavaFX property for author.
    */
    private final StringProperty  author = new SimpleStringProperty();
    /**
    * @return JavaFX observable property for author.
    */
    public StringProperty authorProperty() {
       return author;
    }
    /**
    * JavaFX property for valid.
    */
    private final BooleanProperty  valid = new SimpleBooleanProperty();
    /**
    * @return JavaFX observable property for valid.
    */
    public BooleanProperty validProperty() {
       return valid;
    }
    /**
    * JavaFX property for annee.
    */
    private final IntegerProperty  annee = new SimpleIntegerProperty();
    /**
    * @return JavaFX observable property for annee.
    */
    public IntegerProperty anneeProperty() {
       return annee;
    }
    private final StringProperty systemeEndiguementId = new SimpleStringProperty();
    
    public StringProperty systemeEndiguementIdProperty() {
        return this.systemeEndiguementId;
    }
    private final StringProperty typeId = new SimpleStringProperty();
    
    public StringProperty typeIdProperty() {
        return this.typeId;
    }
    private final StringProperty planifId = new SimpleStringProperty();
    
    public StringProperty planifIdProperty() {
        return this.planifId;
    }
    
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
    
    public String getDesignation(){
        return this.designation.get();
    }
    
        
    public void setDesignation(String designation){
        this.designation.set(designation);
    }
    
    public String getAuthor(){
        return this.author.get();
    }
    
        
    public void setAuthor(String author){
        this.author.set(author);
    }
    
    public boolean getValid(){
        return this.valid.get();
    }
    
        
    public void setValid(boolean valid){
        this.valid.set(valid);
    }
    
    public int getAnnee(){
        return this.annee.get();
    }
    
        
    public void setAnnee(int annee){
        this.annee.set(annee);
    }
    
    public String getClassement(){
        final String seId = getSystemeEndiguementId();
        if(seId!=null && !seId.isEmpty()){
            try{
            final SystemeEndiguement se = Injector.getSession().getRepositoryForClass(SystemeEndiguement.class).get(getSystemeEndiguementId());
            if(se!=null){
                return se.getClassement();
            }
            }catch(DocumentNotFoundException e){
                return ReferenceTableCell.OBJECT_DELETED;
            }
        }
        return "";
    }

    @Reference(ref=SystemeEndiguement.class)  
    public String getSystemeEndiguementId(){
        return this.systemeEndiguementId.get();
    }

    public void setSystemeEndiguementId(String systemeEndiguementId){
        this.systemeEndiguementId.set( systemeEndiguementId );
    }
    
    @Reference(ref=RefTypeObligationReglementaire.class)  
    public String getTypeId(){
        return this.typeId.get();
    }

    public void setTypeId(String typeId){
        this.typeId.set( typeId );
    }
    
    @Reference(ref=PlanificationObligationReglementaire.class)  
    public String getPlanifId(){
        return this.planifId.get();
    }

    public void setPlanifId(String planifId){
        this.planifId.set( planifId );
    }
        
    @Override
    public ObligationReglementaire copy() {
    
        ObligationReglementaire obligationReglementaire = new ObligationReglementaire();
    
        obligationReglementaire.setLibelle(getLibelle());
        obligationReglementaire.setCommentaire(getCommentaire());
        obligationReglementaire.setDesignation(getDesignation());
        obligationReglementaire.setAuthor(getAuthor());
        obligationReglementaire.setValid(getValid());
        obligationReglementaire.setAnnee(getAnnee());
        obligationReglementaire.setSystemeEndiguementId(getSystemeEndiguementId());
        obligationReglementaire.setTypeId(getTypeId());
        obligationReglementaire.setPlanifId(getPlanifId());

        return obligationReglementaire;
    }

    @Override
    public int hashCode() {
        return (id == null)? 0 : id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ObligationReglementaire other = (ObligationReglementaire) obj;
        if (id != null) {
            return id.equals(other.id); // TODO : check revision ?
        } else {
            return contentBasedEquals(other);
        }
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder("[ObligationReglementaire ");
        builder.append("libelle: ");
        builder.append(getLibelle());
        builder.append(", ");
        builder.append("commentaire: ");
        builder.append(getCommentaire());
        builder.append(", ");
        builder.append("designation: ");
        builder.append(getDesignation());
        builder.append(", ");
        builder.append("author: ");
        builder.append(getAuthor());
        builder.append(", ");
        builder.append("valid: ");
        builder.append(getValid());
        builder.append(", ");
        builder.append("annee: ");
        builder.append(getAnnee());
        return builder.toString();
    }
    
    @Override
    public boolean contentBasedEquals(Element element) {
        if(element instanceof ObligationReglementaire) {

            final ObligationReglementaire other = (ObligationReglementaire) element;
            if (getId() == null) {
                if (other.getId() != null) return false;
            } else if (!getId().equals(other.getId())) return false;
            if ((this.getLibelle()==null ^ other.getLibelle()==null) || ( (this.getLibelle()!=null && other.getLibelle()!=null) && !this.getLibelle().equals(other.getLibelle()))) return false;
            if ((this.getCommentaire()==null ^ other.getCommentaire()==null) || ( (this.getCommentaire()!=null && other.getCommentaire()!=null) && !this.getCommentaire().equals(other.getCommentaire()))) return false;
            if ((this.getDesignation()==null ^ other.getDesignation()==null) || ( (this.getDesignation()!=null && other.getDesignation()!=null) && !this.getDesignation().equals(other.getDesignation()))) return false;
            if ((this.getAuthor()==null ^ other.getAuthor()==null) || ( (this.getAuthor()!=null && other.getAuthor()!=null) && !this.getAuthor().equals(other.getAuthor()))) return false;
            if (this.getValid() != other.getValid()) return false;
            if (this.getAnnee() != other.getAnnee()) return false;
            if ((this.getSystemeEndiguementId()==null ^ other.getSystemeEndiguementId()==null) || ( (this.getSystemeEndiguementId()!=null && other.getSystemeEndiguementId()!=null) && !this.getSystemeEndiguementId().equals(other.getSystemeEndiguementId()))) return false;
            if ((this.getTypeId()==null ^ other.getTypeId()==null) || ( (this.getTypeId()!=null && other.getTypeId()!=null) && !this.getTypeId().equals(other.getTypeId()))) return false;
            if ((this.getPlanifId()==null ^ other.getPlanifId()==null) || ( (this.getPlanifId()!=null && other.getPlanifId()!=null) && !this.getPlanifId().equals(other.getPlanifId()))) return false;
            return true;
        }
        return false;
    }

    @Override
    public boolean removeChild(final Element toRemove) {
        if (toRemove == null) return false;
        if (toRemove.getId().equals(getSystemeEndiguementId())) {
            setSystemeEndiguementId(null);
            return true;
        }
        if (toRemove.getId().equals(getTypeId())) {
            setTypeId(null);
            return true;
        }
        if (toRemove.getId().equals(getPlanifId())) {
            setPlanifId(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean addChild(final Element toAdd) {
        if (toAdd == null) return false;
        if (toAdd instanceof SystemeEndiguement) {
            setSystemeEndiguementId(toAdd.getId());
            return true;
        }
        if (toAdd instanceof RefTypeObligationReglementaire) {
            setTypeId(toAdd.getId());
            return true;
        }
        if (toAdd instanceof PlanificationObligationReglementaire) {
            setPlanifId(toAdd.getId());
            return true;
        }
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

