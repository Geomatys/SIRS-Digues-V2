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
package fr.sirs.plugin.vegetation.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.*;
import fr.sirs.theme.ui.FXPositionablePane;
import fr.sirs.theme.ui.FXPositionableVegetationPane;
import fr.sirs.util.SirsStringConverter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;

import static fr.sirs.SIRS.EDITION_PREDICATE;
import static fr.sirs.plugin.vegetation.map.EditVegetationUtils.*;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPositionableForm extends BorderPane {

    @FXML
    private Button uiGoto;
    @FXML
    private Button uiDelete;
    @FXML
    private Button uiSave;

    @FXML
    private Label uiTypeLabel;
    @FXML
    private Label uiTroncon;
    @FXML
    private TextField uiDesignation;
    @FXML
    private ComboBox uiType;
    @FXML
    protected GridPane grid_pane_dens_haut_diam;
    @FXML
    private Label ui_densiteLabel;
    @FXML protected Spinner ui_densite;
    @FXML
    private Label ui_densiteIdLabel;
    @FXML protected ComboBox<Preview> ui_densiteId;
    @FXML
    private Label ui_hauteurLabel;
    @FXML
    protected Spinner ui_hauteur;
    @FXML
    private Label ui_hauteurIdLabel;
    @FXML protected ComboBox<Preview> ui_hauteurId;
    @FXML
    private Label ui_diametreLabel;
    @FXML protected Spinner ui_diametre;
    @FXML
    private Label ui_diametreIdLabel;
    @FXML protected ComboBox<Preview> ui_diametreId;
    @FXML
    protected Label ui_positionLabel;
    @FXML
    protected ComboBox<Preview> ui_positionId;
    @FXML
    private Label ui_typeCoteLabel;
    @FXML
    protected ComboBox<Preview> ui_typeCoteId;
    @FXML
    protected Label LabelContactEau;
    @FXML
    protected CheckBox checkContactEau;


    private final ObjectProperty<Positionable> positionableProperty = new SimpleObjectProperty<>();
    private Node editor = null;

    public FXPositionableForm() {
        SIRS.loadFXML(this, Positionable.class);

        positionableProperty.addListener(this::changed);
        uiGoto.disableProperty().bind(positionableProperty.isNull());
        uiDelete.disableProperty().bind(positionableProperty.isNull());
        uiSave.disableProperty().bind(positionableProperty.isNull());
        uiDesignation.disableProperty().bind(positionableProperty.isNull());
        uiType.disableProperty().bind(positionableProperty.isNull());
        ui_densite.disableProperty().bind(positionableProperty.isNull());
        ui_hauteur.disableProperty().bind(positionableProperty.isNull());
        ui_diametre.disableProperty().bind(positionableProperty.isNull());
        ui_densiteId.disableProperty().bind(positionableProperty.isNull());
        ui_hauteurId.disableProperty().bind(positionableProperty.isNull());
        ui_diametreId.disableProperty().bind(positionableProperty.isNull());

        uiTypeLabel.visibleProperty().bind(uiType.visibleProperty());
        uiTypeLabel.managedProperty().bind(uiTypeLabel.visibleProperty());
        uiType.managedProperty().bind(uiType.visibleProperty());

        // The whole GridPane is ignored if none of the attributes is visible.
        grid_pane_dens_haut_diam.managedProperty().bind(grid_pane_dens_haut_diam.visibleProperty());
        grid_pane_dens_haut_diam.visibleProperty().bind(
                Bindings.or(
                        Bindings.or(ui_densiteLabel.visibleProperty(), ui_densiteIdLabel.visibleProperty()),
                        Bindings.or(Bindings.or(ui_hauteurLabel.visibleProperty(), ui_hauteurIdLabel.visibleProperty()),
                                Bindings.or(ui_diametreLabel.visibleProperty(), ui_diametreIdLabel.visibleProperty()))));

        ui_densiteLabel.visibleProperty().bind(ui_densite.visibleProperty());
        ui_densiteIdLabel.visibleProperty().bind(ui_densiteId.visibleProperty());
        ui_densiteId.managedProperty().bind(ui_densiteId.visibleProperty());
        ui_densiteIdLabel.managedProperty().bind(ui_densiteIdLabel.visibleProperty());
        ui_densite.managedProperty().bind(ui_densite.visibleProperty());
        ui_densiteLabel.managedProperty().bind(ui_densiteLabel.visibleProperty());

        ui_hauteurLabel.visibleProperty().bind(ui_hauteur.visibleProperty());
        ui_hauteurIdLabel.visibleProperty().bind(ui_hauteurId.visibleProperty());
        ui_hauteurId.managedProperty().bind(ui_hauteurId.visibleProperty());
        ui_hauteurIdLabel.managedProperty().bind(ui_hauteurIdLabel.visibleProperty());
        ui_hauteur.managedProperty().bind(ui_hauteur.visibleProperty());
        ui_hauteurLabel.managedProperty().bind(ui_hauteurLabel.visibleProperty());

        ui_diametreLabel.visibleProperty().bind(ui_diametre.visibleProperty());
        ui_diametreIdLabel.visibleProperty().bind(ui_diametreId.visibleProperty());
        ui_diametreId.managedProperty().bind(ui_diametreId.visibleProperty());
        ui_diametreIdLabel.managedProperty().bind(ui_diametreIdLabel.visibleProperty());
        ui_diametre.managedProperty().bind(ui_diametre.visibleProperty());
        ui_diametreLabel.managedProperty().bind(ui_diametreLabel.visibleProperty());

        ui_densite.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));
        ui_hauteur.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));
        ui_diametre.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));
    }

    @FXML
    void delete(ActionEvent event) {
        final Positionable pos = positionableProperty.get();
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer la suppression de " + new SirsStringConverter().toString(pos),
                ButtonType.YES, ButtonType.NO);
        alert.initOwner(this.getScene().getWindow());
        alert.initModality(Modality.WINDOW_MODAL);
        final ButtonType res = alert.showAndWait().get();
        if (res == ButtonType.YES) {
            final AbstractSIRSRepository repo = Injector.getSession().getRepositoryForClass(pos.getClass());
            repo.remove(pos);
            positionableProperty().set(null);
        }
    }

    @FXML
    void save(ActionEvent event) {
        setFormProperties();
        final Positionable pos = positionableProperty.get();
        final AbstractSIRSRepository repo = Injector.getSession().getRepositoryForClass(pos.getClass());
        repo.update(pos);
        positionableProperty.set(null);
    }

    private void setFormProperties() {
        final Positionable pos = positionableProperty.get();

        Object cbValue = uiType.getValue();
        if (cbValue instanceof Preview) {
            cbValue = ((Preview) cbValue).getElementId();
        } else if (cbValue instanceof Element) {
            cbValue = ((Element) cbValue).getId();
        } else if (!(cbValue instanceof String)) {
            cbValue = null;
        }

        if (pos instanceof ZoneVegetation) {
            final ZoneVegetation zone = (ZoneVegetation) pos;
            zone.setContactEau(checkContactEau.isSelected());
            zone.setTypeCoteId(getElementIdOrnull(ui_typeCoteId));
            zone.setTypePositionId((getElementIdOrnull(ui_positionId)));

            if (zone instanceof InvasiveVegetation) {
                final InvasiveVegetation iv = (InvasiveVegetation) zone;
                iv.setTypeVegetationId((String) cbValue);
                iv.setDensiteId((getElementIdOrnull(ui_densiteId)));
            } else if (zone instanceof PeuplementVegetation) {
                final PeuplementVegetation pv = (PeuplementVegetation) zone;
                pv.setTypeVegetationId((String) cbValue);
                pv.setDensite((Double) ui_densite.getValueFactory().valueProperty().getValue());
                pv.setHauteur((Double) ui_hauteur.getValueFactory().valueProperty().getValue());
                pv.setDiametre((Double) ui_diametre.getValueFactory().valueProperty().getValue());
            } else if (zone instanceof ArbreVegetation) {
                final ArbreVegetation arbre = (ArbreVegetation) zone;
                arbre.setHauteurId(getElementIdOrnull(ui_hauteurId));
                arbre.setDiametreId(getElementIdOrnull(ui_diametreId));
            }
        }
    }

    @FXML
    void gotoForm(ActionEvent event) {
        setFormProperties();
        final Positionable pos = positionableProperty.get();
        if (pos != null) {
            Injector.getSession().showEditionTab(pos, EDITION_PREDICATE);
        }
    }

    public ObjectProperty<Positionable> positionableProperty() {
        return positionableProperty;
    }

    public void changed(ObservableValue<? extends Positionable> observable, Positionable oldValue, Positionable newValue) {

        if (oldValue != null) {
            uiDesignation.textProperty().unbindBidirectional(oldValue.designationProperty());
            if (oldValue instanceof PeuplementVegetation) {
                final PeuplementVegetation peuplement = (PeuplementVegetation) oldValue;
                initRefPreviewComboBox(uiType, RefTypePeuplementVegetation.class , peuplement.getTypeVegetationId());
                ui_hauteur.getValueFactory().valueProperty().unbindBidirectional(peuplement.hauteurProperty());
                ui_diametre.getValueFactory().valueProperty().unbindBidirectional(peuplement.diametreProperty());
                ui_densite.getValueFactory().valueProperty().unbindBidirectional(peuplement.densiteProperty());
            }
        }

        if (newValue instanceof PositionableVegetation) {
            PositionableVegetation pv = (PositionableVegetation) newValue;
            uiDesignation.textProperty().bindBidirectional(newValue.designationProperty());

            final Session session = Injector.getSession();
            AbstractSIRSRepository<ParcelleVegetation> repo = session.getRepositoryForClass(ParcelleVegetation.class);
            ParcelleVegetation sr = repo.get(pv.getForeignParentId());
            AbstractSIRSRepository<TronconDigue> tdrepo = session.getRepositoryForClass(TronconDigue.class);
            TronconDigue td = tdrepo.get(sr.getLinearId());
            uiTroncon.setText(SirsStringConverter.getDesignation(td));

            editor = new FXPositionableVegetationPane();
            ((FXPositionableVegetationPane) editor).setPositionable(newValue);
            ((FXPositionableVegetationPane) editor).disableFieldsProperty().set(false);

            uiType.setVisible(false);
            ui_densiteId.setVisible(false);
            ui_densite.setVisible(false);
            ui_hauteurId.setVisible(false);
            ui_hauteur.setVisible(false);
            ui_diametreId.setVisible(false);
            ui_diametre.setVisible(false);

            if (newValue instanceof ArbreVegetation) {
                ui_hauteurId.setVisible(true);
                ui_diametreId.setVisible(true);

                final ArbreVegetation arbre = (ArbreVegetation) newValue;
                initRefPreviewComboBox(ui_hauteurId, RefHauteurVegetation.class , arbre.getHauteurId());
                initRefPreviewComboBox(ui_diametreId, RefDiametreVegetation.class , arbre.getDiametreId());
            } else if (newValue instanceof HerbaceeVegetation) {
                // Default behaviour. Update if changed.
            } else if (newValue instanceof InvasiveVegetation) {
                uiType.setVisible(true);
                ui_densiteId.setVisible(true);

                final InvasiveVegetation iv = (InvasiveVegetation) newValue;
                initRefPreviewComboBox(uiType, RefTypeInvasiveVegetation.class , iv.getTypeVegetationId());
                initRefPreviewComboBox(ui_densiteId, RefDensiteVegetation.class , iv.getDensiteId());
            } else if (newValue instanceof PeuplementVegetation) {
                uiType.setVisible(true);
                ui_hauteur.setVisible(true);
                ui_diametre.setVisible(true);
                ui_densite.setVisible(true);

                final PeuplementVegetation peuplement = (PeuplementVegetation) newValue;
                initRefPreviewComboBox(uiType, RefTypePeuplementVegetation.class , peuplement.getTypeVegetationId());
                ui_hauteur.getValueFactory().valueProperty().bindBidirectional(peuplement.hauteurProperty());
                ui_diametre.getValueFactory().valueProperty().bindBidirectional(peuplement.diametreProperty());
                ui_densite.getValueFactory().valueProperty().bindBidirectional(peuplement.densiteProperty());
            }

            if (pv instanceof ZoneVegetation) {
                final ZoneVegetation zone = (ZoneVegetation) pv;
                if (zone != null) {
                    initRefPreviewComboBox(ui_typeCoteId, RefCote.class , zone.getTypeCoteId());
                    checkContactEau.setSelected(zone.getContactEau());
                    ui_positionId.setVisible(true);
                    initRefPreviewComboBox(ui_positionId, RefPosition.class , zone.getTypePositionId());
                }
            }


        } else if (newValue != null) {
            uiType.setVisible(false);
            editor = new FXPositionablePane();
            ((FXPositionablePane) editor).setPositionable(newValue);
            ((FXPositionablePane) editor).disableFieldsProperty().set(false);
        }

        setCenter(editor);
    }

}
