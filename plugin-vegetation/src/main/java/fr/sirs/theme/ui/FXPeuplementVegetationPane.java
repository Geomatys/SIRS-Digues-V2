
package fr.sirs.theme.ui;

import fr.sirs.core.model.Element;
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
        ui_typeVegetationId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                final PeuplementVegetation peuplement = elementProperty().get();

                if (peuplement != null && newValue != null) {
                    final String typeId = (newValue instanceof Element) ? ((Element) newValue).getId() : (newValue instanceof Preview) ? ((Preview) newValue).getElementId() : null;
                    if (typeId != null) {
                        paramTraitement(PeuplementVegetation.class, peuplement, typeId);
                    }
                }
            }
        });
    }
}
