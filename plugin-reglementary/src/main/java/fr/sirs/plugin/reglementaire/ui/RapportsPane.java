
package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.Digue;
import fr.sirs.theme.ui.PojoTable;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * 
 *
 * @author Johann Sorel (Geomatys)
 */
public class RapportsPane extends BorderPane implements Initializable {

    @FXML private ListView<Digue> uiTroncons;
    @FXML private ChoiceBox<?> uiPrDebut;
    @FXML private DatePicker uiPeriodeFin;
    @FXML private ChoiceBox<?> uiPrFin;
    @FXML private ChoiceBox<?> uiSystemEndiguement;
    @FXML private DatePicker uiPeriodeDebut;
    @FXML private CheckBox uiCrerEntreeCalendrier;
    @FXML private TextField uiTitre;
    @FXML private BorderPane uiTablePane;

    private PojoTable uiTable;

    public RapportsPane() {
        SIRS.loadFXML(this, RapportsPane.class);
        Injector.injectDependencies(this);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        uiTable = new RapportsTable();

        uiTablePane.setCenter(uiTable);
    }

    @FXML
    void generateReport(ActionEvent event) {

    }

}
