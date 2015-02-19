package fr.sirs.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ValiditySummary {
    
    @JsonProperty("docId")
    private String docId;
    
    @JsonProperty("docClass")
    private String docClass;
    
    @JsonProperty("elementId")
    private String elementId;
    
    @JsonProperty("elementClass")
    private String elementClass;
    
    @JsonProperty("author")
    private String author;
    
    @JsonProperty("valid")
    private boolean valid;
    
    @JsonProperty("pseudoId")
    private String pseudoId;
    
    @JsonProperty("label")
    private String label;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getDocClass() {
        return docClass;
    }

    public void setDocClass(String docClass) {
        this.docClass = docClass;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getElementClass() {
        return elementClass;
    }

    public void setElementClass(String elementClass) {
        this.elementClass = elementClass;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getPseudoId() {
        return pseudoId;
    }

    public void setPseudoId(String pseudoId) {
        this.pseudoId = pseudoId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "ValiditySummary{" + "docId=" + docId + ", docClass=" + docClass + ", elementId=" + elementId + ", elementClass=" + elementClass + ", author=" + author + ", valid=" + valid + ", pseudoId=" + pseudoId + ", label=" + label + '}';
    }
    
}
