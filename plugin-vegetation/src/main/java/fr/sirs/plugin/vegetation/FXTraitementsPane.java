package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractPositionableRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.cell.ComboBoxListCell;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXTraitementsPane extends SplitPane {
    
    private final Session session = Injector.getSession();
    private final Previews previews = session.getPreviews();
    private final AbstractPositionableRepository<ParcelleVegetation> parcellesRepo = (AbstractPositionableRepository<ParcelleVegetation>) session.getRepositoryForClass(ParcelleVegetation.class);
    
    @FXML ComboBox<Preview> ui_linear;
    @FXML ListView<ParcelleVegetation> ui_parcelles;
    
    public FXTraitementsPane() {
        final ResourceBundle bundle = null;
        SIRS.loadFXML(this, bundle);
        
        SIRS.initCombo(ui_linear, FXCollections.observableList(previews.getByClass(TronconDigue.class)), null);
        
        
        ui_linear.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Preview>() {

            @Override
            public void changed(ObservableValue<? extends Preview> observable, Preview oldValue, Preview newValue) {
                ui_parcelles.setItems(FXCollections.observableList(parcellesRepo.getByLinearId(newValue.getElementId())));
            }
        });
        
        ui_parcelles.setCellFactory(ComboBoxListCell.forListView(new SirsStringConverter()));
        
    }

}
