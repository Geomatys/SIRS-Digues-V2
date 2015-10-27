
package fr.sirs.query;

import fr.sirs.SIRS;
import fr.sirs.core.model.SQLQuery;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

    private final ObjectProperty<SQLQuery> sqlQueryProperty = new SimpleObjectProperty<>();
    private final BooleanProperty modifiableProperty = new SimpleBooleanProperty(true);
    
    public FXQueryPane() {
        this(null);
    }
    
    public FXQueryPane(SQLQuery query) {
        SIRS.loadFXML(this);
        setSQLQuery(query);
        uiDesc.disableProperty().bind(modifiableProperty.not());
        uiSql.disableProperty().bind(modifiableProperty.not());
        uiLibelle.disableProperty().bind(modifiableProperty.not());
    }
    
    public SQLQuery getSQLQuery() {
        return sqlQueryProperty.get();
    }

    public final BooleanProperty modifiableProperty(){return modifiableProperty;}
    
    public void setSQLQuery(final SQLQuery newValue) {
        
        final SQLQuery oldValue = sqlQueryProperty.get();
        if (oldValue != null) {
            uiLibelle.textProperty().unbindBidirectional(oldValue.libelleProperty());
            uiDesc.textProperty().unbindBidirectional(oldValue.descriptionProperty());
            uiSql.textProperty().unbindBidirectional(oldValue.sqlProperty());
        }
        
        sqlQueryProperty.set(newValue);
        if (newValue != null) {
            uiLibelle.textProperty().bindBidirectional(newValue.libelleProperty());
            uiDesc.textProperty().bindBidirectional(newValue.descriptionProperty());
            uiSql.textProperty().bindBidirectional(newValue.sqlProperty());
        }
    }
}
