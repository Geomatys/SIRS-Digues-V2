/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.theme.detail;

import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.Crete;
import fr.symadrem.sirs.core.model.Structure;
import java.time.LocalDateTime;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import org.geotoolkit.gui.javafx.util.FXDateField;

/**
 *
 * @author Samuel Andrés
 */
public class DetailTronconThemePane extends BorderPane {
    
    private Structure structure;
    
    @FXML private ScrollPane uiEditDetailTronconTheme;
      
    @FXML
    private Label mode;

    @FXML
    private ToggleButton uiEdit;

    @FXML
    private Button uiSave;

    @FXML
    private FXDateField date_maj;

    @FXML
    private ToggleButton uiConsult;

    @FXML
    private Label id;

    @FXML
    private BorderPane uiBorderPane;

    @FXML
    private Label mode1;


    @FXML
    void save(ActionEvent event) {
        structure.setDateMaj(LocalDateTime.now());
    }
    
    
    public DetailTronconThemePane(final Structure structure){
        Symadrem.loadFXML(this);
        this.structure = structure;
        initFields();
        if(structure instanceof Crete) {
            uiEditDetailTronconTheme.setContent(new DetailCretePane((Crete) structure));
        }
    }
    
    
    private void initFields(){
        id.setText(structure.getId());
        date_maj.valueProperty().bindBidirectional(structure.dateMajProperty());
    }
    
}
