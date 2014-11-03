
package fr.sym.theme.detail;

import java.io.IOException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.gui.javafx.util.FXNumberSpinner;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DetailPositionnablePane extends BorderPane {
    
    @FXML private FXNumberSpinner uiPRDebut;
    @FXML private FXNumberSpinner uiDistanceFin;
    @FXML private FXNumberSpinner uiPRFin;
    @FXML private ComboBox<?> uiBorneFin;
    @FXML private ComboBox<?> uiBorneDebut;
    @FXML private FXNumberSpinner uiDistanceDebut;
    
    private final ObjectProperty positionableProperty = new SimpleObjectProperty();
            
    public DetailPositionnablePane(){
        try{
            final Class cdtClass = getClass();
            final String fxmlpath = "/fr/sym/theme/detail/DetailPositionnablePane.fxml";
            final FXMLLoader loader = new FXMLLoader(cdtClass.getResource(fxmlpath));
            loader.setController(this);
            loader.setRoot(this);
            loader.setClassLoader(cdtClass.getClassLoader());
            try {
                loader.load();
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex.getMessage(), ex);
            }
        }catch(Throwable ex){
            ex.printStackTrace();
        }
    }
    
    public ObjectProperty positionableProperty(){
        return positionableProperty;
    }
    
    
}
