
package fr.sirs.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.sirs.util.property.Internal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.ektorp.Attachment;
import org.ektorp.support.Revisions;
import org.ektorp.util.Assert;

/**
 * A structure to describe an SQL query. Contains the request itself, as a name 
 * and an abstract. Id, revision and others are attributes needed for CouchDB 
 * storage.
 * @author Alexis Manin
 * @author Johann Sorel
 */
public class SQLQuery implements Identifiable {
    
    private static final char SEPARATOR = '§';
    
    public final StringProperty name = new SimpleStringProperty("");
    public final StringProperty description = new SimpleStringProperty("");
    public final StringProperty sql = new SimpleStringProperty("");

    public SQLQuery() {}

    public SQLQuery(String name, String desc, String sql) {
        this.name.set(name);
        this.description.set(desc);
        this.sql.set(sql);
    }
    
    public SQLQuery(String name, String value) {
        this.name.set(name);
        final int index = value.indexOf(SEPARATOR);
        if(index<=0){
            this.sql.set(value);
            this.description.set("");
        }else{
            this.sql.set(value.substring(0, index));
            this.description.set(value.substring(index+1));
        }
    }

    public String getName() {
        return name.get();
    }

    public String getDescription() {
        return description.get();
    }

    public String getSql() {
        return sql.get();
    }
    
    
    public void setName(String name) {
        this.name.set(name);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public void setSql(String sql) {
        this.sql.set(sql);
    }
    
    @JsonIgnore
    public String getValueString(){
        return sql.get()+SEPARATOR+description.get();
    }

    public SQLQuery copy() {
        return new SQLQuery(this.name.get(), this.description.get(), this.sql.get());
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.name.get());
        hash = 71 * hash + Objects.hashCode(this.description.get());
        hash = 71 * hash + Objects.hashCode(this.sql.get());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SQLQuery other = (SQLQuery) obj;
        if (!Objects.equals(this.name.get(), other.name.get())) {
            return false;
        }
        if (!Objects.equals(this.description.get(), other.description.get())) {
            return false;
        }
        return Objects.equals(this.sql.get(), other.sql.get());
    }
    
    // COUCHDB DOCUMENT COMMON INFORMATION
    // BEGIN-DUP This code is duplicated from org.ektorp.support.CouchDbDocument
    //
    public static final String ATTACHMENTS_NAME = "_attachments";

    protected String id;
    protected String revision;
    protected Map<String, Attachment> attachments;
    protected List<String> conflicts;
    protected Revisions revisions;

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
     * @return
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
            attachments = new HashMap<String, Attachment>();
        }
        attachments.put(a.getId(), a);
    }
}
