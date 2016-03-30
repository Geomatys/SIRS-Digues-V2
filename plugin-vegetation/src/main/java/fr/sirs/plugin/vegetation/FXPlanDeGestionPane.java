
package fr.sirs.plugin.vegetation;

import fr.sirs.SIRS;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPlanDeGestionPane extends BorderPane {

    @FXML private Tab tabPlanification;
    @FXML private Tab tabParametrage;


    public FXPlanDeGestionPane() {
        SIRS.loadFXML(this);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public void initialize() {
        tabPlanification.setContent(new FXPlanificationPane());
        tabParametrage.setContent(new FXParametragePane());
    }
}
