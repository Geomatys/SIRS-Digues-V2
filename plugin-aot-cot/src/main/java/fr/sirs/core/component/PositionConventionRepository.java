

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.ObjetReseau;

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
@View(name=PositionConventionRepository.BY_RESEAU_ID, map="classpath:positionsConventionsByReseauId.js"),
@View(name="all", map="function(doc) {if(doc['@class']=='fr.sirs.core.model.PositionConvention') {emit(doc._id, doc._id)}}")
})
@Component("fr.sirs.core.component.PositionConventionRepository")
public class PositionConventionRepository extends 
AbstractPositionDocumentRepository
<PositionConvention> {
        
    public static final String BY_RESEAU_ID = "byReseauId";
    
    @Autowired
    private PositionConventionRepository ( CouchDbConnector db) {
       super(PositionConvention.class, db);
       initStandardDesignDocument();
   }
    
    public List<PositionConvention> getByReseau(final ObjetReseau reseau) {
        ArgumentChecks.ensureNonNull("reseau", reseau);
        return this.queryView(BY_RESEAU_ID, reseau.getId());
    }
    
    @Override
    public PositionConvention create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(PositionConvention.class);
    }
}

