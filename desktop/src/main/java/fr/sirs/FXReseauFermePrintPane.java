package fr.sirs;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.RefConduiteFermee;
import fr.sirs.core.model.RefUrgence;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXReseauFermePrintPane extends TemporalTronconChoicePrintPane {
    
    @FXML private Tab uiConduiteTypeChoice;
    @FXML private Tab uiUrgenceTypeChoice;
    
    @FXML private CheckBox uiOptionPhoto;
    @FXML private CheckBox uiOptionReseauOuvrage;
    @FXML private CheckBox uiOptionVoirie;

    private final TypeChoicePojoTable conduiteTypesTable = new TypeChoicePojoTable(RefConduiteFermee.class, "Types de conduites fermées");
    private final TypeChoicePojoTable urgenceTypesTable = new TypeChoicePojoTable(RefUrgence.class, "Types d'urgences");
    
    public FXReseauFermePrintPane(){
        super(FXReseauFermePrintPane.class);
        conduiteTypesTable.setTableItems(()-> (ObservableList) FXCollections.observableList(Injector.getSession().getRepositoryForClass(RefConduiteFermee.class).getAll()));
        conduiteTypesTable.commentAndPhotoProperty().set(false);
        uiConduiteTypeChoice.setContent(conduiteTypesTable);
        urgenceTypesTable.setTableItems(()-> (ObservableList) FXCollections.observableList(Injector.getSession().getRepositoryForClass(RefUrgence.class).getAll()));
        urgenceTypesTable.commentAndPhotoProperty().set(false);
        uiUrgenceTypeChoice.setContent(urgenceTypesTable);
    }
    
    @FXML private void cancel(){
        
    }
    
    @FXML 
    private void print(){
        Injector.getSession().getTaskManager().submit("Génération de fiches détaillées de réseaux hydrauliques fermés",
        () -> {
            
            final List<ReseauHydrauliqueFerme> reseauxFermes = Injector.getSession().getRepositoryForClass(ReseauHydrauliqueFerme.class).getAll();
            
            // On retire les désordres de la liste dans les cas suivants :
            reseauxFermes.removeIf(
                    new LocalPredicate().or(
                            new LocationPredicate<>().or(
                                    (Predicate) new TemporalPredicate())));
            
            try {
                if(!reseauxFermes.isEmpty()){
                    Injector.getSession().getPrintManager().printReseaux(reseauxFermes, uiOptionPhoto.isSelected(), uiOptionReseauOuvrage.isSelected(), uiOptionVoirie.isSelected());
                }
            } catch (Exception ex) {
                SIRS.LOGGER.log(Level.WARNING, null, ex);
            }
        });
    }

    private class LocalPredicate implements Predicate<ReseauHydrauliqueFerme>{

        final List<String> typeConduitesIds = new ArrayList<>();

        LocalPredicate(){
            for(final Element element : conduiteTypesTable.getSelectedItems()){
                typeConduitesIds.add(element.getId());
            }
        }

        @Override
        public boolean test(final ReseauHydrauliqueFerme reseauFerme) {

            /*
            On retire le réseau s'il est d'un type qui n'est pas sélectionné
            dans la liste.
            */
            final boolean typeSelected;
            // Si on n'a sélectionné aucun désordre, on laisse passer a priori quel que soit le type de désordre.
            if (typeConduitesIds.isEmpty()) typeSelected = false;
            // Si la liste de sélection des types de désordres n'est pas vide on vérifie de type de désordre
            else typeSelected = (reseauFerme.getTypeConduiteFermeeId()==null
                    || !typeConduitesIds.contains(reseauFerme.getTypeConduiteFermeeId()));

            return typeSelected;
        }
    }
    
}
