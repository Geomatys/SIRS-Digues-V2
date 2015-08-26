
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.ParamFrequenceTraitementVegetation;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.Preview;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

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
                    final String typeVegetationId = newValue.getElementId();

                    // 1- Récupération de la parcelle :
                    final AbstractSIRSRepository<ParcelleVegetation> parcelleRepo = Injector.getSession().getRepositoryForClass(ParcelleVegetation.class);
                    if(parcelleRepo!=null){
                        final ParcelleVegetation parcelle = parcelleRepo.get(peuplement.getParcelleId());
                        if(parcelle!=null && parcelle.getPlanId()!=null){

                            // 2- Récupération du plan
                            final AbstractSIRSRepository<PlanVegetation> planRepo = Injector.getSession().getRepositoryForClass(PlanVegetation.class);
                            if(planRepo!=null){
                                final PlanVegetation plan = planRepo.get(parcelle.getPlanId());

                                if(plan!=null){

                                    // 3- Récupération des paramétrages de fréquences
                                    final ObservableList<ParamFrequenceTraitementVegetation> params = plan.getParamFrequence();
                                    boolean ponctuelSet=false, nonPonctuelSet=false;
                                    for(final ParamFrequenceTraitementVegetation param : params){

                                        // On ne s'intéresse qu'aux paramètres relatifs au type de zone concerné.
                                        if(param.getType().equals(PeuplementVegetation.class) && typeVegetationId.equals(param.getTypeVegetationId())){

                                            if(param.getPonctuel()){
                                                peuplement.getTraitement().setTraitementPonctuelId(param.getTraitementId());
                                                peuplement.getTraitement().setSousTraitementPonctuelId(param.getSousTraitementId());
                                                ponctuelSet=true;
                                            }
                                            else{
                                                peuplement.getTraitement().setTraitementId(param.getTraitementId());
                                                peuplement.getTraitement().setSousTraitementId(param.getSousTraitementId());
                                                peuplement.getTraitement().setFrequenceId(param.getFrequenceId());
                                                nonPonctuelSet=true;
                                            }
                                        }

                                        // Lorsque les deux traitements ponctuel et non ponctuel ont été initialisés, on peut arrêter l'examen des paramètres.
                                        if(ponctuelSet && nonPonctuelSet) break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }     
}
