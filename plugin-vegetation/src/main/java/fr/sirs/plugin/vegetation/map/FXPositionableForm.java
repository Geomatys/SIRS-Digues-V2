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
package fr.sirs.plugin.vegetation.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.ArbreVegetation;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.HerbaceeVegetation;
import fr.sirs.core.model.InvasiveVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.PositionableVegetation;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefTypeInvasiveVegetation;
import fr.sirs.core.model.RefTypePeuplementVegetation;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.FXPositionablePane;
import fr.sirs.theme.ui.FXPositionableVegetationPane;
import fr.sirs.util.SirsStringConverter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPositionableForm extends BorderPane {

    @FXML private Button uiGoto;
    @FXML private Button uiDelete;
    @FXML private Button uiSave;

    @FXML private Label uiTypeLabel;
    @FXML private Label uiTroncon;
    @FXML private TextField uiDesignation;
    @FXML private ComboBox uiType;


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

        uiTypeLabel.managedProperty().bind(uiTypeLabel.visibleProperty());
        uiTypeLabel.visibleProperty().bind(uiType.visibleProperty());
        uiType.managedProperty().bind(uiType.visibleProperty());
    }

    @FXML
    void delete(ActionEvent event) {
        final Positionable pos = positionableProperty.get();
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer la suppression de "+ new SirsStringConverter().toString(pos),
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
        final Positionable pos = positionableProperty.get();

        Object cbValue = uiType.getValue();
        if (cbValue instanceof Preview) {
            cbValue = ((Preview)cbValue).getElementId();
        } else if (cbValue instanceof Element) {
            cbValue = ((Element)cbValue).getId();
        } else if (!(cbValue instanceof String)) {
            cbValue = null;
        }

        if(pos instanceof InvasiveVegetation){
            final InvasiveVegetation iv = (InvasiveVegetation) pos;
            iv.setTypeVegetationId((String)cbValue);
        }else if(pos instanceof PeuplementVegetation){
            final PeuplementVegetation pv = (PeuplementVegetation) pos;
            pv.setTypeVegetationId((String)cbValue);
        }

        final AbstractSIRSRepository repo = Injector.getSession().getRepositoryForClass(pos.getClass());
        repo.update(pos);
        positionableProperty.set(null);
    }

    @FXML
    void gotoForm(ActionEvent event) {
        final Positionable pos = positionableProperty.get();
        if(pos!=null){
            Injector.getSession().showEditionTab(pos);
        }
    }

    public ObjectProperty<Positionable> positionableProperty(){
        return positionableProperty;
    }

    public void changed(ObservableValue<? extends Positionable> observable, Positionable oldValue, Positionable newValue){

        if(newValue instanceof PositionableVegetation){
            PositionableVegetation pv = (PositionableVegetation) newValue;
            uiDesignation.textProperty().bindBidirectional(newValue.designationProperty());
            final Element parent = newValue.getParent();

            final String sreoid = newValue.getSystemeRepId();
            final Session session = Injector.getSession();
            AbstractSIRSRepository<ParcelleVegetation> repo = session.getRepositoryForClass(ParcelleVegetation.class);
            ParcelleVegetation sr = repo.get(pv.getForeignParentId());
            AbstractSIRSRepository<TronconDigue> tdrepo = session.getRepositoryForClass(TronconDigue.class);
            TronconDigue td = tdrepo.get(sr.getLinearId());
            uiTroncon.setText(SirsStringConverter.getDesignation(td));
        }

        if(newValue instanceof PositionableVegetation){
            editor = new FXPositionableVegetationPane();
            ((FXPositionableVegetationPane)editor).setPositionable(newValue);
            ((FXPositionableVegetationPane)editor).disableFieldsProperty().set(false);

            if(newValue instanceof ArbreVegetation){
                uiType.setVisible(false);
                
            }else if(newValue instanceof HerbaceeVegetation){
                uiType.setVisible(false);
                final HerbaceeVegetation hv = (HerbaceeVegetation) newValue;

            }else if(newValue instanceof InvasiveVegetation){
                uiType.setVisible(true);
                final InvasiveVegetation iv = (InvasiveVegetation) newValue;
                Previews previewRepository = Injector.getSession().getPreviews();
                SIRS.initCombo(uiType, FXCollections.observableList(
                    previewRepository.getByClass(RefTypeInvasiveVegetation.class)),
                    iv.getTypeVegetationId() == null? null : previewRepository.get(iv.getTypeVegetationId()));

            }else if(newValue instanceof PeuplementVegetation){
                uiType.setVisible(true);
                final PeuplementVegetation pv = (PeuplementVegetation) newValue;
                Previews previewRepository = Injector.getSession().getPreviews();
                SIRS.initCombo(uiType, FXCollections.observableList(
                    previewRepository.getByClass(RefTypePeuplementVegetation.class)),
                    pv.getTypeVegetationId() == null? null : previewRepository.get(pv.getTypeVegetationId()));
            }


        }else if(newValue instanceof Positionable){
            uiType.setVisible(false);
            editor = new FXPositionablePane();
            ((FXPositionablePane)editor).setPositionable(newValue);
            ((FXPositionablePane)editor).disableFieldsProperty().set(false);
        }

        setCenter(editor);
    }
    
}
