package fr.sirs.plugins.synchro.ui;

import com.sun.javafx.collections.ObservableListWrapper;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Matthieu Bastianelli (Geomatys)
 */
public class FXTronconPathSelector extends StackPane {

    @FXML
    private ListView<Preview> uiTronconList;

    private final ObservableList<Preview> identificatedTroncons;

    @FXML
    private Button uiSearchDirectory;

    private final SimpleObjectProperty<PhotoDestination> photoDestination;


    public FXTronconPathSelector() {
        SIRS.loadFXML(this, FXTronconPathSelector.class);
        identificatedTroncons = FXCollections.observableList(new ArrayList<>());


        uiTronconList.setItems(identificatedTroncons);
        uiTronconList.setCellFactory(TextFieldListCell.forListView(new SirsStringConverter()));
        uiTronconList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//        uiTronconList.setEditable(false);

        this.photoDestination =  new SimpleObjectProperty<>();
        uiSearchDirectory.setOnAction(this::chooseDirectory);
    }

    void setPhotoDestination(final PhotoDestination photoDestination) {
        this.photoDestination.set(photoDestination);
        this.photoDestination.addListener((ov, t, t1) -> {
            this.photoDestination.set(t1); //Nécessaire?
        });
    }

    private void chooseDirectory(final ActionEvent ae) {
        final ObservableList<Preview> troncons = uiTronconList.getSelectionModel().getSelectedItems();
        if ((troncons == null) || (troncons.isEmpty())) {
                final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucun élément sélectionné. Choix du chemin impossible.");
                alert.setResizable(true);
                alert.showAndWait();
        } else {
            final Optional<File> chosenPath = photoDestination.get().chooseDirectoryFromSubDir();

            if (chosenPath.isPresent()) {
                final Path toSet = chosenPath.get().toPath();
                setPathTo(toSet, troncons.stream().map(p-> p.getElementId()).collect(Collectors.toList()));
            }//Else we do nothing and use the default path.
        }
    }

    void setPathTo(final Path pathToSet, final List<String> troncons){
        photoDestination.get().setPathToTroncons(pathToSet, troncons);
    }

    SimpleObjectProperty<PhotoDestination> getPhotoDestination() {
        return photoDestination;
    }

    void updateTronconList(final List<String> newIds) {
        identificatedTroncons.removeAll(uiTronconList.getSelectionModel().getSelectedItems());
        final List<Preview> previews = Injector.getSession().getPreviews().getByclassAndIds(TronconDigue.class, newIds);

        identificatedTroncons.addAll(previews);

    }




}
