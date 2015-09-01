
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import java.time.LocalDate;
import java.util.logging.Level;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXParcelleVegetationPane extends FXParcelleVegetationPaneStub {

    @FXML protected DatePicker ui_dernierTraitement;
    @FXML protected Spinner<Integer> ui_frequenceTraitement;
    @FXML protected Spinner<Integer> ui_anneePlanifiee;
    @FXML protected Label ui_anneePlanifieeInfo;

    protected FXParcelleVegetationPane(){
        super();

        ui_dernierTraitement.setDisable(true);

        ui_frequenceTraitement.setDisable(true);
        ui_frequenceTraitement.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));

        ui_anneePlanifiee.setDisable(true);
        ui_anneePlanifiee.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));
        
        ui_planId_link.setVisible(false);
    }

    public FXParcelleVegetationPane(final ParcelleVegetation parcelleVegetation){
        this();
        this.elementProperty().set(parcelleVegetation);
    }
    
    @Override
    protected void initFields(ObservableValue<? extends ParcelleVegetation > observableElement, ParcelleVegetation oldElement, ParcelleVegetation newElement) {
        super.initFields(observableElement, oldElement, newElement);

        // Calcul de la fréquence de traitement
        // Calcul de la dernière année de traitement
        // Calcul de l'année planifiée

        ui_dernierTraitement.setValue(PluginVegetation.dernierTraitement(newElement));
        ui_frequenceTraitement.getValueFactory().setValue(PluginVegetation.frequenceTraitementPlanifie(newElement));

        if(newElement.getPlanId()!=null){
            try{
                final PlanVegetation plan = Injector.getSession().getRepositoryForClass(PlanVegetation.class).get(newElement.getPlanId());

                // S'il n'y a pas de plan, on lance une exception car la parcelle devrait être associée à un plan.
                if(plan!=null){
                    final int debutPlan = plan.getAnneeDebut();
                    final int finPlan = plan.getAnneeFin();
                    final int anneeEnCours = LocalDate.now().getYear();
                    final int dureePlan = finPlan-debutPlan;

                    // Pour signaler une année de prochain traitement, il faut
                    // que l'on soit en cours de plan, sinon cette information
                    // n'a pas de sens.
                    if(anneeEnCours>=debutPlan && anneeEnCours<=finPlan){
                        // On vérifie alors l'état des planifications de la parcelle
                        final ObservableList<Boolean> planifications = newElement.getPlanifications();
                        if(planifications!=null){

                            final int index = anneeEnCours-debutPlan;
                            Integer anneePlanif = 0;
                            ui_anneePlanifieeInfo.setText("(pas de traitement planifié)");
                            for(int i=index; i<dureePlan; i++){
                                if(planifications.get(i)) {
                                    anneePlanif = debutPlan+i;
                                    ui_anneePlanifieeInfo.setText("");
                                    break;
                                }
                            }
                            ui_anneePlanifiee.getValueFactory().setValue(anneePlanif);
                        }
                        else{
                            throw new IllegalStateException("Impossible de retrouver les planifications de la parcelle");
                        }
                    }
                    else if (finPlan<debutPlan) {
                        ui_anneePlanifieeInfo.setText("Calcul impossible : plan ("+debutPlan+"-"+finPlan+") incohérent");
                        throw new IllegalStateException("La date de fin du plan est antérieure à la date de début du plan");
                    }
                    else{
                        // Sinon, c'est que l'année courante n'est pas dans le 
                        // plan et il faut signaler ici que le plan n'est pas en
                        // cours et qu'il est normal que la date de prochain
                        // traitement soit nulle.
                        ui_anneePlanifieeInfo.setText("Date courante hors plan ("+debutPlan+"-"+finPlan+")");
                    }

                } else {
                    ui_anneePlanifieeInfo.setText("Pas de plan identifié");
                    throw new IllegalStateException("Impossible de retrouver le plan (id="+newElement.getPlanId()+") de la parcelle "+newElement);
                }
            }catch(Exception e){
                SIRS.LOGGER.log(Level.WARNING, "Impossible de calculer la date du prochain traitement", e);
                // Il faut signaler ici une erreur avec une petite icône d'erreur
            }
        }
    }
}
