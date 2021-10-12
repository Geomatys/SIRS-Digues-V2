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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

/**
 * Graphic component that configures the amenagement hydraulique for the current
 * troncon.
 *
 * @author Maxime Gavens (Geomatys)
 */
public class FXAmenagementHydrauliqueAttachementPane extends BorderPane {

    @FXML private GridPane uiGridPane;
    @FXML private RadioButton uiWithAh;
    @FXML private RadioButton uiWithoutAh;

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
        uiWithAh.setToggleGroup(toggle);
        uiWithoutAh.setToggleGroup(toggle);
        uiWithoutAh.setSelected(true);
        uiWithAh.setDisable(true);
        uiWithoutAh.setDisable(true);

        // Init label
        initLabel(uiLabelAmenagementHydraulique);
        initLabel(uiLabelTypeAmenagementHydraulique);
        initLabel(uiLabelSuperficie);
        initLabel(uiLabelCapaciteStockage);
        initLabel(uiLabelProfondeurMoyenne);

        tronconProperty.addListener(this::onTronconChange);
    }

    public SimpleObjectProperty<TronconDigue> targetProperty() {
        return tronconProperty;
    }

    /*
    * Updates the displayed information of the AH attached to the troncon
    */
    private void onTronconChange(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newElement) {
        // update value
        if (newElement.getAmenagementHydrauliqueId() != null) {
            // If the current troncon is attached to a AH,
            // we retrieve the view containing the information of the AH to display.
            final AmenagementHydrauliqueViewRepository repo = InjectorCore.getBean(AmenagementHydrauliqueViewRepository.class);
            List<AmenagementHydrauliqueView> ahvList = repo.getAmenagementHydrauliqueView(newElement.getAmenagementHydrauliqueId());
            // The list is supposed to contain exactly one value.
            if (!ahvList.isEmpty()) {
                updateValue(ahvList.get(0));
                uiWithAh.setSelected(true);
            } else {
                // If not,
                // the information sheet is empty
                updateValue(null);
                uiWithAh.setSelected(false);
            }
        } else {
            // If not,
            // the infomrtation sheet is empty
            updateValue(null);
            uiWithAh.setSelected(false);
        }
        // update of the interface
        displayAhDetail();
    }

    private void displayAhDetail() {
        if (uiGridPane.getChildren().size() >= 4) {
            uiGridPane.getChildren().remove(3, uiGridPane.getChildren().size());
        }
        if (uiWithAh.isSelected()) {
            appendRow(1, "Nom", uiLabelAmenagementHydraulique);
            appendRow(2, "Type", uiLabelTypeAmenagementHydraulique);
            appendRow(3, "Superficie (m²)", uiLabelSuperficie);
            appendRow(4, "Capacité de Stockage (m³)", uiLabelCapaciteStockage);
            appendRow(5, "Profondeur moyenne (m)", uiLabelProfondeurMoyenne);
        }
    }

    private void appendRow(final int posRow, final String title, final Node node) {
        final Label label = new Label(title);

        initLabel(label);
        uiGridPane.add(label, 0, posRow);
        uiGridPane.add(node, 2, posRow);
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
