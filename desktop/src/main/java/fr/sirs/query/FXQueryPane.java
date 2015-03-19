
package fr.sirs.query;

import fr.sirs.core.model.SQLQuery;
import fr.sirs.SIRS;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXQueryPane extends GridPane {

    @FXML private TextArea uiDesc;
    @FXML private TextArea uiSql;
    @FXML private TextField uiLibelle;

    private final SimpleObjectProperty<SQLQuery> sqlQueryProperty = new SimpleObjectProperty<>();
    
    public FXQueryPane() {
        this(null);
    }
    
    public FXQueryPane(SQLQuery query) {
        SIRS.loadFXML(this);
        setSQLQuery(query);
    }
    
    public SQLQuery getSQLQuery() {
        return sqlQueryProperty.get();
    }
    
    public void setSQLQuery(final SQLQuery newValue) {
        
        final SQLQuery oldValue = sqlQueryProperty.get();
        if (oldValue != null) {
            uiLibelle.textProperty().unbindBidirectional(oldValue.name);
            uiDesc.textProperty().unbindBidirectional(oldValue.description);
            uiSql.textProperty().unbindBidirectional(oldValue.sql);
        }
        
        sqlQueryProperty.set(newValue);
        if (newValue != null) {
            uiLibelle.textProperty().bindBidirectional(newValue.name);
            uiDesc.textProperty().bindBidirectional(newValue.description);
            uiSql.textProperty().bindBidirectional(newValue.sql);
        }
    }
}
