
package fr.sirs.theme.ui;

import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.Preview;
import static fr.sirs.plugin.vegetation.PluginVegetation.paramTraitement;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXPeuplementVegetationPane extends FXPeuplementVegetationPaneStub {
    
    public FXPeuplementVegetationPane(final PeuplementVegetation peuplementVegetation){
        super(peuplementVegetation);


        // Paramétrage du traitement lors du changement de type de peuplement
        ui_typePeuplementId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Preview>() {

            @Override
            public void changed(ObservableValue<? extends Preview> observable, Preview oldValue, Preview newValue) {
                final PeuplementVegetation peuplement = elementProperty().get();

                if(peuplement!=null && newValue!=null && newValue.getElementId()!=null){
                    paramTraitement(PeuplementVegetation.class, peuplement, newValue.getElementId());
                }
            }
        });
    }
}
