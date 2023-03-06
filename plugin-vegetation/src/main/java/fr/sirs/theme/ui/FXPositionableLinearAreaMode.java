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
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.GeometryType;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.PositionableVegetation;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import fr.sirs.util.ConvertPositionableCoordinates;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 * Edition des bornes d'un {@link Positionable}.
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPositionableLinearAreaMode extends FXPositionableAbstractLinearMode {

    public static final String MODE = "LINEAR_AREA";

    //area
    @FXML private Spinner<Double> uiStartNear;
    @FXML private Spinner<Double> uiStartFar;
    @FXML private Spinner<Double> uiEndNear;
    @FXML private Spinner<Double> uiEndFar;

    // Libellés à cacher si c'est un ponctuel
    @FXML private Label lblFin;
    @FXML private Label lblStartNear;
    @FXML private Label lblStartFar;
    @FXML private Label lblEndNear;
    @FXML private Label lblEndFar;

    private final String originalLblStartNear;

    private final BooleanProperty pctProp = new SimpleBooleanProperty(false);

    final ChangeListener<String> typeCoteChangeListener = (ObservableValue<? extends String> observable, String oldValue, String newValue) -> buildGeometry();

    public FXPositionableLinearAreaMode() {
        super();

        uiStartNear.disableProperty().bind(disableProperty);
        uiStartFar.disableProperty().bind(disableProperty);
        uiEndNear.disableProperty().bind(disableProperty);
        uiEndFar.disableProperty().bind(disableProperty);
        uiStartNear.setEditable(true);
        uiStartFar.setEditable(true);
        uiEndNear.setEditable(true);
        uiEndFar.setEditable(true);

        uiStartNear.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));
        uiStartFar.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));
        uiEndNear.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));
        uiEndFar.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));

        final ChangeListener chgListener = (ObservableValue observable, Object oldValue, Object newValue) -> coordChange(false);
        uiStartNear.valueProperty().addListener(chgListener);
        uiStartFar.valueProperty().addListener(chgListener);
        uiEndNear.valueProperty().addListener(chgListener);
        uiEndFar.valueProperty().addListener(chgListener);

        //on cache certain elements quand c'est un ponctuel
        lblFin.visibleProperty().bind(pctProp);
        uiBorneEnd.visibleProperty().bind(pctProp);
        uiAvalEnd.visibleProperty().bind(pctProp);
        uiDistanceEnd.visibleProperty().bind(pctProp);
        uiAmontEnd.visibleProperty().bind(pctProp);
        uiEndNear.visibleProperty().bind(pctProp);
        uiEndFar.visibleProperty().bind(pctProp);
        lblStartFar.visibleProperty().bind(pctProp);
        uiStartFar.visibleProperty().bind(pctProp);
        lblEndNear.visibleProperty().bind(pctProp);
        lblEndFar.visibleProperty().bind(pctProp);
        lblFin.managedProperty().bind(pctProp);
        uiBorneEnd.managedProperty().bind(pctProp);
        uiAvalEnd.managedProperty().bind(pctProp);
        uiDistanceEnd.managedProperty().bind(pctProp);
        uiAmontEnd.managedProperty().bind(pctProp);
        uiEndNear.managedProperty().bind(pctProp);
        uiEndFar.managedProperty().bind(pctProp);
        lblStartFar.managedProperty().bind(pctProp);
        uiStartFar.managedProperty().bind(pctProp);
        lblEndNear.managedProperty().bind(pctProp);
        lblEndFar.managedProperty().bind(pctProp);

        originalLblStartNear = lblStartNear.getText();
        lblStartNear.textProperty().bind(new StringBinding() {
            {bind(pctProp);}
            @Override
            protected String computeValue() {
                if(pctProp.get()){
                    return originalLblStartNear;
                }
                else return "Point unique";
            }
        });

        positionableProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue instanceof ZoneVegetation) {
                ((ZoneVegetation) newValue).typeCoteIdProperty().removeListener(typeCoteChangeListener);
            }
            if (newValue instanceof ZoneVegetation) {
                ((ZoneVegetation) newValue).typeCoteIdProperty().addListener(typeCoteChangeListener);
            }
        });
    }

    @Override
    public String getID() {
        return MODE;
    }

    @Override
    public void updateFields(){
        super.updateFields();
        setReseting(true);

        final PositionableVegetation pos = (PositionableVegetation) positionableProperty().get();
        final String mode = pos.getGeometryMode();

        if (mode == null || MODE.equals(mode)) {
            //on assigne les valeurs sans changement
            uiStartNear.getValueFactory().setValue(pos.getDistanceDebutMin());
            uiStartFar.getValueFactory().setValue(pos.getDistanceDebutMax());
            uiEndNear.getValueFactory().setValue(pos.getDistanceFinMin());
            uiEndFar.getValueFactory().setValue(pos.getDistanceFinMax());
        } else if (pos.getGeometry() != null) {
            //on calcule les valeurs en fonction des points de debut et fin

            uiStartNear.getValueFactory().setValue(pos.getDistanceDebutMin());
            uiStartFar.getValueFactory().setValue(pos.getDistanceDebutMax());
            uiEndNear.getValueFactory().setValue(pos.getDistanceFinMin());
            uiEndFar.getValueFactory().setValue(pos.getDistanceFinMax());
        } else {
            uiStartNear.getValueFactory().setValue(0.0);
            uiStartFar.getValueFactory().setValue(0.0);
            uiEndNear.getValueFactory().setValue(0.0);
            uiEndFar.getValueFactory().setValue(0.0);
        }

        //on cache certains champs si c'est un ponctuel
        pctProp.unbind();
        pctProp.bind(pos.geometryTypeProperty().isNotEqualTo(GeometryType.PONCTUAL));

        setReseting(false);
    }

    @Override
    public void buildGeometry(){
        //sauvegarde des propriétés
        final ZoneVegetation positionable = (ZoneVegetation) positionableProperty().get();

        // On ne met la géométrie à jour depuis ce panneau que si on est dans son mode.
        if (!MODE.equals(positionable.getGeometryMode())) return;

        setValuesFromUi(positionable);
        positionable.setDistanceDebutMin(uiStartNear.getValue());
        positionable.setDistanceDebutMax(uiStartFar.getValue());
        positionable.setDistanceFinMin(uiEndNear.getValue());
        positionable.setDistanceFinMax(uiEndFar.getValue());

        PluginVegetation.buildLinearGeometry(positionable, uiSRs.getValue(), MODE);
    }

}
