package fr.sirs.core.component;

import org.ektorp.support.CouchDbDocument;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
public class DeletedCouchDbDocument extends CouchDbDocument {

	@JsonProperty("_deleted")
	private boolean deleted;
	
	public boolean isDeleted() {
		return deleted;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	
	
}
