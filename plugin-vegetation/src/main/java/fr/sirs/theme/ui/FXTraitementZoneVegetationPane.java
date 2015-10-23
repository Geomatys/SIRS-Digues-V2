
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefSousTraitementVegetation;
import fr.sirs.core.model.RefTraitementVegetation;
import fr.sirs.core.model.TraitementZoneVegetation;
import static fr.sirs.plugin.vegetation.PluginVegetation.initComboSousTraitement;
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

    boolean frequencyChanged = false;
    String firstFrequencyId = "";
    
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
                        sousTypesDispos.removeIf((RefSousTraitementVegetation st) -> !traitementId.equals(st.getTypeTraitementId()));
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
                        sousTypesDispos.removeIf((RefSousTraitementVegetation st) -> !traitementId.equals(st.getTypeTraitementId()));
                        SIRS.initCombo(ui_sousTypeTraitementPonctuelId, FXCollections.observableList(sousTypesDispos), null);
                    }
                }
            }
        });

        ui_frequenceId.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
            if(!frequencyChanged && oldValue instanceof Preview){
                firstFrequencyId = ((Preview) oldValue).getElementId();
                frequencyChanged=true;
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

        initComboSousTraitement(newElement.getTypeTraitementPonctuelId(), newElement.getSousTypeTraitementPonctuelId(), sousTraitementPreviews, sousTraitements, ui_sousTypeTraitementPonctuelId);

        initComboSousTraitement(newElement.getTypeTraitementId(), newElement.getSousTypeTraitementId(), sousTraitementPreviews, sousTraitements, ui_sousTypeTraitementId);
    }

    /*
    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    IMPORTANT
    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    La modification d'une donnée dans un traitemetn de zone de végétation est
    susceptible d'avoir un effet sur la planification d'une parcelle en mode
    auto.

    MAIS !

    Il est à noter que la planification, tout en étant en mode auto, peut avoir
    été personnalisée !

    exemple : si la planification est de 5 ans en 5 ans à partir de 2015
    (prochain traitement auto-calculé en 2020), on peut avoir forcé la planif à
    partir de 2016 (ce qui implique un prochain traitement auto-calculé en 2021)

    Ainsi, même en mode automatique, on ne peut pas être au courant des souhaits
    de l'utilisateur à vouloir ou pas garder sa planification courante.

    IL FAUT DONC LAISSER L'UTILISATEUR GÉRER SA PLANIFICATION COMME IL L'ENTEND.

    LES MODIFICATIONS RELATIVES AU TRAITEMENT DE LA ZONE DE VÉGÉTATION QUI
    AURAIENT ÉTÉ APPORTÉES VIA CE PANNEAU SERONT PRISES EN COMPTE LORS DES
    MODIFICATIONS POSTÉRIEURES DE LA PLANIFICATION DES PARCELLES.
    
     */

//    @Override
//    public void preSave(){
//        super.preSave();
//
//        final TraitementZoneVegetation traitement = elementProperty.get();
//        if(traitement!=null){
//            final Element parent = traitement.getParent();
//            if(parent instanceof ZoneVegetation && !(parent instanceof InvasiveVegetation)){
//                final Alert alert = new Alert(Alert.AlertType.INFORMATION,
//                        "La planification d'une parcelle doit être mise à jour.",
//                        ButtonType.OK);
//                alert.setResizable(true);
//                final Optional<ButtonType> result = alert.showAndWait();
//                if(result.isPresent() && result.get()==ButtonType.OK){
//                    updateParcelleAutoPlanif((ZoneVegetation) parent);
//                }
//            }
//        }
//
//    }


//    @Override
//    public void preSave(){
//        super.preSave();
//
//        if(ui_frequenceId.getSelectionModel().getSelectedItem() instanceof Preview){
//            final String choosenFrequencyId = ((Preview) ui_frequenceId.getSelectionModel().getSelectedItem()).getElementId();
//            if(frequencyChanged && !Objects.equals(firstFrequencyId, choosenFrequencyId)){
//                final TraitementZoneVegetation traitement = elementProperty.get();
//                if(traitement!=null){
//                    final Element parent = traitement.getParent();
//                    if(parent instanceof ZoneVegetation && !(parent instanceof InvasiveVegetation)){
//                        final Alert alert = new Alert(Alert.AlertType.INFORMATION,
//                                "Il semblerait que la fréquence ait été modifiée.\n"
//                                        + "Voulez-vous mettre à jour la planification de la parcelle à partir de l'année courante ?",
//                                ButtonType.YES, ButtonType.NO);
//                        alert.setResizable(true);
//                        final Optional<ButtonType> result = alert.showAndWait();
//                        if(result.isPresent() && result.get()==ButtonType.YES){
//                            updateParcelleAutoPlanif((ZoneVegetation) parent);
//                        }
//                    }
//                }
//            }
//        }
//    }
}
