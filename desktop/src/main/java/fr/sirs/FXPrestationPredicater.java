/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs;

import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AvecPrestations;
import fr.sirs.core.model.GlobalPrestation;
import fr.sirs.core.model.Prestation;
import fr.sirs.util.SirsStringConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.VBox;

/**
 *
 * @author Matthieu Bastianelli (Geomatys)
 */
public class FXPrestationPredicater extends VBox {

    @FXML protected CheckBox uiOptionPrestation;

    @FXML protected ComboBox<GlobalPrestation> uiChoiceGlobalPrestation;
    @FXML protected Button uiAddGlobalPrestation;

    @FXML protected ComboBox<Prestation> uiChoicePrestation;
    @FXML protected Button uiAddPrestation;

    @FXML protected ListView<Prestation> uiListPrestation;
    @FXML protected Button uiRemovePrestation;

    private final AbstractSIRSRepository<GlobalPrestation> repositoryGP;
    private final AbstractSIRSRepository<Prestation> repositoryP;
    private final ObservableList<Prestation> selectedPrestations;

    private final SelectedPrestationPredicate predicate;

    public FXPrestationPredicater() {
        SIRS.loadFXML(this, FXPrestationPredicater.class);

        repositoryGP = Injector.getSession().getRepositoryForClass(GlobalPrestation.class);
        repositoryP = Injector.getSession().getRepositoryForClass(Prestation.class);

        if (repositoryP == null) {
            throw new IllegalStateException("Try to instantiate FXPrestationPredicater but failed to get the Prestation repository.");
        }
        if (repositoryGP == null) {
            throw new IllegalStateException("Try to instantiate FXPrestationPredicater but failed to get the Prestation repository.");
        }

//        final ObservableList choices = SIRS.observableList(new ArrayList<>(Injector.getSession().getPreviews().getByClass(Prestation.class)));
        final ObservableList choicesP = SIRS.observableList(new ArrayList<>(repositoryP.getAll()));
        SIRS.initCombo(uiChoicePrestation, choicesP, null);
        final ObservableList choicesGP = SIRS.observableList(new ArrayList<>(repositoryGP.getAll()));
        SIRS.initCombo(uiChoiceGlobalPrestation, choicesGP, null);
        selectedPrestations = FXCollections.observableList(new ArrayList<>());
        uiListPrestation.setItems(selectedPrestations);
        uiListPrestation.setCellFactory(TextFieldListCell.forListView(new SirsStringConverter()));
        uiListPrestation.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        uiAddGlobalPrestation.setOnAction(this::addGlobalPrestationToFilter);
        uiAddPrestation.setOnAction(this::addChoosenPrestationToFilter);
        uiRemovePrestation.setOnAction(this::removePrestationToFilter);
        predicate = new SelectedPrestationPredicate();
        uiOptionPrestation.selectedProperty().addListener(predicate.listener);
        selectedPrestations.addListener(predicate.listener);

    }

    /**
     * Add selected {@link Prestation}s from {@link #uiChoicePrestation} to the
     * filter and activate it.
     *
     * @param evt
     */
    private void addChoosenPrestationToFilter(final ActionEvent evt) {
        final Prestation choosen = uiChoicePrestation.getSelectionModel().getSelectedItem();
        addPrestationToFilter(choosen);


    }

    /**
     * Add input {@link Prestation} to the filter and activate it.
     *
     * @param prestation
     */
    private void addPrestationToFilter(final Prestation prestation) {
        if (!selectedPrestations.contains(prestation))
            selectedPrestations.add(prestation);
        if (!uiOptionPrestation.selectedProperty().get())
            uiOptionPrestation.selectedProperty().setValue(Boolean.TRUE);
    }

    /**
     * Add all prestations associated with a selected {@link GlobalPrestation}
     * from {@link #uiChoiceGlobalPrestation} to the filter and activate it.
     *
     * @param evt
     */
    private void addGlobalPrestationToFilter(final ActionEvent evt) {
        final GlobalPrestation added = uiChoiceGlobalPrestation.getSelectionModel().getSelectedItem();

        if(added ==null) return;

        final List<Prestation> prestations = repositoryP.get(added.getPrestationIds());
        if (prestations != null) {
            prestations.forEach(this::addPrestationToFilter);
        }


    }
    private void removePrestationToFilter(final ActionEvent evt) {
        selectedPrestations.removeAll(uiListPrestation.getSelectionModel().getSelectedItems());
        if (selectedPrestations.isEmpty()) {
            uiOptionPrestation.selectedProperty().setValue(Boolean.FALSE);
        }
    }

    public Predicate<AvecPrestations> getPredicate() {
        return predicate;
    }


    private class SelectedPrestationPredicate implements Predicate<AvecPrestations> {

        private boolean toApply;

        final InvalidationListener listener = c -> predicate.determineToApply();

        /**
         * Prestations to test.
         * Nullable if it isn't to apply
         */
//        private final List<Prestation> selectedPrestations;

        private SelectedPrestationPredicate() {
            determineToApply();
        }

        private void determineToApply() {
            this.toApply = (uiOptionPrestation.isSelected()) && (!selectedPrestations.isEmpty());
        }

        @Override
        public boolean test(AvecPrestations input) {
            if (!toApply) {
                return true;
            }
            final List<String> prestationIds = input.getPrestationIds();

            if ( (prestationIds == null) || (prestationIds.isEmpty()) ) {
                SIRS.LOGGER.log(Level.INFO, "Try to filter elements to print from null or empty list of prestations");
                return false;
            }

            final List<Prestation> inputPrestations = repositoryP.get(prestationIds.toArray(new String[prestationIds.size()]));

            return (selectedPrestations.stream().anyMatch((p) -> (inputPrestations.contains(p))));
        }

    }

}
