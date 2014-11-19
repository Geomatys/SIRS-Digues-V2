package fr.sirs.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PreviewLabel {

    
    @JsonProperty("_id")
    private String id;
    
    @JsonProperty("_rev")
    private String rev;
    
    @JsonProperty("libelle")
    private String label;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "PreviewLabel [label=" + label + "]";
    }
    
    
    
}
