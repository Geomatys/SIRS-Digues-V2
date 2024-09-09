/**
 * This file is part of SIRS-Digues 2.
 * <p>
 * Copyright (C) 2019, FRANCE-DIGUES,
 * <p>
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.theme.ui.pojotable;

import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Preview;
import fr.sirs.theme.ui.PojoTableChoiceStage;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import org.apache.sis.util.ArgumentChecks;

import java.util.List;
import java.util.Optional;

/**
 * @author Matthieu Bastianelli (Geomatys)
 * @author Estelle Idée (Geomatys) : changed class to abstract class
 */
public abstract class AbstractElementCopier {

    protected AbstractSIRSRepository targetRepo;

    // Classe des éléments de la pojotable associée
    final protected Class pojoClass;

    ObjectProperty<? extends Element> container;

    //Class vers laquelle on souhaite faire la copie des éléments sélectionnés.
    protected Optional<Class> targetClass;

    public AbstractElementCopier(Class pojoClass, ObjectProperty<? extends Element> container) {
        ArgumentChecks.ensureNonNull("Pojo class", pojoClass);

        this.pojoClass = pojoClass;
        this.container = container;
    }

    /**
     * Méthode permettant à l'utilisateur de choisir l'élément vers lequel il
     * veut faire une copie.
     *
     * @return target : élément ciblé.
     * @throws CopyElementException
     */
    public Element askForCopyTarget() throws CopyElementException {
        final ObservableList<Preview> choices = getChoices();

        final PojoTableChoiceStage<Element> stage = new ChoiceStage(targetRepo, choices, null, "Copier les éléments vers...", "Copier");
        stage.showAndWait();
        final Element target = stage.getRetrievedElement().get();

        if (target != null) {
            return target;
        } else {
            throw new CopyElementException("Copie annulée ou aucun élément sélectionné comme cible de la copie.");
        }
    }

    protected abstract ObservableList<Preview> getChoices() throws CopyElementException;

    /**
     * Copie d'un ensemble d'éléments sélectionnés vers un élément cible.
     * <p>
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
    public abstract List<? extends Element> copyPojosTo(final Element targetedElement, final Element... pojosToCopy);


    //Getters
    public AbstractSIRSRepository getTargetRepo() {
        return targetRepo;
    }

    public Class getPojoClass() {
        return pojoClass;
    }

    public ObjectProperty<? extends Element> getContainer() {
        return container;
    }

    /**
     * Used to determine wether the object can be copied or not on a parent objet.
     * Example : a Desordre must be copied on a Troncon Digue. A DesordreDependance on a Dependance or an AH.
     * @return
     */
    public abstract boolean hasForeignParent();

    public abstract boolean isRapportEtude();

}
