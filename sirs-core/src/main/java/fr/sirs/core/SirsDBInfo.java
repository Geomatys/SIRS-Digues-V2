package fr.sirs.core;

import org.ektorp.support.CouchDbDocument;

@SuppressWarnings("serial")
public class SirsDBInfo extends CouchDbDocument {

	private String version;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
