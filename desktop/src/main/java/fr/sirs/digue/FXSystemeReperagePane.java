package fr.sirs.digue;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXSystemeReperagePane extends BorderPane {

    @FXML private TextField uiNom;
    @FXML private HTMLEditor uiComment;
    @FXML private DatePicker uiDate;

    private final ObjectProperty<SystemeReperage> srProperty = new SimpleObjectProperty<>();
    private final BorneTable borneTable = new BorneTable();
    private final BooleanProperty editableProperty = new SimpleBooleanProperty(true);

    public FXSystemeReperagePane(){
        SIRS.loadFXML(this);

        setCenter(borneTable);

        srProperty.addListener(this::updateFields);

        this.visibleProperty().bind(srProperty.isNotNull());

        uiNom.editableProperty().bind(editableProperty);
        uiComment.disableProperty().bind(editableProperty.not());
        uiDate.setDisable(true);
        // Client query...
        borneTable.editableProperty().set(false);
    }

    public BooleanProperty editableProperty(){
        return editableProperty;
    }

    public ObjectProperty<SystemeReperage> getSystemeReperageProperty() {
        return srProperty;
    }

    private void updateFields(ObservableValue<? extends SystemeReperage> observable, SystemeReperage oldValue, SystemeReperage newValue) {
        if (oldValue != null) {
            uiNom.textProperty().unbindBidirectional(oldValue.libelleProperty());
            uiDate.valueProperty().unbindBidirectional(oldValue.dateMajProperty());
            borneTable.getUiTable().setItems(FXCollections.emptyObservableList());
        }

        if(newValue==null) return;
        uiNom.textProperty().bindBidirectional(newValue.libelleProperty());
        uiDate.valueProperty().bindBidirectional(newValue.dateMajProperty());
        uiComment.setHtmlText(newValue.getCommentaire());

        borneTable.getUiTable().setItems(newValue.systemeReperageBornes);
    }

    public void save(){
        final SystemeReperage sr = srProperty.get();
        if(sr==null) return;

        sr.setCommentaire(uiComment.getHtmlText());
        final Session session = Injector.getBean(Session.class);
        final SystemeReperageRepository repo = (SystemeReperageRepository) session.getRepositoryForClass(SystemeReperage.class);

        final String tcId = sr.getLinearId();
        if (tcId == null || tcId.isEmpty()) {
            throw new IllegalArgumentException("Aucun tronçon n'est associé au SR. Sauvegarde impossible.");
        }
        final TronconDigue troncon = session.getRepositoryForClass(TronconDigue.class).get(tcId);
        repo.update(sr, troncon);
    }

    private class BorneTable extends PojoTable {

        public BorneTable() {
            super(SystemeReperageBorne.class, "Liste des bornes");
            getColumns().remove((TableColumn) editCol);
            fichableProperty.set(false);
            uiAdd.setVisible(false);
            uiDelete.setVisible(false);
            uiFicheMode.setVisible(false);
        }

        @Override
        protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
            //on ne sauvegarde pas, le formulaire conteneur s'en charge
        }
    }

}
