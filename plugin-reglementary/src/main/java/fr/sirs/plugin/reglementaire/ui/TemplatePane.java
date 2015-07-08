
package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.TemplateObligationReglementaire;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.apache.sis.util.ArgumentChecks;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class TemplatePane extends GridPane implements Initializable {

    @FXML private TextField uiTitre;
    @FXML private TextField uiFile;

    private final TemplateObligationReglementaire template;

    public TemplatePane(TemplateObligationReglementaire rapport) {
        ArgumentChecks.ensureNonNull("rapport", rapport);
        this.template = rapport;
        
        SIRS.loadFXML(this, TemplatePane.class);
        Injector.injectDependencies(this);

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        uiTitre.textProperty().bindBidirectional(template.libelleProperty());
        uiFile.textProperty().bindBidirectional(template.fichierProperty());
    }

    public TemplateObligationReglementaire getTemplate(){
        return template;
    }

    @FXML
    void uploadFile(ActionEvent event) {
        final FileChooser chooser = new FileChooser();
        final FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Open Document Format", "*.odt");
        chooser.getExtensionFilters().add(filter);
        chooser.setSelectedExtensionFilter(filter);

        final File file = chooser.showOpenDialog(null);
        if(file!=null){
            template.setFichier(file.getPath());
            try {
                template.setOdt(readAll(file));
            } catch (IOException ex) {
                SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
            }
        }
    }

    public static TemplateObligationReglementaire showCreateDialog(){
        final TemplatePane rpane = new TemplatePane(new TemplateObligationReglementaire());
        rpane.template.setValid(true);

        final Dialog dialog = new Dialog();
        final DialogPane pane = new DialogPane();
        pane.setContent(rpane);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Modèle de document");

        final Optional opt = dialog.showAndWait();
        if(opt.isPresent() && ButtonType.APPLY.equals(opt.get())){
            return rpane.getTemplate();
        }
        return null;
    }

    public static TemplateObligationReglementaire showEditDialog(TemplateObligationReglementaire rapport){
        final TemplatePane rpane = new TemplatePane(rapport);
        rpane.template.setValid(true);

        final Dialog dialog = new Dialog();
        final DialogPane pane = new DialogPane();
        pane.setContent(rpane);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Modèle de document");

        final Optional opt = dialog.showAndWait();
        if(opt.isPresent() && ButtonType.APPLY.equals(opt.get())){
            return rpane.getTemplate();
        }
        return null;
    }

    private static byte[] readAll(File file) throws FileNotFoundException, IOException{
        final byte[] array;
        try (FileInputStream in = new FileInputStream(file)) {
            array = new byte[(int)file.length()];
            new DataInputStream(in).readFully(array);
        }
        return array;
    }

}