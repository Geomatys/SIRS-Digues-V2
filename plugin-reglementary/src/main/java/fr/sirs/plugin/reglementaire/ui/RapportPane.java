
package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.RapportModeleObligationReglementaire;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.apache.sis.util.ArgumentChecks;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class RapportPane extends GridPane implements Initializable {

    @FXML private TextField uiTitre;
    @FXML private BorderPane uiTablePane;

    private final RapportModeleObligationReglementaire rapport;
    private RapportSectionTable uiTable;

    public RapportPane(RapportModeleObligationReglementaire rapport) {
        ArgumentChecks.ensureNonNull("rapport", rapport);
        this.rapport = rapport;
        
        SIRS.loadFXML(this, RapportPane.class);
        Injector.injectDependencies(this);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        uiTable = new RapportSectionTable();
        uiTablePane.setCenter(uiTable);
    }

    public static RapportModeleObligationReglementaire showCreateDialog(){
        final RapportPane rpane = new RapportPane(new RapportModeleObligationReglementaire());

        final Dialog dialog = new Dialog();
        final DialogPane pane = new DialogPane();
        pane.setContent(rpane);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Modèle de document");

        final Optional opt = dialog.showAndWait();
        if(opt.isPresent() && ButtonType.APPLY.equals(opt.get())){
            return rpane.rapport;
        }
        return null;
    }

    public static RapportPane showEditDialog(RapportModeleObligationReglementaire rapport){
        final RapportPane rpane = new RapportPane(rapport);

        final Dialog dialog = new Dialog();
        final DialogPane pane = new DialogPane();
        pane.setContent(rpane);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Modèle de document");

        final Optional opt = dialog.showAndWait();
        if(opt.isPresent() && ButtonType.APPLY.equals(opt.get())){
            return rpane;
        }
        return null;
    }

}
