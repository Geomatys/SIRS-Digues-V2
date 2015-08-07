

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.Objet;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.PositionConvention;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets PositionConvention.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@Views ({
@View(name=AbstractPositionableRepository.BY_LINEAR_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.PositionConvention') {emit(doc.linearId, doc._id)}}"),
@View(name=AbstractPositionDocumentRepository.BY_DOCUMENT_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.PositionConvention') {emit(doc.sirsdocument, doc._id)}}"),
@View(name=PositionConventionRepository.BY_OBJET_ID, map="classpath:positionsConventionsByObjetId.js"),
@View(name="all", map="function(doc) {if(doc['@class']=='fr.sirs.core.model.PositionConvention') {emit(doc._id, doc._id)}}")
})
@Component("fr.sirs.core.component.PositionConventionRepository")
public class PositionConventionRepository extends 
AbstractPositionDocumentRepository
<PositionConvention> {
        
    public static final String BY_OBJET_ID = "byObjetId";
    
    @Autowired
    private PositionConventionRepository ( CouchDbConnector db) {
       super(PositionConvention.class, db);
       initStandardDesignDocument();
   }
    
    public List<PositionConvention> getByObjet(final Objet objet) {
        ArgumentChecks.ensureNonNull("objet", objet);
        return this.queryView(BY_OBJET_ID, objet.getId());
    }
    
    @Override
    public PositionConvention create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(PositionConvention.class);
    }
}

