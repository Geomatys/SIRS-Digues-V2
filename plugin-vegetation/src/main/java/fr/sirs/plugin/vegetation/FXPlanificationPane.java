package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.TronconDigue;
import static fr.sirs.plugin.vegetation.FXPlanTable.Mode.PLANIFICATION;
import fr.sirs.util.SirsStringConverter;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Samuel Andrés (Geomatys)
 */
public class FXPlanificationPane extends BorderPane {

    @FXML private GridPane uiHeader;
    private final Session session = Injector.getSession();
    private final ChoiceBox<TronconDigue> tronconChoiceBox = new ChoiceBox<>();
    
    public FXPlanificationPane() {
        SIRS.loadFXML(this, FXPlanificationPane.class);
        initialize();
    }

    private void initialize() {

        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Choix du tronçon
        tronconChoiceBox.setMaxWidth(300);
        tronconChoiceBox.setConverter(new SirsStringConverter());
        final List<TronconDigue> allTroncon = session.getRepositoryForClass(TronconDigue.class).getAll();
        allTroncon.add(0, null);
        tronconChoiceBox.setItems(FXCollections.observableArrayList(allTroncon));
        final Label lblTroncon = new Label("Tronçon : ");
        lblTroncon.getStyleClass().add("label-header");
        final HBox tronconBox = new HBox(10, lblTroncon, tronconChoiceBox);
        tronconBox.getStyleClass().add("blue-light");
        tronconBox.setPadding(new Insets(10));

        uiHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        uiHeader.getStyleClass().add("blue-light");
        uiHeader.setHgap(10);
        uiHeader.setVgap(10);
        uiHeader.setPadding(new Insets(10, 10, 10, 10));
        uiHeader.add(lblTroncon, 1, 0);
        uiHeader.add(tronconChoiceBox, 2, 0);
        final Label lblTitle = new Label("Planification des parcelles");
        lblTitle.setPadding(new Insets(0, 40, 0, 40));
        lblTitle.getStyleClass().add("label-header");
        lblTitle.setStyle("-fx-font-size: 1.5em;");
        uiHeader.add(lblTitle, 0, 0);
        

        if(VegetationSession.INSTANCE.planProperty().getValue()!=null){
            setCenter(new FXPlanTable(VegetationSession.INSTANCE.planProperty().getValue(), tronconChoiceBox.getValue(), PLANIFICATION, null, 0));
        }

        //on ecoute les changements de troncon et de plan
        final ChangeListener chgListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(VegetationSession.INSTANCE.planProperty().getValue()!=null){
                    setCenter(new FXPlanTable(VegetationSession.INSTANCE.planProperty().getValue(), tronconChoiceBox.getValue(), PLANIFICATION, null, 0));
                }
                else setCenter(null);
            }
        };

        VegetationSession.INSTANCE.planProperty().addListener(new WeakChangeListener(chgListener));
        tronconChoiceBox.valueProperty().addListener(chgListener);
    }
}
