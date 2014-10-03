

package fr.sym.digue;

import fr.sym.Session;
import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.util.StringConverter;
import jidefx.scene.control.combobox.LocalDateComboBox;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class TronconDigueController {
    
    public Parent root;
    private TronconDigue troncon;
    
    @Autowired
    private Session session;
    
    @FXML
    private TextField section_name;
    
    @FXML
    private TextArea commentaireTronconTextField;
    
    @FXML
    private ChoiceBox<Digue> digues;
    
    @FXML
    private DatePicker date_debut;
    
    @FXML
    private DatePicker date_fin;
    
    @FXML
    private ToggleButton editionButton;
    
    @FXML
    private void enableFields(ActionEvent event){
        if (this.editionButton.isSelected()) {
            this.section_name.setEditable(false);
            this.commentaireTronconTextField.setEditable(false);
        } else {
            this.section_name.setEditable(true);
            this.commentaireTronconTextField.setEditable(true);
        }
    }
    
    public void init(TronconDigue troncon){
        this.troncon = troncon;
        
        this.section_name.setEditable(true);
        this.section_name.textProperty().bindBidirectional(this.troncon.libelleProperty());
        
        this.commentaireTronconTextField.setEditable(true);
        this.commentaireTronconTextField.setWrapText(true);
        this.commentaireTronconTextField.textProperty().bindBidirectional(this.troncon.commentaireProperty());
        
        //this.digues.
        
        final List<Digue> digs = session.getDigues();
        final ObservableList<Digue> diguesObservables = FXCollections.observableArrayList();
        digs.stream().forEach((dig) -> {
            diguesObservables.add(dig);
        });
        this.digues.setItems(diguesObservables);
        StringConverter<Digue> digueStringConverter = new StringConverter<Digue>() {
        @Override
        public String toString(Digue digue) {return digue.getLibelle();}

        // TODO
        @Override
        public Digue fromString(String string) {return null;}
        };
        this.digues.setConverter(digueStringConverter);
        //this.digues.setEditable(false);
        
        
        
        /////////////////////////////////////// A VOIR /////////////////////////
        this.date_debut.valueProperty();
        
        Instant instant = Instant.now();
        //instant.
        LocalDate date = this.date_debut.getValue();
        
        
        //LocalDate d = new LocalDate();
    }
    
    public static TronconDigueController create(TronconDigue troncon) {
        final FXMLLoader loader = new FXMLLoader(Symadrem.class.getResource(
                "/fr/sym/digue/tronconDigueDisplay.fxml"));
        final Parent root;
        try {
            root = loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        
        final TronconDigueController controller = loader.getController();
        Injector.injectDependencies(controller);
        controller.root = root;
        controller.init(troncon);
        return controller;
    }
    
}
