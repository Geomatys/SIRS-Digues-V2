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
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Matthieu Bastianelli (Geomatys)
 * @param <T>
 */
public class FXObjetEditPane<T extends Objet> extends FXAbstractEditOnTronconPane<T> {

    @FXML
    final ComboBox<String> uiGeomTypeBox = new ComboBox<>();
//    @FXML
//    final ComboBox<String> uiObjetType = new ComboBox<>();
    @FXML
    Button uiModifyObjet;

    /**
     *
     * @param map
     * @param typeName
     * @param clazz
     */
    public FXObjetEditPane(FXMap map, final String typeName, final Class clazz) {
        super(map, typeName, clazz, true, false);

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

        uiGeomTypeBox.setItems(FXCollections.observableArrayList("Ponctuel", "Linéaire", "Surfacique"));
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


    }

    /**
     * Retourne le type de géométrie à éditer
     * @return
     */
    public String getGeomType() {
        return uiGeomTypeBox.getSelectionModel().getSelectedItem();
    }

    /**
     * Ajout
     *
     * @param evt
     */
    void modifyObjet(ActionEvent evt) {
        throw new UnsupportedOperationException("Unsupported modifyObjet() yet.");
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
        throw new UnsupportedOperationException("Unsupported createObjet() yet.");
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

        throw new UnsupportedOperationException("Unsupported deleteObjets() yet.");
    }


    /*
     * TABLE UTILITIES
     */
    @Override
    void updateObjetTable(ObservableValue observable, SystemeReperage oldValue, SystemeReperage newValue) {
        throw new UnsupportedOperationException("Unsupported updateObjetTable yet.");
    }

}
