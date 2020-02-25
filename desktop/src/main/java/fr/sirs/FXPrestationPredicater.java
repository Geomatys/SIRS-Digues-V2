/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs;

import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import java.util.ArrayList;
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
    @FXML protected ComboBox<Prestation> uiChoicePrestation;
    @FXML protected ListView<Prestation> uiListPrestation;
    @FXML protected Button uiAddPrestation;
    @FXML protected Button uiRemovePrestation;

    public FXPrestationPredicater() {
        SIRS.loadFXML(this, FXPrestationPredicater.class);

//        final ObservableList choices = SIRS.observableList(new ArrayList<>(Injector.getSession().getPreviews().getByClass(Prestation.class)));
        final ObservableList choices = SIRS.observableList(new ArrayList<>(Injector.getSession().getRepositoryForClass(Prestation.class).getAll()));
        SIRS.initCombo(uiChoicePrestation, choices, null);
        uiListPrestation.setItems(FXCollections.observableList(new ArrayList<>()));
        uiListPrestation.setCellFactory(TextFieldListCell.forListView(new SirsStringConverter()));
        uiListPrestation.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        uiAddPrestation.setOnAction(this::addPrestationToFilter);
        uiRemovePrestation.setOnAction(this::removePrestationToFilter);
    }


    private void addPrestationToFilter(final ActionEvent evt) {
        final Prestation added = uiChoicePrestation.getSelectionModel().getSelectedItem();
        ObservableList<Prestation> items = uiListPrestation.getItems();

        if (!items.contains(added))
            items.add(added);
        if (!uiOptionPrestation.selectedProperty().get())
            uiOptionPrestation.selectedProperty().setValue(Boolean.TRUE);

    }
    private void removePrestationToFilter(final ActionEvent evt) {
        final ObservableList<Prestation> prestations = uiListPrestation.getItems();
        prestations.removeAll(uiListPrestation.getSelectionModel().getSelectedItems());
        if (prestations.isEmpty()) {
            uiOptionPrestation.selectedProperty().setValue(Boolean.FALSE);
        }
    }

}
