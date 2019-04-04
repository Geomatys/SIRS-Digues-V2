/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.theme.ui.pojotable;

import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AbstractObservation;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Preview;
import fr.sirs.theme.ui.PojoTableChoiceStage;
import fr.sirs.ui.Growl;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

/**
 *
 * @author matthieu
 */
public class ElementCopier {

    // Repository 
    protected AbstractSIRSRepository currentPojoRepo;
    protected AbstractSIRSRepository targetRepo;
    protected Session session;

    // Classe des éléments de la pojotable associée
    private Class pojoClass;

    protected Boolean avecForeignParent;
    protected Boolean isAbstractObservation;

    ObjectProperty<? extends Element> container;

    //Class vers laquelle on souhaite faire la copie des éléments sélectionnés.
    private Optional<Class> targetCLass;

    public ElementCopier(Class pojoClass, ObjectProperty<? extends Element> container, Session session, AbstractSIRSRepository pojoRepo) {

        this.pojoClass = pojoClass;
        this.avecForeignParent = AvecForeignParent.class.isAssignableFrom(pojoClass);
        this.isAbstractObservation = AbstractObservation.class.isAssignableFrom(pojoClass);
        this.container = container;
        this.session = session;

        //Identification de la classe vers laquelle on permet la copie.
        if (avecForeignParent) {

            //On a besoin du repositorie de la classe courante uniquement pour 
            // les classes AvecForeignParent.
            currentPojoRepo = pojoRepo;

            try {
                this.targetCLass = Optional.of(AvecForeignParent.getForeignParentClass(pojoClass));
            } catch (Exception e) {
                this.targetCLass = Optional.empty();
            }
        } else if (isAbstractObservation) { //On priorise la classe du ForeignParent pour la target classe.
            try {
                this.targetCLass = Optional.of(container.getValue().getClass());
            } catch (NullPointerException e) {
                this.targetCLass = Optional.empty();
            }
        } else {
            this.targetCLass = Optional.empty();
        }

        //Si on a trouvé une classe cible, on récupère son repositorie.
        if (targetCLass.isPresent()) {
            this.targetRepo = session.getRepositoryForClass(targetCLass.get());
        }
    }

    /**
     * Méthode permettant à l'utilisateur de choisir l'élément vers lequel il
     * veut faire une copie.
     *
     * @return target : élément ciblé.
     * @throws CopyElementException
     */
    public Element askForCopyTarget() throws CopyElementException {

        final Element target;
        final ObservableList<Preview> choices;

        //-------------------------Test si ces cas particuliers se produisent----------------------
        if (avecForeignParent && isAbstractObservation) {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Cas particulier pour la copie (ForeignParent && Observation).");
            alert.setResizable(true);
            alert.showAndWait();
        }

        if ((avecForeignParent && (container != null))) {//Se produit : exemple des voies sur Digues depuis désordre. Container -> désordre ; ForeignParent-> tronçon;
            final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Cas particulier pour la copie (ForeignParent && container).");
            alert.setResizable(true);
            alert.showAndWait();
        }
        //------------------------------------------------------------------------------------------

        //L'identification de la targetClass est faite dans le constructeur et priorise 
        //le ForeignParent sur le container.
        if (avecForeignParent || isAbstractObservation) {

            if (targetCLass.isPresent()) {
                // récupération de tous les éléments de la classe identifiée
                choices = SIRS.observableList(new ArrayList<>(session.getPreviews().getByClass(targetCLass.get())));
            } else {
                throw new CopyElementException("Copie impossible, aucune cible identifiée pour la copie.");
            }
        } else {
            throw new CopyElementException("Copie impossible pour ce type d'élément.");
        }

        final PojoTableChoiceStage<Element> stage = new ChoiceStage(targetRepo, choices, null, "Copier les éléments vers...", "Copier");
        stage.showAndWait();
        target = stage.getRetrievedElement().get();

        if (target != null) {
            return target;
        } else {
            throw new CopyElementException("Aucun élément sélectionné comme cible de la copie.");
        }

    }

    /**
     * Copie d'un ensemble d'éléments sélectionnés vers un élément cible.
     *
     * Cette méthode vise à être redéfinie dans les PojoTables spécifiques
     * (Classe extends PojoTable) en fonction du comportement souhaité de la
     * 'copie'. Par défaut, cette méthode informe l'utilisateur que la copie est
     * impossible.
     *
     * @param targetedElement : Elément auquel on veut ajouter les éléments
     * copiés.
     * @param pojosToCopy : éléments à copier.
     * @return
     */
    public List<? extends Element> copyPojosTo(Element targetedElement, Element... pojosToCopy) {

        if (this.avecForeignParent) {
            return copyPojosToForeignParent(targetedElement, pojosToCopy);

        } else if (this.isAbstractObservation) {

            final Alert alert = new Alert(Alert.AlertType.WARNING, "Attention les éléments copiés sont susceptibles de ne pas respecter les bornes temporelles de l'élément de destination! Vous devrez les mettre à jour manuellement.");
            alert.setResizable(true);
            alert.showAndWait();

            return copyPojosToContainer(targetedElement, pojosToCopy);
        }

        new Growl(Growl.Type.WARNING, "La copie n'est pas définie pour ce type d'élément ou depuis cette fenêtre.").showAndFade();
        return null;
    }

    //======================================
    //Différentes méthodes de copie gérées :
    //======================================
    /**
     *
     * Copie des éléments sélectionnés vers un Foreign Parent.
     *
     * @param targetedForeignParent: élément auquel on veut ajouter les éléments
     * copiés. Cette élément sera le ForeignParent des copies.
     * @param pojosToCopy éléments à copier.
     * @return
     */
    public List<AvecForeignParent> copyPojosToForeignParent(Element targetedForeignParent, Element... pojosToCopy) {

        // Si l'utilisateur est un externe, on court-circuite
        // la copie. -> s'assurer que la copie n'est pas réalisable pour les
        // utilisateurs externes disposant des droits sur l'élément cible.
        if (session.editionAuthorized(targetedForeignParent)) {

            List<AvecForeignParent> copiedPojos = new ArrayList<>();
            Boolean completSuccess = true;

            for (Element pojo : pojosToCopy) {

                try {

                    AvecForeignParent copiedPojo = (AvecForeignParent) pojo.copy();
                    copiedPojo.setForeignParentId(targetedForeignParent.getId());

                    //CHECK BORNES TEMPORELLES Parent ET BORNES TEMPORELLLES copiedPojo
                    copiedPojos.add(copiedPojo);

                } catch (ClassCastException e) {

                    completSuccess = false;
                    SIRS.LOGGER.log(Level.FINE, "Echec de la copie de l'élément :\n" + pojo.toString(), e);
                }

            }
            try {
                currentPojoRepo.executeBulk(copiedPojos);
            } catch (NullPointerException e) {
                SIRS.LOGGER.log(Level.FINE, "Repository introuvable", e);
            }

            if (!completSuccess) {
                new Growl(Growl.Type.WARNING, "Certains éléments n'ont pas pu être copiés.").showAndFade();
            }
            return copiedPojos;

        } else {
            new Growl(Growl.Type.WARNING, "Les éléments n'ont pas été copiés car vous n'avez pas les droits nécessaires.").showAndFade();
            return null;
        }

    }

    /**
     * Copie des éléments sélectionnés vers un container ciblé.
     *
     * Cette méthode est utilisée pour copier des éléments héritant de la classe
     * AbstractObservation. Note : elle peut servir de base si la demande de
     * l'utilisateur évolue pour permettre la copie d'autres type d'éléments
     * vers leur container. ((avecForeignParent && (container != null))
     *
     * @param targetedContainer : container (implémentant Element) auquel on
     * veut ajouter les éléments copiés.
     * @param pojosToCopy : éléments à copier
     * @return
     */
    public List<? extends Element> copyPojosToContainer(Element targetedContainer, Element... pojosToCopy) {

        // Si l'utilisateur est un externe, on court-circuite
        // la copie. -> s'assurer que la copie n'est pas réalisable pour les
        // utilisateurs externes disposant des droits sur l'élément cible.
        if (this.session.editionAuthorized(targetedContainer)) {
            Boolean completSuccess = true;

            List<Element> copiedPojos = new ArrayList<>();

            for (Element pojo : pojosToCopy) {

                try {
                    AbstractObservation copiedPojo = ((AbstractObservation) pojo).copy();
                    copiedPojos.add(copiedPojo);
                    targetedContainer.addChild(copiedPojo);

                    //CHECK BORNES TEMPORELLES Parent ET BORNES TEMPORELLLES copiedPojo
                } catch (ClassCastException e) {

                    completSuccess = false;
                    SIRS.LOGGER.log(Level.FINE, "Echec de la copie de l'élément car il n'implémente pas la classe AbstractObservation :\n" + pojo.toString(), e);
                }

            }

            try {
                // Le container porte les référence vers les "enfants" sauf pour les
                // AbstractObservation qui ne sont pas des documents couchDB.
                // C'est donc le container cible de la copie, qu'il faut mettre à 
                // jour en base.
                this.getTargetRepo().update(targetedContainer);
            } catch (NullPointerException e) {
                SIRS.LOGGER.log(Level.FINE, "Repository introuvable", e);
            }

            if (!completSuccess) {
                new Growl(Growl.Type.WARNING, "Certains éléments n'ont pas pu être copiés.").showAndFade();
            }

            return copiedPojos;

        } else {
            new Growl(Growl.Type.WARNING, "Les éléments n'ont pas été copiés car vous n'avez pas les droits nécessaires.").showAndFade();
            return null;
        }

    }

    //Getters
    public AbstractSIRSRepository getTargetRepo() {
        return targetRepo;
    }

    public Session getSession() {
        return session;
    }

    public Class getPojoClass() {
        return pojoClass;
    }

    public Boolean getAvecForeignParent() {
        return avecForeignParent;
    }

    public Boolean getIsAbstractObservation() {
        return isAbstractObservation;
    }

    public ObjectProperty<? extends Element> getContainer() {
        return container;
    }

    public Optional<Class> getTargetCLass() {
        return targetCLass;
    }

}
