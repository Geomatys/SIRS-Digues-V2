/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.repo;

import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.util.List;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
@View(name="byDigueId", map="function(doc) {if(doc['@class']=='fr.symadrem.sirs.core.model.TronconDigue') {emit(doc.digueId, doc._id)}}")
public class CustomizedTronconDigueRepository  extends TronconDigueRepository {

    @Autowired
    public CustomizedTronconDigueRepository(CouchDbConnector db) {
        super(db);
    }
    
    public List<TronconDigue> getByDigue(final Digue digue){
        return this.queryView("byDigueId", digue.getId());
    }
}
