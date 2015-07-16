
package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 *
 * @author guilhem
 */
public class DatabaseVersionPane extends GridPane{

    @FXML
    private Label SFUuid;
    
    @FXML
    private Label SFProj;

    @FXML
    private Label SFVersion;
    
    @FXML
    private Label SFDistantUrl;

    @FXML
    private Label BDUuid;

    @FXML
    private Label BDProj;

    @FXML
    private Label BDVersion;
    
    @FXML
    private Label BDDistantUrl;

    
    public DatabaseVersionPane(final String existingKey, final String dbKey) {
        SIRS.loadFXML(this, ImportPane.class);
        Injector.injectDependencies(this);
        String[] sfKey = existingKey.split("\\|");
        
        SFUuid.setText(cleanNull(sfKey[0]));
        SFProj.setText(cleanNull(sfKey[1]));
        SFVersion.setText(cleanNull(sfKey[2]));
        SFDistantUrl.setText(cleanNull(sfKey[3]));
        
        String[] bdKey = dbKey.split("\\|");
        BDUuid.setText(cleanNull(bdKey[0]));
        BDProj.setText(cleanNull(bdKey[1]));
        BDVersion.setText(cleanNull(bdKey[2]));
        BDDistantUrl.setText(cleanNull(bdKey[3]));
    }
 
    private String cleanNull(final String value) {
        if (value.equals("null")) {
            return "non disponible";
        }
        return value;
    }
}
