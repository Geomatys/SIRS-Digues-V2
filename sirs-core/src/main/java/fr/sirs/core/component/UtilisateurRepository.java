package fr.sirs.core.component;

import fr.sirs.core.Repository;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.Utilisateur;
import java.util.List;
import org.ektorp.support.Views;


@Component
@Views({
        @View(name="all", map="function(doc) {if(doc['@class']=='fr.sirs.core.model.Utilisateur') {emit(doc._id, doc._id)}}"),
        @View(name = "byLogin", map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.Utilisateur') {emit(doc.login, doc._id)}}") })
public class UtilisateurRepository extends CouchDbRepositorySupport<Utilisateur> implements Repository<Utilisateur>{

    @Autowired
    public UtilisateurRepository ( CouchDbConnector db) {
       super(Utilisateur.class, db);
       initStandardDesignDocument();
   }
    
    public Class<Utilisateur> getModelClass() {
        return Utilisateur.class;
    }
    
    public Utilisateur create(){
        return new Utilisateur();
    }

    public List<Utilisateur> getByLogin(final String login) {
        return this.queryView("byLogin", login);
    }
   
}

