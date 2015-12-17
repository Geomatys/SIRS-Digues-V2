package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.TronconDigue;
import static fr.sirs.plugin.vegetation.FXPlanTable.Mode.PLANIFICATION;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Samuel Andrés (Geomatys)
 */
public class FXPlanificationPane extends BorderPane {

    @FXML private GridPane uiHeader;
    private final Session session = Injector.getSession();

    public FXPlanificationPane() {
        SIRS.loadFXML(this, FXPlanificationPane.class);
        initialize();
    }

    private void initialize() {

        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Choix du tronçon

        final Session session = Injector.getSession();

        // Choix du tronçon.
        final ComboBox<Preview> uiTroncons = new ComboBox<>();
        SIRS.initCombo(uiTroncons, SIRS.observableList(session.getPreviews().getByClass(TronconDigue.class)).sorted(), null);
        final Label lblTroncon = new Label("Tronçon : ");
        lblTroncon.getStyleClass().add("label-header");

        uiHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        uiHeader.getStyleClass().add("blue-light");
        uiHeader.setHgap(10);
        uiHeader.setVgap(10);
        uiHeader.setPadding(new Insets(10, 10, 10, 10));
        uiHeader.add(lblTroncon, 1, 0);
        uiHeader.add(uiTroncons, 2, 0);
        final Label lblTitle = new Label("Planification des parcelles");
        lblTitle.setPadding(new Insets(0, 40, 0, 40));
        lblTitle.getStyleClass().add("label-header");
        lblTitle.setStyle("-fx-font-size: 1.5em;");
        uiHeader.add(lblTitle, 0, 0);


        if(VegetationSession.INSTANCE.planProperty().getValue()!=null){
            setCenter(new FXPlanTable(VegetationSession.INSTANCE.planProperty().getValue(), uiTroncons.getValue() == null? null : uiTroncons.getValue().getElementId(), PLANIFICATION, null, 0));
        }

        //on ecoute les changements de troncon et de plan
        final ChangeListener chgListener = (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) -> {
            if(VegetationSession.INSTANCE.planProperty().getValue()!=null){
                setCenter(new FXPlanTable(VegetationSession.INSTANCE.planProperty().getValue(), uiTroncons.getValue() == null? null : uiTroncons.getValue().getElementId(), PLANIFICATION, null, 0));
            }
            else setCenter(null);
        };

        VegetationSession.INSTANCE.planProperty().addListener(new WeakChangeListener(chgListener));
        uiTroncons.valueProperty().addListener(chgListener);
    }
}
