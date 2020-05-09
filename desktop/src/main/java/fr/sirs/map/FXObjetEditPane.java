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
package fr.sirs.map;

import com.vividsolutions.jts.geom.Point;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.ui.Growl;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.util.TaskManager;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Matthieu Bastianelli (Geomatys)
 * @param <T>
 */
public class FXObjetEditPane<T extends Objet> extends FXAbstractEditOnTronconPane<T> {

    @FXML
    ComboBox<String> uiGeomTypeBox;
//    @FXML
//    final ComboBox<String> uiObjetType = new ComboBox<>();
    @FXML
    Button uiModifyObjet;

//    @FXML private FXTableView<T> uiObjetTable;

    /**
     *
     * @param map
     * @param typeName
     * @param clazz
     */
    public FXObjetEditPane(FXMap map, final String typeName, final Class clazz) {
        super(map, typeName, clazz, true, false);
//        SIRS.loadFXML(this);

//        final Stage stage = new Stage();
//        stage.getIcons().add(SIRS.ICON);
//        stage.setTitle("Création d'objet");
//        stage.initModality(Modality.WINDOW_MODAL);
//        stage.setAlwaysOnTop(true);
//        final GridPane gridPane = new GridPane();
//        gridPane.setVgap(10);
//        gridPane.setHgap(5);
//        gridPane.setPadding(new Insets(10));
//        gridPane.add(new Label("Choisir un type de d'objet"), 0, 0);
//        uiGeomTypeBox.setItems(FXCollections.observableArrayList("Ponctuel", "Linéaire", "Surfacique"));
        uiGeomTypeBox.setItems(FXCollections.observableArrayList("Ponctuel", "Linéaire"));
        uiGeomTypeBox.getSelectionModel().selectFirst();
//        final Label geomChoiceLbl = new Label("Choisir une forme géométrique");
//        geomChoiceLbl.visibleProperty().bind(geomTypeBox.visibleProperty());
//        gridPane.add(geomChoiceLbl, 0, 1);
//        gridPane.add(geomTypeBox, 1, 1);
//
//        final Button validateBtn = new Button("Valider");
//        validateBtn.setOnAction(event -> stage.close());
//        gridPane.add(validateBtn, 2, 3);
//
//        final Scene sceneChoices = new Scene(gridPane);
//        stage.setScene(sceneChoices);
//        stage.showAndWait();

//                final Class clazz = DesordreDependance.class;
//                objetHelper = new EditionHelper(map, objetLayer);

        tronconProp.addListener(this::updateObjetTable);

    }

    /**
     * Retourne le type de géométrie à éditer
     * @return
     */
    public String getGeomType() {
        return uiGeomTypeBox.getSelectionModel().getSelectedItem();
    }

//    @Override
//    public void save() {
//        save(getTronconProperty());
//    }
//
//    private void save(final TronconDigue td) {
//        final boolean mustSaveTd = saveTD.get();
//
//        if (mustSaveTd) {
//            saveTD.set(false);
//
//            TaskManager.INSTANCE.submit("Sauvegarde...", () -> {
//                if (td != null && mustSaveTd) {
//                    ((AbstractSIRSRepository) session.getRepositoryForClass(td.getClass())).update(td);
//                }
//            });
//        }
//    }

    /**
     * Ajout
     *
     * @param evt
     */
    void modifyObjet(ActionEvent evt) {
        mode.setValue(EditModeObjet.EDIT_OBJET);
//        throw new UnsupportedOperationException("Unsupported modifyObjet() yet.");
    }

//    private void startCreateObjet(ActionEvent evt){
//        if(mode.get().equals(ObjetEditMode.CREATE_OBJET)){
//            //on retourne on mode edition
//            mode.set(ObjetEditMode.EDIT_OBJET);
//        }else{
//            mode.set(ObjetEditMode.CREATE_OBJET);
//        }
//    }
    /**
     * Création d'un élément
     *
     * @param geom
     */
    @Override
    public void createObjet(final Point geom) { //uniquement un point ici, on veut pouvoir éditer un segment!

        if (getTronconProperty() == null) {
            Growl alert = new Growl(Growl.Type.WARNING, "Pour créer un nouvel élément, veuillez sélectionner un tronçon d'appartenance");
            alert.showAndFade();
            mode.setValue(EditModeObjet.PICK_TRONCON);
        } else {

            mode.setValue(EditModeObjet.CREATE_OBJET);
        }
//        throw new UnsupportedOperationException("Unsupported createObjet() yet.");
    }

    /**
     * Open a {@link ListView} to allow user to select one or more
     * {@link BorneDigue} to delete.
     *
     * Note : Once suppression is confirmed, we're forced to check all
     * {@link SystemeReperage} defined on the currently edited
     * {@link TronconDigue}, and update them if they use chosen bornes.
     *
     * @param e Event fired when deletion button has been fired.
     */
    @FXML
    @Override
    void deleteObjets(ActionEvent e) {

//        mode.setValue(EditModeObjet.NONE);

        throw new UnsupportedOperationException("Unsupported deleteObjets() yet.");
    }


    /*
     * TABLE UTILITIES
     */
    /**
     * Met à jour les éléments de la liste à partir du tronçon sélectionné.
     *
     * @param observable
     * @param oldValue
     * @param newValue
     */
    void updateObjetTable(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) {

//        if (oldValue != null) {
//            save(oldValue);
//        }

        if (newValue == null) {
            uiObjetTable.setItems(FXCollections.emptyObservableList());
        } else {
            final EditModeObjet current = getMode();
            if (current.equals(EditModeObjet.CREATE_OBJET) || current.equals(EditModeObjet.EDIT_OBJET)) {
                //do nothing
            } else {
                mode.set(EditModeObjet.EDIT_OBJET);
            }

            // By default, we'll sort bornes from uphill to downhill, but alow user to sort them according to available table columns.
            ObservableList items;
            try {
                items = getObjectListFromTroncon(null);
            } catch (Exception e) {
                SIRS.LOGGER.log(Level.WARNING, "Exception lors de la récupération des éléments du tronçon", e);
                items = null;
            }

//            sortedItems.comparatorProperty().bind(uiObjetTable.comparatorProperty());
            uiObjetTable.setItems(items);
        }
    }

}
