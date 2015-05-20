package fr.sirs.core.component;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCoreRuntimeExecption;
import static fr.sirs.core.component.UtilisateurRepository.BY_LOGIN;
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
        @View(name = BY_LOGIN, map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.Utilisateur') {emit(doc.login, doc._id)}}") })
public class UtilisateurRepository extends AbstractSIRSRepository<Utilisateur>{

    public static final String BY_LOGIN = "byLogin";
    
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
        final SessionCore session = InjectorCore.getBean(SessionCore.class);
        if(session!=null){
            final ElementCreator elementCreator = session.getElementCreator();
            return elementCreator.createElement(Utilisateur.class);
        } else {
            throw new SirsCoreRuntimeExecption("Pas de session courante");
        }
    }

    public List<Utilisateur> getByLogin(final String login) {
        return this.queryView(BY_LOGIN, login);
    }
   
}

