/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author Samuel Andrés (Geomatys)
 */
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
