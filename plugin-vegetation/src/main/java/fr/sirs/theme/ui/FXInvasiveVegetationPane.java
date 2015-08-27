
package fr.sirs.theme.ui;

import fr.sirs.core.model.InvasiveVegetation;
import fr.sirs.core.model.Preview;
import static fr.sirs.plugin.vegetation.PluginVegetation.paramTraitement;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXInvasiveVegetationPane extends FXInvasiveVegetationPaneStub {

    public FXInvasiveVegetationPane(final InvasiveVegetation invasiveVegetation){
        super(invasiveVegetation);

        // Paramétrage du traitement lors du changement de type de peuplement
        ui_typeInvasive.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Preview>() {

            @Override
            public void changed(ObservableValue<? extends Preview> observable, Preview oldValue, Preview newValue) {
                final InvasiveVegetation invasive = elementProperty().get();
                
                if(invasive!=null && newValue!=null && newValue.getElementId()!=null){
                    paramTraitement(InvasiveVegetation.class, invasive, newValue.getElementId());
                }
            }
        });
    }
}
