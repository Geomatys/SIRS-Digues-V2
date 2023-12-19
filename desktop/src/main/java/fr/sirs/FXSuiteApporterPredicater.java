/**
 * This file is part of SIRS-Digues 2.
 * <p>
 * Copyright (C) 2016, FRANCE-DIGUES,
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

package fr.sirs;

import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefSuiteApporter;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.property.SirsPreferences;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FXSuiteApporterPredicater extends VBox {

    private final ListView<Preview> choiceListSuiteAApporter = new ListView<>();
    private final Alert alert;
    @FXML
    protected ListView<Preview> uiSuiteApporterListView;

    @FXML
    protected Button uiDeleteSuitesBtn;
    @FXML
    protected Button uiAddSuitesBtn;

    private final ObservableList<Preview> availablePrefixes = FXCollections.observableArrayList();

    public FXSuiteApporterPredicater() {
        SIRS.loadFXML(this, FXSuiteApporterPredicater.class);

        final SirsStringConverter converter = new SirsStringConverter();

        final Callback<ListView<Preview>, ListCell<Preview>> cellFactory = new Callback<ListView<Preview>, ListCell<Preview>>() {
            @Override
            public ListCell<Preview> call(ListView<Preview> param) {
                return new ListCell<Preview>() {
                    @Override
                    protected void updateItem(Preview item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(null);
                        if (!empty && item != null) {
                            setText(converter.toString(item));
                        } else {
                            setText("");
                        }
                    }
                };
            }
        };

        final Previews previews = Injector.getSession().getPreviews();
        final List<Preview> refSuitePreviews = previews.getByClass(RefSuiteApporter.class);

        availablePrefixes.addAll(refSuitePreviews);

        choiceListSuiteAApporter.setCellFactory(cellFactory);
        choiceListSuiteAApporter.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        uiSuiteApporterListView.setCellFactory(cellFactory);
        uiSuiteApporterListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        choiceListSuiteAApporter.setItems(availablePrefixes);

        SirsPreferences.INSTANCE.showCasePropProperty().addListener((c, o, n) -> updateTextsToSirsPreferences());

        uiDeleteSuitesBtn.disableProperty().bind(Bindings.isNull(uiSuiteApporterListView.getSelectionModel().selectedItemProperty()));

        uiAddSuitesBtn.setGraphic(new ImageView(SIRS.ICON_ADD_BLACK));
        uiAddSuitesBtn.setText(null);
        uiDeleteSuitesBtn.setGraphic(new ImageView(SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TRASH_O,16, Color.BLACK),null)));
        uiDeleteSuitesBtn.setText(null);

        alert = new Alert(Alert.AlertType.CONFIRMATION, null, ButtonType.CANCEL, ButtonType.OK);
        alert.setResizable(true);
        alert.setWidth(400);
        alert.setHeaderText("Choisissez les suites à apporter");
    }

    private void updateTextsToSirsPreferences() {
        choiceListSuiteAApporter.refresh();
        uiSuiteApporterListView.refresh();
    }

    protected void addListener(final InvalidationListener parameterListener) {
        uiSuiteApporterListView.getItems().addListener(parameterListener);
    }

    protected ObservableList<Preview> getSuiteApporter() {
        return uiSuiteApporterListView.getItems();
    }

    @FXML
    void addSuites() {
        // Set height before showing dialog otherwise it does not adapt properly
        alert.setHeight(400);
        alert.getDialogPane().setContent(choiceListSuiteAApporter);
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
        if (ButtonType.OK.equals(result)) {
            final ObservableList<Preview> selectedItems = choiceListSuiteAApporter.getSelectionModel().getSelectedItems();
            if (!selectedItems.isEmpty()) {
                uiSuiteApporterListView.getItems().addAll(selectedItems);
                availablePrefixes.removeAll(selectedItems);
            }
        }
    }

    @FXML
    void deleteSuites() {
        final List<Preview> selectedItem =  new ArrayList<>(uiSuiteApporterListView.getSelectionModel().getSelectedItems());
        availablePrefixes.addAll(selectedItem);
        uiSuiteApporterListView.getItems().removeAll(selectedItem);
    }
}
