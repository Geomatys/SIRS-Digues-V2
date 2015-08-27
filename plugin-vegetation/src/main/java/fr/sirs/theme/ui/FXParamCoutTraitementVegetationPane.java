
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.ParamCoutTraitementVegetation;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefSousTraitementVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXParamCoutTraitementVegetationPane extends FXParamCoutTraitementVegetationPaneStub {

    public FXParamCoutTraitementVegetationPane(final ParamCoutTraitementVegetation paramCoutTraitementVegetation){
        super(paramCoutTraitementVegetation);

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
    protected void initFields(ObservableValue<? extends ParamCoutTraitementVegetation > observableElement, ParamCoutTraitementVegetation oldElement, ParamCoutTraitementVegetation newElement) {
        super.initFields(observableElement, oldElement, newElement);

        // Initialisation des sous-types
        final AbstractSIRSRepository<RefSousTraitementVegetation> repoSousTraitements = Injector.getSession().getRepositoryForClass(RefSousTraitementVegetation.class);
        final Map<String, RefSousTraitementVegetation> sousTraitements = new HashMap<>();

        for(final RefSousTraitementVegetation sousTraitement : repoSousTraitements.getAll()){
            sousTraitements.put(sousTraitement.getId(), sousTraitement);
        }
        final List<Preview> sousTraitementPreviews = previewRepository.getByClass(RefSousTraitementVegetation.class);

        PluginVegetation.initComboSousTraitement(newElement.getTraitementId(), newElement.getSousTraitementId(), sousTraitementPreviews, sousTraitements, ui_sousTraitementId);
    }

    /**
     * 
     */
    @Override
    public void preSave(){
        super.preSave();
    }
}
