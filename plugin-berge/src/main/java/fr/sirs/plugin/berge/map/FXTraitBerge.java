
package fr.sirs.plugin.berge.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Berge;
import fr.sirs.core.model.TraitBerge;
import fr.sirs.util.SirsStringConverter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.navigation.FXPanHandler;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTraitBerge extends GridPane{

    private static final String MESSAGE_BERGE = "Sélectionner une berge sur la carte.";
    private static final String MESSAGE_TRAIT = "Sélectionner un trait de berge sur la carte ou cliquer sur nouveau.";
    private static final String MESSAGE_TRAIT_IMPORT = "Sélectionner une géometrie à convertir en trait de berge sur la carte.";

    @FXML private Label uiLblBerge;
    @FXML private Label uiLblTrait;
    @FXML private DatePicker uiDateDebut;
    @FXML private DatePicker uiDateFin;
    @FXML private Button uiBtnDelete;
    @FXML private Button uiBtnSave;
    @FXML private ToggleButton uiBtnNew;

    private final BooleanProperty importProperty = new SimpleBooleanProperty(false);
    private final ObjectProperty<Berge> bergeProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<TraitBerge> traitProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<FXMap> mapProperty = new SimpleObjectProperty<>();

    public FXTraitBerge(){
        SIRS.loadFXML(this);
        
        getStylesheets().add(SIRS.CSS_PATH);
        getStyleClass().add("blue-light");

        uiLblBerge.setText(MESSAGE_BERGE);
        bergeProperty.addListener(new ChangeListener<Berge>() {
            @Override
            public void changed(ObservableValue<? extends Berge> observable, Berge oldValue, Berge newValue) {
                if(newValue!=null){
                    uiLblBerge.setText(new SirsStringConverter().toString(newValue));
                    uiLblTrait.setText(importProperty.get() ? MESSAGE_TRAIT_IMPORT :MESSAGE_TRAIT );
                }else{
                    uiLblBerge.setText(MESSAGE_BERGE);
                    uiLblTrait.setText("");
                }
            }
        });

        uiLblTrait.setText("");
        traitProperty.addListener(new ChangeListener<TraitBerge>() {
            @Override
            public void changed(ObservableValue<? extends TraitBerge> observable, TraitBerge oldValue, TraitBerge newValue) {
                if(newValue!=null){
                    uiLblTrait.setText(new SirsStringConverter().toString(newValue));
                }else{
                    uiLblTrait.setText(importProperty.get() ? MESSAGE_TRAIT_IMPORT :MESSAGE_TRAIT );
                }
            }
        });

        uiBtnNew.disableProperty().bind(bergeProperty.isNull().or(traitProperty.isNotNull()));
        uiBtnNew.visibleProperty().bind(importProperty.not());

        uiDateDebut.disableProperty().bind(traitProperty.isNull());
        uiDateFin.disableProperty().bind(traitProperty.isNull());
        uiBtnSave.disableProperty().bind(traitProperty.isNull());
        uiBtnDelete.disableProperty().bind(traitProperty.isNull());

    }

    public BooleanProperty importProperty(){
        return importProperty;
    }

    public ObjectProperty<Berge> bergeProperty(){
        return bergeProperty;
    }

    public ObjectProperty<TraitBerge> traitProperty(){
        return traitProperty;
    }

    public ObjectProperty<FXMap> mapProperty(){
        return mapProperty;
    }

    @FXML
    void delete(ActionEvent event) {
        final TraitBerge traitBerge = traitProperty.get();
        final AbstractSIRSRepository<TraitBerge> repo = Injector.getSession().getRepositoryForClass(TraitBerge.class);
        if(!traitBerge.isNew()){
            repo.remove(traitBerge);
        }
        endEdition();
    }

    @FXML
    void save(ActionEvent event) {
        final TraitBerge traitBerge = traitProperty.get();
        traitBerge.setDate_debut(uiDateDebut.getValue());
        traitBerge.setDate_fin(uiDateFin.getValue());
        final AbstractSIRSRepository<TraitBerge> repo = Injector.getSession().getRepositoryForClass(TraitBerge.class);
        if(traitBerge.isNew()){
            repo.add(traitBerge);
        }else{
            repo.update(traitBerge);
        }
        endEdition();
    }

    private void endEdition(){
        final FXMap map = mapProperty.get();
        if(map!=null){
            map.setHandler(new FXPanHandler(true));
        }
    }
}
