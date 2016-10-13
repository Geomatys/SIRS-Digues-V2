/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 *
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs;

import fr.sirs.core.model.RefConduiteFermee;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.ui.Growl;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import org.geotoolkit.util.collection.CloseableIterator;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXReseauFermePrintPane extends TemporalTronconChoicePrintPane {

    @FXML private Tab uiConduiteTypeChoice;

    @FXML private CheckBox uiOptionPhoto;
    @FXML private CheckBox uiOptionReseauOuvrage;

    private final TypeChoicePojoTable conduiteTypesTable = new TypeChoicePojoTable(RefConduiteFermee.class, "Types de conduites fermées");

    public FXReseauFermePrintPane(){
        super(FXReseauFermePrintPane.class);
        conduiteTypesTable.setTableItems(()-> (ObservableList) SIRS.observableList(Injector.getSession().getRepositoryForClass(RefConduiteFermee.class).getAll()));
        conduiteTypesTable.commentAndPhotoProperty().set(false);
        uiConduiteTypeChoice.setContent(conduiteTypesTable);
    }

    @FXML private void cancel(){

    }

    @FXML
    private void print(){
        Injector.getSession().getTaskManager().submit("Génération de fiches détaillées de réseaux hydrauliques fermés",
                () -> {
                    final Predicate userOptions = new TypeConduitePredicate()
                    .and(new TemporalPredicate())
                    .and(new PRPredicate<>());

                    final List<ReseauHydrauliqueFerme> toPrint = new ArrayList<>(100);
                    try (final CloseableIterator<ReseauHydrauliqueFerme> it = Injector.getSession().getRepositoryForClass(ReseauHydrauliqueFerme.class).getAllStreaming().iterator()) {
                        ReseauHydrauliqueFerme r;
                        while (it.hasNext()) {
                            r = it.next();
                            if (userOptions.test(r))
                                toPrint.add(r);
                        }
                    }

                    if (!toPrint.isEmpty())
                        Injector.getSession().getPrintManager().printReseaux(toPrint, uiOptionPhoto.isSelected(), uiOptionReseauOuvrage.isSelected());

                    return !toPrint.isEmpty();
                }
        ).setOnSucceeded(evt -> Platform.runLater(() -> {
            if (Boolean.TRUE.equals(evt.getSource().getValue())) {
                new Growl(Growl.Type.INFO, "La fiche a été générée avec succès.").showAndFade();
            } else {
                new Growl(Growl.Type.WARNING, "Aucun élément trouvé pour les critères demandés.").showAndFade();
            }
        }));
    }

    private class TypeConduitePredicate implements Predicate<ReseauHydrauliqueFerme> {

        final Set<String> acceptedIds;

        TypeConduitePredicate() {
            acceptedIds = conduiteTypesTable.getSelectedItems().stream()
                    .map(e -> e.getId())
                    .collect(Collectors.toSet());
        }

        @Override
        public boolean test(ReseauHydrauliqueFerme t) {
            return acceptedIds.isEmpty() || (t.getTypeConduiteFermeeId() != null && acceptedIds.contains(t.getTypeConduiteFermeeId()));
        }
    }
}
