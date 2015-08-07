package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.LineString;
import fr.sirs.Injector;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.PositionConvention;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.util.StringConverter;

/**
 *
 * @author Samuel Andrés (Geomaty)
 */
public class PositionConventionPojoTable extends ListenPropertyPojoTable<String>{

    public PositionConventionPojoTable(String title) {
        super(PositionConvention.class, title);
    }
    
    @Override
    protected PositionConvention createPojo() {
        final Alert choice = new Alert(Alert.AlertType.NONE, 
                "Le positionnement de convention est associable à un objet.\n"
                        + "L'association à un objet est facultative et peut être réalisée ultérieurement.\n"
                        + "Voulez-vous associer un objet maintenant ?\n", 
                ButtonType.YES, ButtonType.NO);
        choice.setResizable(true);
        final Optional<ButtonType> result = choice.showAndWait();
        
        final PositionConvention position;
        if(result.isPresent()&&result.get()==ButtonType.YES){
            
            final ChoiceStage stage = new ChoiceStage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            final Element retrievedElement = stage.getRetrievedElement().get();

            final TronconDigue troncon;
            if(retrievedElement instanceof Objet && ((Objet) retrievedElement).getLinearId()!=null){
                troncon = Injector.getSession().getRepositoryForClass(TronconDigue.class).get(((Objet) retrievedElement).getLinearId());
            }
            else {
                troncon = Injector.getSession().getRepositoryForClass(TronconDigue.class).getOne();

            }

            position = (PositionConvention) super.createPojo(troncon);


            if(retrievedElement instanceof Objet){
                final Objet retrievedObjet = (Objet) retrievedElement;
                position.setSystemeRepId(retrievedObjet.getSystemeRepId());
                position.setPrDebut(stage.prMinProperty().floatValue());
                position.setPrFin(stage.prMaxProperty().floatValue());
                final LineString positionGeometry = LinearReferencingUtilities.buildSubGeometry(retrievedObjet, position, 
                        Injector.getSession().getRepositoryForClass(BorneDigue.class), 
                        Injector.getSession().getRepositoryForClass(SystemeReperage.class));
                position.setPositionDebut(positionGeometry.getStartPoint());
                position.setPositionFin(positionGeometry.getEndPoint());
                position.setGeometry(positionGeometry);
                position.setLinearId(retrievedObjet.getLinearId());
            }
                
            position.setObjetId(retrievedElement.getId());
                
            try {
                ((Property<String>) propertyMethodToListen.invoke(position)).setValue(propertyReference);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(PositionDocumentPojoTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            final TronconDigue premierTroncon = Injector.getSession().getRepositoryForClass(TronconDigue.class).getOne();
            position = (PositionConvention) super.createPojo(premierTroncon);

            try {
                ((Property<String>) propertyMethodToListen.invoke(position)).setValue(propertyReference);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(PositionDocumentPojoTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return position;
    }
    
    private static class ChoiceStage extends PojoTableChoiceStage {
        private final DoubleProperty prMinProperty = new SimpleDoubleProperty();
        private final DoubleProperty prMaxProperty = new SimpleDoubleProperty();
        private final StringConverter converter = new SirsStringConverter();
        
        private ChoiceStage(){
            super();
            setTitle("Choix de l'élément");
            final FXPositionConventionChoicePane positionConventionChoicePane = new FXPositionConventionChoicePane();
            setScene(new Scene(positionConventionChoicePane));
            retrievedElement.bind(positionConventionChoicePane.selectedObjetProperty());
            prMinProperty.bind(positionConventionChoicePane.prDebutProperty());
            prMaxProperty.bind(positionConventionChoicePane.prFinProperty());
            
            retrievedElement.addListener(new ChangeListener<Element>() {

                @Override
                public void changed(ObservableValue<? extends Element> observable, Element oldValue, Element newValue) {
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
        
        public DoubleProperty prMinProperty(){return prMinProperty;}
        public DoubleProperty prMaxProperty(){return prMaxProperty;}
        
    }
}
