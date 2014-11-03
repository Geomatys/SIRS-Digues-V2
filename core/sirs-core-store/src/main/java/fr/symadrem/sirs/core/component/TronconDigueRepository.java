/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.symadrem.sirs.core.component;

import fr.symadrem.sirs.core.Repository;
import fr.symadrem.sirs.core.model.Crete;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.Fondation;
import fr.symadrem.sirs.core.model.PiedDigue;
import fr.symadrem.sirs.core.model.TronconDigue;

import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Views({
    @View(name = "all",       map = "function(doc) {if(doc['@class']=='fr.symadrem.sirs.core.model.TronconDigue') {emit(doc._id, doc._id)}}"),
    @View(name = "byDigueId", map = "function(doc) {if(doc['@class']=='fr.symadrem.sirs.core.model.TronconDigue') {emit(doc.digueId, doc._id)}}")
})
public class TronconDigueRepository extends
		CouchDbRepositorySupport<TronconDigue> implements
		Repository<TronconDigue> {

	public static final String PIED_DIGUES = "pieds_digues";

	public static final String FONDATIONS = "fondations";

	public static final String CRETES = "cretes";

	@Autowired
	public TronconDigueRepository(CouchDbConnector db) {
		super(TronconDigue.class, db);
		initStandardDesignDocument();
	}

	public List<TronconDigue> getByDigue(final Digue digue) {
		return this.queryView("byDigueId", digue.getId());
	}

	@Override
	public Class<TronconDigue> getModelClass() {
		return TronconDigue.class;
	}

	@Override
	public TronconDigue create() {
		return new TronconDigue();
	}

	@View(name = FONDATIONS, map = "classpath:/symadrem/couchdb/view/fondations-map.js")
	public List<Fondation> getAllFondations() {
		return db.queryView(createQuery(FONDATIONS), Fondation.class);
	}

	@View(name = CRETES, map = "classpath:/symadrem/couchdb/view/cretes-map.js")
	public List<Crete> getAllCretes() {
		return db.queryView(createQuery(CRETES), Crete.class);
	}

	@View(name = PIED_DIGUES, map = "classpath:/symadrem/couchdb/view/pieds_digue-map.js")
	public List<PiedDigue> getAllPiedDigues() {
		return db.queryView(createQuery(PIED_DIGUES), PiedDigue.class);
	}
}
