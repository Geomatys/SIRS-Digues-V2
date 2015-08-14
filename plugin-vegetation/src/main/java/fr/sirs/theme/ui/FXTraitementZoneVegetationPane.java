
package fr.sirs.theme.ui;

import fr.sirs.core.model.TraitementZoneVegetation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXTraitementZoneVegetationPane extends FXTraitementZoneVegetationPaneStub {
    
    public FXTraitementZoneVegetationPane(final TraitementZoneVegetation traitementZoneVegetation){
        super(traitementZoneVegetation);
    }
    
    /**
     * Initialize fields at element setting.
     */
    protected void initFields(ObservableValue<? extends TraitementZoneVegetation > observableElement, TraitementZoneVegetation oldElement, TraitementZoneVegetation newElement) {
        super.initFields(observableElement, oldElement, newElement);

        ui_typeTraitementPonctuelId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                final Alert alert = new Alert(Alert.AlertType.NONE, "coucou", ButtonType.OK);
                alert.showAndWait();
            }
        });
        /*
//         * Bind control properties to Element ones.
//         */
//        // Propriétés de TraitementZoneVegetation
//        SIRS.initCombo(ui_typeTraitementPonctuelId, FXCollections.observableArrayList(
//            previewRepository.getByClass(RefTraitementVegetation.class)),
//            newElement.getTypeTraitementPonctuelId() == null? null : previewRepository.get(newElement.getTypeTraitementPonctuelId()));
//        SIRS.initCombo(ui_sousTypeTraitementPonctuelId, FXCollections.observableArrayList(
//            previewRepository.getByClass(RefSousTraitementVegetation.class)),
//            newElement.getSousTypeTraitementPonctuelId() == null? null : previewRepository.get(newElement.getSousTypeTraitementPonctuelId()));
//        SIRS.initCombo(ui_typeTraitementId, FXCollections.observableArrayList(
//            previewRepository.getByClass(RefTraitementVegetation.class)),
//            newElement.getTypeTraitementId() == null? null : previewRepository.get(newElement.getTypeTraitementId()));
//        SIRS.initCombo(ui_sousTypeTraitementId, FXCollections.observableArrayList(
//            previewRepository.getByClass(RefSousTraitementVegetation.class)),
//            newElement.getSousTypeTraitementId() == null? null : previewRepository.get(newElement.getSousTypeTraitementId()));
//        SIRS.initCombo(ui_frequenceId, FXCollections.observableArrayList(
//            previewRepository.getByClass(RefFrequenceTraitementVegetation.class)),
//            newElement.getFrequenceId() == null? null : previewRepository.get(newElement.getFrequenceId()));
    }
}
