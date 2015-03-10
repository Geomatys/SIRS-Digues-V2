package fr.sirs.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PreviewLabel {

    @JsonProperty("libelle")
    private String label;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("objectId")
    private String objectId;
    
    @JsonProperty("pseudoId")
    private String pseudoId;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getType(){
        return type;
    }
    
    public void setType(String type){
        this.type=type;
    }
    
    public String getObjectId(){
        return objectId;
    }
    
    public void setObjectid(String objectId){
        this.objectId = objectId;
    }

    public String getPseudoId() {
        return pseudoId;
    }

    public void setPseudoId(String pseudoId) {
        this.pseudoId = pseudoId;
    }

    @Override
    public String toString() {
        return "PreviewLabel [label=" + label + " type="+ type + " objectId="+objectId+"]";
    }
}
