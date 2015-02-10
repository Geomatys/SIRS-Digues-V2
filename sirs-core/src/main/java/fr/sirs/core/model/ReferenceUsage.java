package fr.sirs.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReferenceUsage {

    @JsonProperty("property")
    private String property;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("objectId")
    private String objectId;
    
    @JsonProperty("label")
    private String label;

    public String getProperty() {
        return property;
    }

    public void setProperty(String label) {
        this.property = label;
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
    
    public String getLabel(){return label;}
    
    public void setLabel(final String label){this.label=label;}

    @Override
    public String toString() {
        return "ReferenceUsage [property=" + property + " type="+ type + " objectId="+objectId+ " label="+label+"]";
    }
}
