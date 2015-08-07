
package fr.sirs.plugin.vegetation;

import fr.sirs.SIRS;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXParametragePane extends BorderPane {
    
    @FXML private ListView<?> uiPlanList;
    @FXML private TextField uiPlanName;
    @FXML private TextField uiPlanFin;
    @FXML private TextField uiPlanDebut;
    @FXML private TableView<?> uiCoutTable;
    @FXML private TableView<?> uiTraitementTable;

    public FXParametragePane() {
        SIRS.loadFXML(this, FXParametragePane.class);
    }

    @FXML
    void planAdd(ActionEvent event) {

    }

    @FXML
    void planDelete(ActionEvent event) {

    }

    @FXML
    void coutAdd(ActionEvent event) {

    }

    @FXML
    void coutDelete(ActionEvent event) {

    }

    @FXML
    void traitementAdd(ActionEvent event) {

    }

    @FXML
    void traitementDelete(ActionEvent event) {

    }

}
