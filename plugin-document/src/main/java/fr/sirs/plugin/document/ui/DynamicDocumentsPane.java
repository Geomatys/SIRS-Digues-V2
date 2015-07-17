package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class DynamicDocumentsPane extends BorderPane implements Initializable {
    @FXML private ComboBox<Preview> uiSECombo;

    @FXML private ListView<TronconDigue> uiTronconsList;

    public DynamicDocumentsPane() {
        SIRS.loadFXML(this, DynamicDocumentsPane.class);
        Injector.injectDependencies(this);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final Previews previewRepository = Injector.getSession().getPreviews();

        uiSECombo.setEditable(false);
        uiSECombo.valueProperty().addListener(this::systemeEndiguementChange);
        uiTronconsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        final SirsStringConverter converter = new SirsStringConverter();
        uiTronconsList.setCellFactory(new Callback<ListView<TronconDigue>, ListCell<TronconDigue>>() {
            @Override
            public ListCell<TronconDigue> call(ListView<TronconDigue> param) {
                return new ListCell<TronconDigue>() {
                    @Override
                    protected void updateItem(TronconDigue item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(converter.toString(item));
                    }
                };
            }
        });

        uiSECombo.setConverter(new SirsStringConverter());
        uiSECombo.setItems(FXCollections.observableArrayList(
                previewRepository.getByClass(SystemeEndiguement.class)));
        if(uiSECombo.getItems()!=null){
            uiSECombo.getSelectionModel().select(0);
        }

    }

    private void systemeEndiguementChange(ObservableValue<? extends Preview> observable,
                                          Preview oldValue, Preview newValue) {
        if(newValue==null){
            uiTronconsList.setItems(FXCollections.emptyObservableList());
        }else{
            final SystemeEndiguementRepository sdRepo = (SystemeEndiguementRepository) Injector.getSession().getRepositoryForClass(SystemeEndiguement.class);
            final DigueRepository digueRepo = (DigueRepository) Injector.getSession().getRepositoryForClass(Digue.class);
            final TronconDigueRepository tronconRepo = (TronconDigueRepository) Injector.getSession().getRepositoryForClass(TronconDigue.class);
            final SystemeEndiguement sd = sdRepo.get(newValue.getElementId());
            final Set<TronconDigue> troncons = new HashSet<>();
            final List<Digue> digues = digueRepo.getBySystemeEndiguement(sd);
            for(Digue digue : digues){
                troncons.addAll(tronconRepo.getByDigue(digue));
            }
            uiTronconsList.setItems(FXCollections.observableArrayList(troncons));
        }
    }
}
