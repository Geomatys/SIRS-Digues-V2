
package fr.sym.theme.detail;

import fr.symadrem.sirs.core.model.BorneDigue;
import fr.symadrem.sirs.core.model.Positionable;
import java.io.IOException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    @FXML private ComboBox<BorneDigue> uiBorneFin;
    @FXML private ComboBox<BorneDigue> uiBorneDebut;
    @FXML private FXNumberSpinner uiDistanceDebut;
    
    private final ObjectProperty<Positionable> positionableProperty = new SimpleObjectProperty<>();
            
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
        
        positionableProperty.addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                updateField();
            }
        });
        
    }
    
    public ObjectProperty<Positionable> positionableProperty(){
        return positionableProperty;
    }
    
    private void updateField(){
        final Positionable pos = (Positionable) positionableProperty.get();
        if(pos==null) return;
        
        //maj des liste de borne
        
        
    }
    
    
}
