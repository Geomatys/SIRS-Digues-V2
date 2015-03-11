package fr.sirs.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PreviewLabel implements Identifiable {

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
    
    /**
     * Return canonical class name of the mirrored element.
     * @return 
     */
    public String getType(){
        return type;
    }
    
    public void setType(String type){
        this.type=type;
    }
    
    /**
     * Return the Id of the object mirrored by the current preview.
     * @return The couchDB identifier of the target element of this preview.
     */
    public String getObjectId(){
        return objectId;
    }
    
    public void setObjectId(String objectId) {
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

    /**
     * Same as {@link #getObjectId() }
     * @return 
     */
    @Override
    public String getId() {
        return objectId;
    }
}
