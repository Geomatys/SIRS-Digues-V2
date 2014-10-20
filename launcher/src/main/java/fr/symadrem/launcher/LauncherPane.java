
package fr.symadrem.launcher;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class LauncherPane extends BorderPane {

    @FXML
    private TextField uiDistantLogin;
    @FXML
    private TextField uiNewName;
    @FXML
    private PasswordField uiDistantPassword;
    @FXML
    private TableView<?> uiLocalBaseTable;
    @FXML
    private TextField uiDistantUrl;
    @FXML
    private CheckBox uiDistantSync;
    @FXML
    private TextField uiDistantOauth;
    
    public LauncherPane() {
        final Class cdtClass = getClass();
        final String fxmlpath = "/"+cdtClass.getName().replace('.', '/')+".fxml";
        final FXMLLoader loader = new FXMLLoader(cdtClass.getResource(fxmlpath));
        loader.setController(this);
        loader.setRoot(this);
        //in special environement like osgi or other, we must use the proper class loaders
        //not necessarly the one who loaded the FXMLLoader class
        loader.setClassLoader(cdtClass.getClassLoader());
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
    
    
    @FXML
    void connectLocal(ActionEvent event) {

    }

    @FXML
    void connectDistant(ActionEvent event) {

    }

    @FXML
    void createEmpty(ActionEvent event) {

    }

    @FXML
    void createFromAccess(ActionEvent event) {

    }

    @FXML
    void updateApp(ActionEvent event) {

    }
    
}
