package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ZoneVegetation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.stage.Modality;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ZoneVegetationPojoTable extends ListenPropertyPojoTable<String> {

    public ZoneVegetationPojoTable(String title) {
        super(ZoneVegetation.class, title);
    }
    
    @Override
    protected ZoneVegetation createPojo() {

        final ZoneVegetation position;

        final ChoiceStage stage = new ChoiceStage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        final Class<? extends ZoneVegetation> retrievedClass = stage.getRetrievedElement().get();
        if(retrievedClass!=null){
            final AbstractSIRSRepository zoneVegetationRepo = session.getRepositoryForClass(retrievedClass);
            position = (ZoneVegetation) zoneVegetationRepo.create();
            position.setForeignParentId(getPropertyReference());
            zoneVegetationRepo.add(position);
            getAllValues().add(position);
        }
        else {
            position = null;
        }
        return position;
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
