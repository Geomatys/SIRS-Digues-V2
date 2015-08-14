package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.util.SirsStringConverter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.util.StringConverter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ZoneVegetationPojoTable extends ListenPropertyPojoTable<String> {

    public ZoneVegetationPojoTable(String title) {
        super(ZoneVegetation.class, title);
    }
    
//    @Override
//    protected ZoneVegetation createPojo() {
//        final TronconDigue premierTroncon = Injector.getSession().getRepositoryForClass(TronconDigue.class).getOne();
//        final T position = (T) super.createPojo(premierTroncon);
//        
//        try {
//            ((Property<String>) propertyMethodToListen.invoke(position)).setValue(propertyReference);
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//            Logger.getLogger(PositionDocumentPojoTable.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return position;
//    }
    
    
    
    @Override
    protected ZoneVegetation createPojo() {
        final Alert choice = new Alert(Alert.AlertType.NONE, 
                "Vous devez choisir le type de la zone à ajouter. Voulez-vous continuer ?", 
                ButtonType.YES, ButtonType.NO);
        choice.setResizable(true);
        final Optional<ButtonType> result = choice.showAndWait();
        
        final ZoneVegetation position;
        if(result.isPresent()&&result.get()==ButtonType.YES){

            final ChoiceStage stage = new ChoiceStage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            final Class<? extends ZoneVegetation> retrievedElement = stage.getRetrievedElement().get();


            final AbstractSIRSRepository repo = Injector.getSession().getRepositoryForClass(retrievedElement);
            position = (ZoneVegetation) repo.create();
            position.setForeignParentId(getPropertyReference());
            repo.add(position);
            
//            final TronconDigue troncon;
//            if(retrievedElement instanceof Objet && ((Objet) retrievedElement).getLinearId()!=null){
//                troncon = Injector.getSession().getRepositoryForClass(TronconDigue.class).get(((Objet) retrievedElement).getLinearId());
//            }
//            else {
//                troncon = Injector.getSession().getRepositoryForClass(TronconDigue.class).getOne();
//
//            }
//
//            position = (ZoneVegetation) super.createPojo(troncon);
//
//
//            if(retrievedElement instanceof Objet){
//                final Objet retrievedObjet = (Objet) retrievedElement;
//                position.setSystemeRepId(retrievedObjet.getSystemeRepId());
//                position.setPrDebut(stage.prMinProperty().floatValue());
//                position.setPrFin(stage.prMaxProperty().floatValue());
//                final LineString positionGeometry = LinearReferencingUtilities.buildSubGeometry(retrievedObjet, position, 
//                        Injector.getSession().getRepositoryForClass(BorneDigue.class), 
//                        Injector.getSession().getRepositoryForClass(SystemeReperage.class));
//                position.setPositionDebut(positionGeometry.getStartPoint());
//                position.setPositionFin(positionGeometry.getEndPoint());
//                position.setGeometry(positionGeometry);
//                position.setLinearId(retrievedObjet.getLinearId());
//            }
//                
//            position.setObjetId(retrievedElement.getId());
//                
//            try {
//                ((Property<String>) propertyMethodToListen.invoke(position)).setValue(propertyReference);
//            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//                Logger.getLogger(PositionDocumentPojoTable.class.getName()).log(Level.SEVERE, null, ex);
//            }
        } else {
            position = null;
        }
        return position;
    }

    private static class ChoiceStage extends PojoTableComboBoxChoiceStage<Class<? extends ZoneVegetation>, Class<? extends ZoneVegetation>> {

        private final StringConverter converter = new SirsStringConverter();
        
        private ChoiceStage(){
            super();
            setTitle("Choix de l'élément");

            final List<Class<? extends Element>> classes = Session.getElements();

            final List<Class<? extends ZoneVegetation>> zoneTypes = new ArrayList<>();
            for(final Class element : classes){
                if(ZoneVegetation.class.isAssignableFrom(element) && !Modifier.isAbstract(element.getModifiers())){
                    zoneTypes.add(element);
                }
            }

            comboBox.setItems(FXCollections.observableList(zoneTypes));

            retrievedElement.bind(comboBox.getSelectionModel().selectedItemProperty());

            setScene(new Scene(comboBox));
//            retrievedElement.bind(positionConventionChoicePane.selectedObjetProperty());
            
            retrievedElement.addListener(new ChangeListener<Class<? extends ZoneVegetation>>() {

                @Override
                public void changed(ObservableValue<? extends Class<? extends ZoneVegetation>> observable, Class<? extends ZoneVegetation> oldValue, Class<? extends ZoneVegetation> newValue) {
                    final Alert alert;
                    if(newValue==null){
                        alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez allez quitter la procédure de choix sans avoir choisi d'élément associé à la convention.", ButtonType.YES, ButtonType.NO);
                    }
                    else {
                        alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez vous choisir "+converter.toString(newValue)+" et quitter la procédure de choix ?", ButtonType.YES, ButtonType.NO);
                    }
                    alert.setResizable(true);
                    
                    final Optional<ButtonType> result = alert.showAndWait();
                    if(result.isPresent() && result.get()==ButtonType.YES){
                        hide();
                    }
                }
            });
        }
    }
}
