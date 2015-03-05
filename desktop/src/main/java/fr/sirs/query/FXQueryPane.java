
package fr.sirs.query;

import fr.sirs.core.model.SQLQuery;
import fr.sirs.SIRS;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Johann Sorel
 */
public class FXQueryPane extends GridPane {

    @FXML private TextArea uiDesc;
    @FXML private TextArea uiSql;
    @FXML private TextField uiLibelle;

    public FXQueryPane(SQLQuery query) {
        SIRS.loadFXML(this);
        uiLibelle.textProperty().bindBidirectional(query.name);
        uiDesc.textProperty().bindBidirectional(query.description);
        uiSql.textProperty().bindBidirectional(query.sql);
    }
    
}
