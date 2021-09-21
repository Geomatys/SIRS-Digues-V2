/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.GlobalRepository;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.TronconDigue;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
    private ComboBox<String> uiComboAh = new ComboBox<>();
    private Label uiLabelTypeAmenagementHydraulique = new Label();
    private Label uiLabelSuperficie = new Label();
    private Label uiLabelCapaciteStockage = new Label();
    private Label uiLabelProfondeurMoyenne = new Label();

    private final SimpleObjectProperty<TronconDigue> tronconProperty = new SimpleObjectProperty<TronconDigue>();
    private final SimpleBooleanProperty disableFieldsProperty = new SimpleBooleanProperty(false);

    public FXAmenagementHydrauliqueAttachementPane() {
        super();
        SIRS.loadFXML(this);

        // Init toggle yes or not Ah
        ToggleGroup toggle = new ToggleGroup();
        uiAhOui.setToggleGroup(toggle);
        uiAhNon.setToggleGroup(toggle);
        uiAhNon.setSelected(true);
        uiAhOui.disableProperty().bind(disableFieldsProperty);
        uiAhNon.disableProperty().bind(disableFieldsProperty);
        uiAhOui.setOnAction((event) -> updateIsAh());
        uiAhNon.setOnAction((event) -> updateIsAh());

        // Init ah combobox
        uiComboAh.disableProperty().bind(disableFieldsProperty);
        //ObservableList<AmenagementHydraulique> observableList = FXCollections.observableArrayList(SIRS.observableList(Injector.getSession().getRepositoryForClass(AmenagementHydraulique.class).getAll()));
        //SIRS.initCombo(uiComboAh, observableList, observableList.isEmpty() ? null : observableList.get(0));
        //uiComboAh.setOnAction((event) -> updateValue(uiComboAh.getValue()));

        // Init label
        initLabel(uiLabelTypeAmenagementHydraulique);
        initLabel(uiLabelSuperficie);
        initLabel(uiLabelCapaciteStockage);
        initLabel(uiLabelProfondeurMoyenne);

        tronconProperty.addListener(this::ahIdOnChange);
    }

    public SimpleObjectProperty<TronconDigue> targetProperty() {
        return tronconProperty;
    }

    public BooleanProperty disableFieldsProperty(){
        return disableFieldsProperty;
    }

    private void ahIdOnChange(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newElement) {
        // update value
        GlobalRepository bean = Injector.getBean(GlobalRepository.class);
        if (newElement.getAmenagementHydrauliqueId() != null) {
            //AmenagementHydraulique ah = Injector.getSession().getRepositoryForClass(AmenagementHydraulique.class).get(newElement.getAmenagementHydrauliqueId());
            //updateValue(ah);
            uiAhOui.setSelected(true);
            //uiComboAh.getSelectionModel().select(ah);
        } else {
            //updateValue(null);
            uiAhOui.setSelected(false);
            uiComboAh.getSelectionModel().select(null);
        }
        // update ui
        displayAhDetail();
    }

    private void displayAhDetail() {
        if (uiVbox.getChildren().size() >= 2) {
            uiVbox.getChildren().remove(1, uiVbox.getChildren().size());
        }
        if (uiAhOui.isSelected()) {
            final HBox h0 = buildHbox("Aménagement hydraulique", uiComboAh);
            final HBox h1 = buildHbox("Type aménagement hydraulique", uiLabelTypeAmenagementHydraulique);
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

    private void updateIsAh() {
        if (uiAhOui.isSelected()) {
            //tronconProperty.get().setAmenagementHydrauliqueId(uiComboAh.getValue() == null ? null : uiComboAh.getValue().getId());
        } else {
            tronconProperty.get().setAmenagementHydrauliqueId(null);
        }
        displayAhDetail();
    }

    private void initLabel(final Label label) {
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(5));
        label.setPrefWidth(USE_COMPUTED_SIZE);
    }

//    private void updateValue(final AmenagementHydraulique ah) {
//        if (ah != null) {
//            uiLabelTypeAmenagementHydraulique.setText(ah.getType());
//            uiLabelSuperficie.setText(Float.toString(ah.getSuperficie()));
//            uiLabelCapaciteStockage.setText(Float.toString(ah.getCapaciteStockage()));
//            uiLabelProfondeurMoyenne.setText(Float.toString(ah.getProfondeurMoyenne()));
//            // update the value of ah of the current troncon
//            tronconProperty.get().setAmenagementHydrauliqueId(ah.getId());
//        } else {
//            uiLabelTypeAmenagementHydraulique.setText("");
//            uiLabelSuperficie.setText("");
//            uiLabelCapaciteStockage.setText("");
//            uiLabelProfondeurMoyenne.setText("");
//            // update the value of ah of the current troncon
//            tronconProperty.get().setAmenagementHydrauliqueId(null);
//        }
//    }

    private HBox buildHbox(final String labelString, final Node node) {
        final Label label = new Label(labelString);
        initLabel(label);

        final HBox hbox = new HBox();
        hbox.setPrefWidth(USE_COMPUTED_SIZE);

        final Separator sep = new Separator();
        sep.setOrientation(Orientation.VERTICAL);
        sep.setVisible(false);

        hbox.getChildren().add(label);
        hbox.getChildren().add(node);
        return hbox;
    }
}
