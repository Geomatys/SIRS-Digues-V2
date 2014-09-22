/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.symadrem.sirs.component;

import java.util.List;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.GenerateView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author samuel
 */
@Component
public class DigueRepository extends CouchDbRepositorySupport<Digue>{
    
    @Autowired
    public DigueRepository ( CouchDbConnector db) {
        super(Digue.class, db);
        initStandardDesignDocument();
    }
    
    @GenerateView @Override
    public List<Digue> getAll() {
        ViewQuery q = createQuery("all")
                                        .descending(true)
                                        .includeDocs(true);
        return db.queryView(q, Digue.class);
    }
}
