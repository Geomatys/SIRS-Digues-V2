package fr.sirs;

import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.RefTypeDesordre;
import fr.sirs.core.model.RefUrgence;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDisorderPrintPane extends TemporalTronconChoicePrintPane {

    @FXML private Tab uiDisorderTypeChoice;
    @FXML private Tab uiUrgenceTypeChoice;

    @FXML private CheckBox uiOptionPhoto;
    @FXML private CheckBox uiOptionReseauOuvrage;
    @FXML private CheckBox uiOptionVoirie;

    private final TypeChoicePojoTable disordreTypesTable = new TypeChoicePojoTable(RefTypeDesordre.class, "Types de désordres");
    private final TypeChoicePojoTable urgenceTypesTable = new TypeChoicePojoTable(RefUrgence.class, "Types d'urgences");

    public FXDisorderPrintPane(){
        super(FXDisorderPrintPane.class);
        disordreTypesTable.setTableItems(()-> (ObservableList) SIRS.observableList(Injector.getSession().getRepositoryForClass(RefTypeDesordre.class).getAll()));
        disordreTypesTable.commentAndPhotoProperty().set(false);
        uiDisorderTypeChoice.setContent(disordreTypesTable);
        urgenceTypesTable.setTableItems(()-> (ObservableList) SIRS.observableList(Injector.getSession().getRepositoryForClass(RefUrgence.class).getAll()));
        urgenceTypesTable.commentAndPhotoProperty().set(false);
        uiUrgenceTypeChoice.setContent(urgenceTypesTable);
    }

    @FXML private void cancel(){

    }


    @FXML
    private void print(){
        Injector.getSession().getTaskManager().submit("Génération de fiches détaillées de désordres",
        () -> {

            final List<Desordre> desordres = Injector.getSession().getRepositoryForClass(Desordre.class).getAll();

            // On retire les désordres de la liste dans les cas suivants...
            desordres.removeIf(new LocalPredicate().or(
                    new LocationPredicate<>().or(
                            (Predicate) new TemporalPredicate())));

            try {
                if(!desordres.isEmpty()){
                    Injector.getSession().getPrintManager().printDesordres(desordres, uiOptionPhoto.isSelected(), uiOptionReseauOuvrage.isSelected(), uiOptionVoirie.isSelected());
                }
            } catch (Exception ex) {
                SIRS.LOGGER.log(Level.WARNING, null, ex);
            }
        });
    }

    private class LocalPredicate implements Predicate<Desordre> {

        final List<String> typeDesordresIds = new ArrayList<>();
        final List<String> typeUrgencesIds = new ArrayList<>();

        LocalPredicate(){
            for(final Element element : disordreTypesTable.getSelectedItems()){
                typeDesordresIds.add(element.getId());
            }
            for(final Element element : urgenceTypesTable.getSelectedItems()){
                typeUrgencesIds.add(element.getId());
            }
        }

        @Override
        public boolean test(final Desordre desordre) {

                /*
                On retire les désordres dont la dernière urgence ne figure pas
                dans les urgence choisies.
                 */
                final boolean urgenceOption;

                /*
                Si aucune urgence n'a été choisie on ne retire aucun désordre.
                */
                if(typeUrgencesIds.isEmpty()){
                    urgenceOption = false;
                }
                else{
                    // Recherche de la dernière observation.
                    final List<Observation> observations = desordre.getObservations();
                    Observation derniereObservation = null;
                    for(final Observation obs : observations){
                        if(obs.getDate()!=null){
                            if(derniereObservation==null) derniereObservation = obs;
                            else{
                                if(obs.getDate().isAfter(derniereObservation.getDate())) derniereObservation = obs;
                            }
                        }
                    }

                    /*
                    Si le désordre a une "dernière observation", on regarde si
                    son degré d'urgence figure dans les urgences choisies.
                    */
                    if(derniereObservation!=null){
                        urgenceOption = !typeUrgencesIds.contains(derniereObservation.getUrgenceId());
                    }
                    /*
                    Si on n'a pas pu déterminer de "dernière observation" :
                    On garde le désordre (mais c'est discutable).
                    */
                    else urgenceOption=false;
                }

                /*
                On retire le désordre s'il est d'un type qui n'est pas
                sélectionné dans la liste.
                */
                final boolean typeSelected; // Si le type du désordre n'est pas parmi les types sélectionnés
                // Si on n'a sélectionné aucun désordre, on laisse passer a priori quel que soit le type de désordre.
                if (typeDesordresIds.isEmpty()) typeSelected = false;
                // Si la liste de sélection des types de désordres n'est pas vide on vérifie de type de désordre
                else typeSelected = (desordre.getTypeDesordreId()==null
                        || !typeDesordresIds.contains(desordre.getTypeDesordreId()));

                return typeSelected || urgenceOption;
        }

    }
}
