/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.theme.ui;

import fr.sirs.SIRS;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.component.AmenagementHydrauliqueViewRepository;
import fr.sirs.core.model.AmenagementHydrauliqueView;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import javafx.scene.layout.VBox;

/**
 * Graphic component that configures the amenagement hydraulique for the current
 * troncon.
 *
 * @author maximegavens
 */
public class FXAmenagementHydrauliqueAttachementPane extends BorderPane {

    @FXML private VBox uiVbox;
    @FXML private RadioButton uiAhOui;
    @FXML private RadioButton uiAhNon;

    private Label uiLabelAmenagementHydraulique = new Label();
    private Label uiLabelTypeAmenagementHydraulique = new Label();
    private Label uiLabelSuperficie = new Label();
    private Label uiLabelCapaciteStockage = new Label();
    private Label uiLabelProfondeurMoyenne = new Label();

    private final SimpleObjectProperty<TronconDigue> tronconProperty = new SimpleObjectProperty<TronconDigue>();

    public FXAmenagementHydrauliqueAttachementPane() {
        super();
        SIRS.loadFXML(this);

        // Init toggle yes or not Ah
        ToggleGroup toggle = new ToggleGroup();
        uiAhOui.setToggleGroup(toggle);
        uiAhNon.setToggleGroup(toggle);
        uiAhNon.setSelected(true);
        uiAhOui.setDisable(true);
        uiAhNon.setDisable(true);

        // Init label
        initLabel(uiLabelAmenagementHydraulique);
        initLabel(uiLabelTypeAmenagementHydraulique);
        initLabel(uiLabelSuperficie);
        initLabel(uiLabelCapaciteStockage);
        initLabel(uiLabelProfondeurMoyenne);

        tronconProperty.addListener(this::ahIdOnChange);
    }

    public SimpleObjectProperty<TronconDigue> targetProperty() {
        return tronconProperty;
    }

    private void ahIdOnChange(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newElement) {
        // update value
        if (newElement.getAmenagementHydrauliqueId() != null) {
            final AmenagementHydrauliqueViewRepository repo = InjectorCore.getBean(AmenagementHydrauliqueViewRepository.class);
            List<AmenagementHydrauliqueView> ahvList = repo.getAmenagementHydrauliqueView(newElement.getAmenagementHydrauliqueId());
            if (!ahvList.isEmpty()) {
                updateValue(ahvList.get(0));
                uiAhOui.setSelected(true);
            } else {
                updateValue(null);
                uiAhOui.setSelected(false);
            }
        } else {
            updateValue(null);
            uiAhOui.setSelected(false);
        }
        // update ui
        displayAhDetail();
    }

    private void displayAhDetail() {
        if (uiVbox.getChildren().size() >= 2) {
            uiVbox.getChildren().remove(1, uiVbox.getChildren().size());
        }
        if (uiAhOui.isSelected()) {
            final HBox h0 = buildHbox("Nom", uiLabelAmenagementHydraulique);
            final HBox h1 = buildHbox("Type", uiLabelTypeAmenagementHydraulique);
            final HBox h2 = buildHbox("Superficie (m²)", uiLabelSuperficie);
            final HBox h3 = buildHbox("Capacité de Stockage (m³)", uiLabelCapaciteStockage);
            final HBox h4 = buildHbox("Profondeur moyenne (m)", uiLabelProfondeurMoyenne);
            uiVbox.getChildren().add(h0);
            uiVbox.getChildren().add(h1);
            uiVbox.getChildren().add(h2);
            uiVbox.getChildren().add(h3);
            uiVbox.getChildren().add(h4);
        }
    }

    private void initLabel(final Label label) {
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(5));
        label.setPrefWidth(USE_COMPUTED_SIZE);
    }

    private void updateValue(final AmenagementHydrauliqueView ah) {
        if (ah != null) {
            uiLabelAmenagementHydraulique.setText((new SirsStringConverter()).toString(ah));
            uiLabelTypeAmenagementHydraulique.setText(ah.getType());
            uiLabelSuperficie.setText(ah.getSuperficie());
            uiLabelCapaciteStockage.setText(ah.getCapaciteStockage());
            uiLabelProfondeurMoyenne.setText(ah.getProfondeurMoyenne());
            // update the value of ah of the current troncon
            tronconProperty.get().setAmenagementHydrauliqueId(ah.getId());
        } else {
            uiLabelAmenagementHydraulique.setText("");
            uiLabelTypeAmenagementHydraulique.setText("");
            uiLabelSuperficie.setText("");
            uiLabelCapaciteStockage.setText("");
            uiLabelProfondeurMoyenne.setText("");
            // update the value of ah of the current troncon
            tronconProperty.get().setAmenagementHydrauliqueId(null);
        }
    }

    private HBox buildHbox(final String labelString, final Node node) {
        final Label label = new Label(labelString);
        initLabel(label);

        final Separator sep = new Separator();
        sep.setVisible(false);
        HBox.setHgrow(sep, Priority.ALWAYS);

        HBox hbox = new HBox(label, sep, node);
        hbox.setPrefWidth(USE_COMPUTED_SIZE);
        hbox.setPadding(new Insets(5));
        return hbox;
    }
}
