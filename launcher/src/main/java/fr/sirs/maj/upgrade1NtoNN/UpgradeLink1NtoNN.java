/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.maj.upgrade1NtoNN;

import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Role;
import fr.sirs.core.model.Utilisateur;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.collection.BackingStoreException;

/**
 *
 * @author Matthieu Bastianelli (Geomatys)
 */
final class UpgradeLink1NtoNN {

    private final AbstractSIRSRepository <? extends Element> repoSide1;
    private final Map< Class, AbstractSIRSRepository<? extends Element>> reposSidesN;

    UpgradeLink1NtoNN(final Session session, final Upgrades1NtoNNSupported upgrade){
        ArgumentChecks.ensureNonNull("Session", session);

        final Utilisateur user = session.getUtilisateur();

        if( (user == null) || (session.getUtilisateur().getRole() != Role.ADMIN) ) {
            throw new IllegalStateException("Update should be lead by an administrator!");
        }

        //Récupération des répositories nécessaire à la mise à jour :
        //-----------------------------------------------------------
        repoSide1 = session.getRepositoryForClass(upgrade.linkSide1);

        reposSidesN = upgrade.linkSidesN.stream()
                .map(u -> u.clazz)
                .collect(Collectors.toMap(c -> c,
                                          c -> session.getRepositoryForClass(c)
                                         ));

        // Parcours des éléments portant initialement les liens
        repoSide1.getAllStreaming() //récupère tout les éléments de la classe du repo (désordres)
                .forEach(d -> upgrade(upgrade, d));


//        Injector.getSession()
//        session.getModelRepositories()
    }

    private void upgrade(final Upgrades1NtoNNSupported upgrade, final Element element) {
        if (!(upgrade.linkSide1.isInstance(element))) {
            throw new IllegalArgumentException(" Element input must be an instance of the upgrade input");
        }

        try {
            for (ClassAndItsGetter classeAndGetter : upgrade.linkSidesN) {
                List<String> extractedIds  = (List<String>) classeAndGetter.getter.invoke(element);
                extractedIds.forEach(
                 id -> {
                     final AbstractSIRSRepository<? extends Element> repo = reposSidesN.get(classeAndGetter.clazz);
                     final Element elt = repo.get(id);
                     elt.addChild(element);
                 }
                );
            }
        } catch (Exception e) {
            throw new BackingStoreException(e.getCause());
        }
    }




}
