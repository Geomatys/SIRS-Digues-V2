package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.TraitementZoneVegetation;
import fr.sirs.core.model.ZoneVegetation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.stage.Modality;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ZoneVegetationPojoTable extends ListenPropertyPojoTable<String> {

    public ZoneVegetationPojoTable(String title) {
        super(ZoneVegetation.class, title);
        setDeletor(new Consumer<Element>() {

            @Override
            public void accept(Element pojo) {
                if(pojo instanceof ZoneVegetation) ((AbstractSIRSRepository) Injector.getSession().getRepositoryForClass(pojo.getClass())).remove(pojo);
            }
        });
    }
    
    @Override
    protected ZoneVegetation createPojo() {

        final ZoneVegetation zone;

        final ChoiceStage stage = new ChoiceStage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        final Class<? extends ZoneVegetation> retrievedClass = stage.getRetrievedElement().get();
        if(retrievedClass!=null){
            //Création de la zone
            final AbstractSIRSRepository zoneVegetationRepo = Injector.getSession().getRepositoryForClass(retrievedClass);
            zone = (ZoneVegetation) zoneVegetationRepo.create();
            zone.setForeignParentId(getPropertyReference());
            zoneVegetationRepo.add(zone);
            getAllValues().add(zone);

            //Création du traitement associé
            final TraitementZoneVegetation traitement = Injector.getSession().getElementCreator().createElement(TraitementZoneVegetation.class);

            ////////////////////////////////////////////////////////////////////
            // Remplissage par défaut du traitement en fonction des paramétrages du plan
            ////////////////////////////////////////////////////////////////////

//            // 1- Récupération de la parcelle :
//            final AbstractSIRSRepository<ParcelleVegetation> parcelleRepo = Injector.getSession().getRepositoryForClass(ParcelleVegetation.class);
//            if(parcelleRepo!=null){
//                final ParcelleVegetation parcelle = parcelleRepo.get(getPropertyReference());// L'identifiant de la parcelle est la référence qui est écoutée !!
//                if(parcelle!=null && parcelle.getPlanId()!=null){
//
//                    // 2- Récupération du plan
//                    final AbstractSIRSRepository<PlanVegetation> planRepo = Injector.getSession().getRepositoryForClass(PlanVegetation.class);
//                    if(planRepo!=null){
//                        final PlanVegetation plan = planRepo.get(parcelle.getPlanId());
//
//                        if(plan!=null){
//
//                            // 3- Récupération des paramétrages de fréquences
//                            final ObservableList<ParamFrequenceTraitementVegetation> params = plan.getParamFrequence();
//                            boolean poncuelSet=false, nonPonctuelSet=false;
//                            for(final ParamFrequenceTraitementVegetation param : params){
//
//                                // On ne s'intéresse qu'aux paramètres relatifs au type de zone concerné.
//                                if(param.getType().equals(retrievedClass)){
//
//                                }
//                            }
//                        }
//                    }
//                }
//            }

            zone.setTraitement(traitement);
        }
        else {
            zone = null;
        }
        return zone;
    }

    private static class ChoiceStage extends PojoTableComboBoxChoiceStage<Class<? extends ZoneVegetation>, Class<? extends ZoneVegetation>> {

        private ChoiceStage(){
            super();
            setTitle("Choix du type de zone");

            final List<Class<? extends Element>> classes = Session.getElements();

            final List<Class<? extends ZoneVegetation>> zoneTypes = new ArrayList<>();
            for(final Class element : classes){
                if(ZoneVegetation.class.isAssignableFrom(element) && !Modifier.isAbstract(element.getModifiers())){
                    zoneTypes.add(element);
                }
            }

            comboBox.setItems(FXCollections.observableList(zoneTypes));

            retrievedElement.bind(comboBox.getSelectionModel().selectedItemProperty());
        }
    }
}
