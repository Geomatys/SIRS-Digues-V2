package fr.sirs.core.component;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SirsCoreRuntimeExecption;
import fr.sirs.core.model.ElementCreator;
import org.ektorp.CouchDbConnector;
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
public class UtilisateurRepository extends AbstractSIRSRepository<Utilisateur>{

    @Autowired
    public UtilisateurRepository ( CouchDbConnector db) {
       super(Utilisateur.class, db);
       initStandardDesignDocument();
   }
    
    @Override
    public Class<Utilisateur> getModelClass() {
        return Utilisateur.class;
    }
    
    @Override
    public Utilisateur create(){
        final SessionGen session = InjectorCore.getBean(SessionGen.class);
        if(session!=null && session instanceof OwnableSession){
            final ElementCreator elementCreator = ((OwnableSession) session).getElementCreator();
            return elementCreator.createElement(Utilisateur.class);
        } else {
            throw new SirsCoreRuntimeExecption("Pas de session courante");
        }
//        return new Utilisateur();
    }

    public List<Utilisateur> getByLogin(final String login) {
        return this.queryView("byLogin", login);
    }
   
}

