
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefSousTraitementVegetation;
import fr.sirs.core.model.RefTraitementVegetation;
import fr.sirs.core.model.TraitementZoneVegetation;
import static fr.sirs.plugin.vegetation.PluginVegetation.initSubType;
import java.util.ArrayList;
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
public class FXTraitementZoneVegetationPane extends FXTraitementZoneVegetationPaneStub {
    
    public FXTraitementZoneVegetationPane(final TraitementZoneVegetation traitementZoneVegetation){
        super(traitementZoneVegetation);

        ui_typeTraitementId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(newValue instanceof Preview){
                    final AbstractSIRSRepository<RefSousTraitementVegetation> sousTypeRepo = Injector.getSession().getRepositoryForClass(RefSousTraitementVegetation.class);
                    final String traitementId = ((Preview) newValue).getElementId();
                    if(traitementId!=null){
                        final List<RefSousTraitementVegetation> sousTypesDispos = sousTypeRepo.getAll();
                        sousTypesDispos.removeIf((RefSousTraitementVegetation st) -> !traitementId.equals(st.getTraitementId()));
                        SIRS.initCombo(ui_sousTypeTraitementId, FXCollections.observableList(sousTypesDispos), null);
                    }
                }
            }
        });

        ui_typeTraitementPonctuelId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(newValue instanceof Preview){
                    final AbstractSIRSRepository<RefSousTraitementVegetation> sousTypeRepo = Injector.getSession().getRepositoryForClass(RefSousTraitementVegetation.class);
                    final String traitementId = ((Preview) newValue).getElementId();
                    if(traitementId!=null){
                        final List<RefSousTraitementVegetation> sousTypesDispos = sousTypeRepo.getAll();
                        sousTypesDispos.removeIf((RefSousTraitementVegetation st) -> !traitementId.equals(st.getTraitementId()));
                        SIRS.initCombo(ui_sousTypeTraitementPonctuelId, FXCollections.observableList(sousTypesDispos), null);
                    }
                }
            }
        });
    }


    @Override
    protected void initFields(ObservableValue<? extends TraitementZoneVegetation > observableElement, TraitementZoneVegetation oldElement, TraitementZoneVegetation newElement) {
        super.initFields(observableElement, oldElement, newElement);

        // Initialisation des types
        final AbstractSIRSRepository<RefTraitementVegetation> repoTraitements = Injector.getSession().getRepositoryForClass(RefTraitementVegetation.class);
        final Map<String, RefTraitementVegetation> traitements = new HashMap<>();

        for(final RefTraitementVegetation traitement : repoTraitements.getAll()){
            traitements.put(traitement.getId(), traitement);
        }

        final List<Preview> traitementPreviews = previewRepository.getByClass(RefTraitementVegetation.class);
        final List<Preview> traitementsPonctuels = new ArrayList<>();
        final List<Preview> traitementsNonPonctuels = new ArrayList<>();

        for(final Preview preview : traitementPreviews){
            final String traitementId = preview.getElementId();
            if(traitementId!=null){
                final RefTraitementVegetation traitement = traitements.get(traitementId);
                if(traitement!=null){
                    if(traitement.getPonctuel()) traitementsPonctuels.add(preview);
                    else traitementsNonPonctuels.add(preview);
                }
            }
        }

        SIRS.initCombo(ui_typeTraitementPonctuelId, FXCollections.observableList(traitementsPonctuels),
            newElement.getTypeTraitementPonctuelId() == null? null : previewRepository.get(newElement.getTypeTraitementPonctuelId()));
        SIRS.initCombo(ui_typeTraitementId, FXCollections.observableList(traitementsNonPonctuels),
            newElement.getTypeTraitementId() == null? null : previewRepository.get(newElement.getTypeTraitementId()));



        // Initialisation des sous-types
        final AbstractSIRSRepository<RefSousTraitementVegetation> repoSousTraitements = Injector.getSession().getRepositoryForClass(RefSousTraitementVegetation.class);
        final Map<String, RefSousTraitementVegetation> sousTraitements = new HashMap<>();

        for(final RefSousTraitementVegetation sousTraitement : repoSousTraitements.getAll()){
            sousTraitements.put(sousTraitement.getId(), sousTraitement);
        }

        final List<Preview> sousTraitementPreviews = previewRepository.getByClass(RefSousTraitementVegetation.class);

        initSubType(newElement.getTypeTraitementPonctuelId(), newElement.getSousTypeTraitementPonctuelId(), sousTraitementPreviews, sousTraitements, ui_sousTypeTraitementPonctuelId);

        initSubType(newElement.getTypeTraitementId(), newElement.getSousTypeTraitementId(), sousTraitementPreviews, sousTraitements, ui_sousTypeTraitementId);
    }
}
