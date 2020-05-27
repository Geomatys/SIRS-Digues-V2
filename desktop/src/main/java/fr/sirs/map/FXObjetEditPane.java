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
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.PojoTableChoiceStage;
import fr.sirs.theme.ui.pojotable.ChoiceStage;
import fr.sirs.ui.Growl;
import java.util.ArrayList;
import java.util.logging.Level;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Matthieu Bastianelli (Geomatys)
 * @param <T>
 */
public class FXObjetEditPane<T extends Objet> extends FXAbstractEditOnTronconPane<T> {

    @FXML ToggleButton uiSelectTroncon;
    @FXML ComboBox<String> uiGeomTypeBox;
    @FXML ToggleButton uiModifyObjet;

    final ObjectProperty<String> geometryTypeProperty;

    /**
     *
     * @param map
     * @param typeName
     * @param clazz
     */
    public FXObjetEditPane(FXMap map, final String typeName, final Class clazz) {
        super(map, typeName, clazz, true, false);


        //etat des boutons sélectionné
        final ToggleGroup group = new ToggleGroup();
        uiPickTroncon.setToggleGroup(group);
        uiCreateObjet.setToggleGroup(group);
        uiModifyObjet.setToggleGroup(group);

        mode.addListener((observable, oldValue, newValue) -> {
            switch ((EditModeObjet) newValue) {
                case CREATE_OBJET:
                    group.selectToggle(uiCreateObjet);
                    break;
                case PICK_TRONCON:
                    group.selectToggle(uiPickTroncon);
                    break;
                case EDIT_OBJET:
                    group.selectToggle(uiModifyObjet);
                    break;
                default:
                    group.selectToggle(null);
                    break;
            }
        });

        uiModifyObjet.setOnAction(this::modifyObjet);


        uiGeomTypeBox.setItems(FXCollections.observableArrayList("Linéaire", "Ponctuel"));
        uiGeomTypeBox.getSelectionModel().selectFirst();
        geometryTypeProperty = new SimpleObjectProperty<>(getGeomType());
         uiGeomTypeBox.getSelectionModel().selectedItemProperty().addListener((o, old, n) -> {
            geometryTypeProperty.setValue(getGeomType());
        });
        uiObjetTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);


        uiSelectTroncon.setOnAction(e -> {
            final ObservableList<Preview> tronconPreviews = SIRS.observableList(new ArrayList<>(session.getPreviews().getByClass(TronconDigue.class)));
            final PojoTableChoiceStage<Element> stage = new ChoiceStage(session.getRepositoryForClass(TronconDigue.class), tronconPreviews, null, "Choix du tronçon d'appartenance", "Choisir");
            stage.showAndWait();
            tronconProp.setValue( (TronconDigue) stage.getRetrievedElement().get());
        });

        uiSelectTroncon.setGraphic(new ImageView(SIRS.ICON_ARROW_RIGHT_BLACK));

        tronconProp.addListener(this::updateObjetTable);

    }

    /**
     * Retourne le type de géométrie à éditer
     * @return
     */
    public final String getGeomType() {
        return uiGeomTypeBox.getSelectionModel().getSelectedItem();
    }

//    private void startCreateObjet(ActionEvent evt){
//        if(mode.get().equals(EditModeObjet.CREATE_OBJET)){
//            //on retourne on mode edition
//            mode.set(EditModeObjet.EDIT_OBJET);
//        }else{
//            mode.set(EditModeObjet.CREATE_OBJET);
//        }
//    }

    /**
     * Ajout
     *
     * @param evt
     */
    void modifyObjet(ActionEvent evt) {
        mode.setValue(EditModeObjet.EDIT_OBJET);
        geometryTypeProperty.setValue(getGeomType());
    }

    /**
     * Création d'un élément
     *
     * @param geom
     */
    @Override
    public void createObjet(final Point geom) { //uniquement un point ici, on veut pouvoir éditer un segment!

        if (getTronconFromProperty() == null) {
            Growl alert = new Growl(Growl.Type.WARNING, "Pour créer un nouvel élément, veuillez sélectionner un tronçon d'appartenance");
            alert.showAndFade();
            mode.setValue(EditModeObjet.PICK_TRONCON);
        } else {
            geometryTypeProperty.setValue(getGeomType());
            mode.setValue(EditModeObjet.CREATE_OBJET);
        }
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
    /**
     * Met à jour les éléments de la liste à partir du tronçon sélectionné.
     *
     * @param observable
     * @param oldValue
     * @param newValue
     */
    void updateObjetTable(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) {

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

            uiObjetTable.setItems(items);
        }
    }

}
