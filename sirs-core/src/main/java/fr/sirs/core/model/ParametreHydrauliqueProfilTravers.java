

package fr.sirs.core.model;

import fr.sirs.core.model.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.sirs.util.json.LocalDateDeserializer;
import fr.sirs.util.json.LocalDateSerializer;
import fr.sirs.util.property.Internal;
import fr.sirs.util.property.Reference;
import java.time.LocalDate;
import java.util.UUID;
import javafx.beans.property.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Overriden from generator. We had to add a duplicate check for {@link #evenementHydrauliqueIdProperty() }.
 * JIRA SYM-1456
 * @author Alexis Manin (Geomatys)
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(Include.NON_EMPTY)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@SuppressWarnings("serial")
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ParametreHydrauliqueProfilTravers
    implements Element , AvecCommentaire, AvecDateMaj   {
    /**
    * @deprecated Please avoid to use this constructor from application because
    * of validity/author control. If the element you want to create is a
    * couchDB document, you can use the create() method from the corresponding
    * repo. On the contrary, use the T createElement(Class clazz) method
    * of the ElementCreator available in the Session.
    */
    @Deprecated public ParametreHydrauliqueProfilTravers() {super();}
    private String id;

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
    /**
    * JavaFX property for dateMaj.
    */
    private final ObjectProperty<LocalDate>  dateMaj = new SimpleObjectProperty<LocalDate>();
    /**
    * @return JavaFX observable property for dateMaj.
    */
    public ObjectProperty<LocalDate> dateMajProperty() {
       return dateMaj;
    }
    /**
    * JavaFX property for debitPointe.
    */
    private final FloatProperty  debitPointe = new SimpleFloatProperty();
    /**
    * @return JavaFX observable property for debitPointe.
    */
    public FloatProperty debitPointeProperty() {
       return debitPointe;
    }
    /**
    * JavaFX property for vitessePointe.
    */
    private final FloatProperty  vitessePointe = new SimpleFloatProperty();
    /**
    * @return JavaFX observable property for vitessePointe.
    */
    public FloatProperty vitessePointeProperty() {
       return vitessePointe;
    }
    /**
    * JavaFX property for coteEau.
    */
    private final FloatProperty  coteEau = new SimpleFloatProperty();
    /**
    * @return JavaFX observable property for coteEau.
    */
    public FloatProperty coteEauProperty() {
       return coteEau;
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
    * JavaFX property for lastUpdateAuthor.
    */
    private final StringProperty  lastUpdateAuthor = new SimpleStringProperty();
    /**
     * @return JavaFX observable property for lastUpdateAuthor.
     */
    public StringProperty lastUpdateAuthorProperty() {
        return lastUpdateAuthor;
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
     * HACK : We override set method to detect duplicates. I.e, we want only one
     * parameter to be bound to a specific hydraulic event inside a {@link ProfilTravers}.
     */
    private final StringProperty evenementHydrauliqueId = new SimpleStringProperty() {

        @Override
        public void set(String newValue) {
            if (newValue != null) {
                final Element doc = getCouchDBDocument();
                if (doc instanceof ProfilTravers) {
                    ObservableList<ParametreHydrauliqueProfilTravers> phs = ((ProfilTravers) doc).getParametresHydrauliques();
                    for (final ParametreHydrauliqueProfilTravers ph : phs) {
                        if (ph != ParametreHydrauliqueProfilTravers.this) {
                            if (newValue.equals(ph.evenementHydrauliqueIdProperty().get())) {
                                if (ph.getDesignation() != null)
                                    throw new IllegalArgumentException("L'événement hydraulique défini est déjà référencé par le paramètre hydraulique "+ph.getDesignation());
                                else
                                    throw new IllegalArgumentException("Impossible d'associer l'événement hydraulique : un autre paramètre hydraulique l'utilise");
                            }
                        }
                    }
                }
            }
            super.set(newValue);
        }
    };

    public StringProperty evenementHydrauliqueIdProperty() {
        return this.evenementHydrauliqueId;
    }

    @JsonSerialize(using=LocalDateSerializer.class)
    public LocalDate getDateMaj(){
        return this.dateMaj.get();
    }


    @JsonDeserialize(using=LocalDateDeserializer.class)
    public void setDateMaj(LocalDate dateMaj){
        this.dateMaj.set(dateMaj);
    }

    public float getDebitPointe(){
        return this.debitPointe.get();
    }


    public void setDebitPointe(float debitPointe){
        this.debitPointe.set(debitPointe);
    }

    public float getVitessePointe(){
        return this.vitessePointe.get();
    }


    public void setVitessePointe(float vitessePointe){
        this.vitessePointe.set(vitessePointe);
    }

    public float getCoteEau(){
        return this.coteEau.get();
    }


    public void setCoteEau(float coteEau){
        this.coteEau.set(coteEau);
    }

    public String getCommentaire(){
        return this.commentaire.get();
    }


    public void setCommentaire(String commentaire){
        this.commentaire.set(commentaire);
    }

    public String getAuthor(){
        return this.author.get();
    }

    public void setAuthor(String author){
        this.author.set(author);
    }

    public String getLastUpdateAuthor(){
        return this.lastUpdateAuthor.get();
    }

    public void setLastUpdateAuthor(String lastUpdateAuthor){
        this.lastUpdateAuthor.set(lastUpdateAuthor);
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

    @Reference(ref=EvenementHydraulique.class)
    public String getEvenementHydrauliqueId(){
        return this.evenementHydrauliqueId.get();
    }

    public void setEvenementHydrauliqueId(String evenementHydrauliqueId){
        this.evenementHydrauliqueId.set( evenementHydrauliqueId );
    }

    @Override
    public ParametreHydrauliqueProfilTravers copy() {

        ParametreHydrauliqueProfilTravers parametreHydrauliqueProfilTravers = new ParametreHydrauliqueProfilTravers();

        parametreHydrauliqueProfilTravers.setDateMaj(getDateMaj());
        parametreHydrauliqueProfilTravers.setDebitPointe(getDebitPointe());
        parametreHydrauliqueProfilTravers.setVitessePointe(getVitessePointe());
        parametreHydrauliqueProfilTravers.setCoteEau(getCoteEau());
        parametreHydrauliqueProfilTravers.setCommentaire(getCommentaire());
        parametreHydrauliqueProfilTravers.setAuthor(getAuthor());
        parametreHydrauliqueProfilTravers.setLastUpdateAuthor(getLastUpdateAuthor());
        parametreHydrauliqueProfilTravers.setDesignation(getDesignation());
        parametreHydrauliqueProfilTravers.setValid(getValid());
        parametreHydrauliqueProfilTravers.setEvenementHydrauliqueId(getEvenementHydrauliqueId());

        return parametreHydrauliqueProfilTravers;
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
        ParametreHydrauliqueProfilTravers other = (ParametreHydrauliqueProfilTravers) obj;
        if (id != null) {
            return id.equals(other.id); // TODO : check revision ?
        } else {
            return contentBasedEquals(other);
        }
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder("[ParametreHydrauliqueProfilTravers ");
        builder.append("dateMaj: ");
        builder.append(getDateMaj());
        builder.append(", ");
        builder.append("debitPointe: ");
        builder.append(getDebitPointe());
        builder.append(", ");
        builder.append("vitessePointe: ");
        builder.append(getVitessePointe());
        builder.append(", ");
        builder.append("coteEau: ");
        builder.append(getCoteEau());
        builder.append(", ");
        builder.append("commentaire: ");
        builder.append(getCommentaire());
        builder.append(", ");
        builder.append("author: ");
        builder.append(getAuthor());
        builder.append(", ");
        builder.append("lastUpdateAuthor: ");
        builder.append(getLastUpdateAuthor());
        builder.append(", ");
        builder.append("designation: ");
        builder.append(getDesignation());
        builder.append(", ");
        builder.append("valid: ");
        builder.append(getValid());
        return builder.toString();
    }

    @Override
    public boolean contentBasedEquals(Element element) {
        if(element instanceof ParametreHydrauliqueProfilTravers) {

            final ParametreHydrauliqueProfilTravers other = (ParametreHydrauliqueProfilTravers) element;
            if (getId() == null) {
                if (other.getId() != null) return false;
            } else if (!getId().equals(other.getId())) return false;
            if ((this.getDateMaj()==null ^ other.getDateMaj()==null) || ( (this.getDateMaj()!=null && other.getDateMaj()!=null) && !this.getDateMaj().equals(other.getDateMaj()))) return false;
            if (this.getDebitPointe() != other.getDebitPointe()) return false;
            if (this.getVitessePointe() != other.getVitessePointe()) return false;
            if (this.getCoteEau() != other.getCoteEau()) return false;
            if ((this.getCommentaire()==null ^ other.getCommentaire()==null) || ( (this.getCommentaire()!=null && other.getCommentaire()!=null) && !this.getCommentaire().equals(other.getCommentaire()))) return false;
            if ((this.getAuthor()==null ^ other.getAuthor()==null) || ( (this.getAuthor()!=null && other.getAuthor()!=null) && !this.getAuthor().equals(other.getAuthor()))) return false;
            if ((this.getLastUpdateAuthor()==null ^ other.getLastUpdateAuthor()==null) || ( (this.getLastUpdateAuthor()!=null && other.getLastUpdateAuthor()!=null) && !this.getLastUpdateAuthor().equals(other.getLastUpdateAuthor()))) return false;
            if ((this.getDesignation()==null ^ other.getDesignation()==null) || ( (this.getDesignation()!=null && other.getDesignation()!=null) && !this.getDesignation().equals(other.getDesignation()))) return false;
            if (this.getValid() != other.getValid()) return false;
            if ((this.getEvenementHydrauliqueId()==null ^ other.getEvenementHydrauliqueId()==null) || ( (this.getEvenementHydrauliqueId()!=null && other.getEvenementHydrauliqueId()!=null) && !this.getEvenementHydrauliqueId().equals(other.getEvenementHydrauliqueId()))) return false;
            return true;
        }
        return false;
    }

    @Override
    public boolean removeChild(final Element toRemove) {
        if (toRemove == null) return false;
        if (toRemove.getId().equals(getEvenementHydrauliqueId())) {
            setEvenementHydrauliqueId(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean addChild(final Element toAdd) {
        if (toAdd == null) return false;
        if (toAdd instanceof EvenementHydraulique) {
            setEvenementHydrauliqueId(toAdd.getId());
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
