
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.ParamFrequenceTraitementVegetation;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefSousTraitementVegetation;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXParamFrequenceTraitementVegetationPane extends FXParamFrequenceTraitementVegetationPaneStub {

    @FXML ComboBox<Class<? extends ZoneVegetation>> ui_type;
    
    public FXParamFrequenceTraitementVegetationPane(final ParamFrequenceTraitementVegetation paramFrequenceTraitementVegetation){
        super(paramFrequenceTraitementVegetation);

        ui_type.disableProperty().bind(disableFieldsProperty());

        ui_traitementId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(newValue instanceof Preview){
                    final AbstractSIRSRepository<RefSousTraitementVegetation> sousTypeRepo = Injector.getSession().getRepositoryForClass(RefSousTraitementVegetation.class);
                    final String traitementId = ((Preview) newValue).getElementId();
                    if(traitementId!=null){
                        final List<RefSousTraitementVegetation> sousTypesDispos = sousTypeRepo.getAll();
                        sousTypesDispos.removeIf((RefSousTraitementVegetation st) -> !traitementId.equals(st.getTraitementId()));
                        SIRS.initCombo(ui_sousTraitementId, FXCollections.observableList(sousTypesDispos), null);
                    }
                }
            }
        });
    }     

    /**
     * Initialize fields at element setting.
     * @param observableElement
     * @param oldElement
     * @param newElement
     */
    @Override
    protected void initFields(ObservableValue<? extends ParamFrequenceTraitementVegetation > observableElement, ParamFrequenceTraitementVegetation oldElement, ParamFrequenceTraitementVegetation newElement) {

        super.initFields(observableElement, oldElement, newElement);

        SIRS.initCombo(ui_type, FXCollections.observableList(PluginVegetation.zoneVegetationClasses()), newElement.getType() == null? null : newElement.getType());

        // Initialisation des sous-types
        final AbstractSIRSRepository<RefSousTraitementVegetation> repoSousTraitements = Injector.getSession().getRepositoryForClass(RefSousTraitementVegetation.class);
        final Map<String, RefSousTraitementVegetation> sousTraitements = new HashMap<>();

        for(final RefSousTraitementVegetation sousTraitement : repoSousTraitements.getAll()){
            sousTraitements.put(sousTraitement.getId(), sousTraitement);
        }
        final List<Preview> sousTraitementPreviews = previewRepository.getByClass(RefSousTraitementVegetation.class);

        PluginVegetation.initComboSousTraitement(newElement.getTraitementId(), newElement.getSousTraitementId(), sousTraitementPreviews, sousTraitements, ui_sousTraitementId);
    }


    @Override
    public void preSave() {
        super.preSave();

        final ParamFrequenceTraitementVegetation element = (ParamFrequenceTraitementVegetation) elementProperty().get();
        element.setType(ui_type.getSelectionModel().getSelectedItem());
    }
}
