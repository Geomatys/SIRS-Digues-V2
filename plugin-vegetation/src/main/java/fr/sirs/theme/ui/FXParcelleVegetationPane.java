
package fr.sirs.theme.ui;

import fr.sirs.core.model.ParcelleVegetation;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXParcelleVegetationPane extends FXParcelleVegetationPaneStub {

    protected FXParcelleVegetationPane(){
        super();

        ui_planId_link.setVisible(false);

        ui_traitements.getTabPane().getTabs().remove(ui_traitements);
    }

    public FXParcelleVegetationPane(final ParcelleVegetation parcelleVegetation){
        this();
        this.elementProperty().set(parcelleVegetation);
    }
}
