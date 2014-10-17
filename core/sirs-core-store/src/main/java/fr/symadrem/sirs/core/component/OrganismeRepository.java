package fr.symadrem.sirs.core.component;

import fr.symadrem.sirs.core.Repository;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;

import fr.symadrem.sirs.core.model.Organisme;

@View(name = "all", map = "function(doc) {if(doc['@class']=='fr.symadrem.sirs.core.model.Organisme') {emit(doc._id, doc._id)}}")
public class OrganismeRepository extends CouchDbRepositorySupport<Organisme> implements Repository<Organisme> {

    @Autowired
    public OrganismeRepository(CouchDbConnector db) {
        super(Organisme.class, db);
        initStandardDesignDocument();
    }

    @Override
    public Class<Organisme> getModelClass() {
        return Organisme.class;
    }

    @Override
    public Organisme create() {
        return new Organisme();
    }
}
