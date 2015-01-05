package fr.sirs.core.model;

import java.io.Serializable;

public interface Element extends Serializable {

    Element getCouchDBDocument();
    
	String getDocumentId();
	
	String getId();
	
	void setParent(Element parent);
		
}
