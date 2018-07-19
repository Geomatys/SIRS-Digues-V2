package org.ektorp.impl;

import java.util.Date;

import org.ektorp.ActiveTask;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.sirs.couchdb2.Couchdb2ReplicationTask;

/**
 * Branchement de la réplication sur une tâche spécifiquement implémentée pour le support de couchDB 2.
 * 
 * @author Samuel Andrés (Geomatys) [surcharge de la classe correspondante d'Ektorp]
 * @see Couchdb2ReplicationTask
 */
@JsonTypeInfo(
   use = JsonTypeInfo.Id.NAME,
   include = JsonTypeInfo.As.PROPERTY,
   property = "type")
@JsonSubTypes({
   @Type(value = Couchdb2ReplicationTask.class, name = "replication"), // implémentation spécifique aux tâches de réplication
   @Type(value = StdIndexerTask.class, name = "indexer"),
   @Type(value = StdDatabaseCompactionTask.class, name = "database_compaction"),
   @Type(value = StdViewCompactionTask.class, name = "view_compaction") })
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP")
public abstract class StdActiveTask implements ActiveTask {

    private String pid;
    private int progress;
    private Date startedOn;
    private Date updatedOn;

    @Override
    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    @Override
    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public Date getStartedOn() {
        return startedOn;
    }

    @JsonProperty(value = "started_on")
    public void setStartedOn(Date startedOn) {
        this.startedOn = startedOn;
    }

    @Override
    public Date getUpdatedOn() {
        return updatedOn;
    }

    @JsonProperty(value = "updated_on")
    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }
}
