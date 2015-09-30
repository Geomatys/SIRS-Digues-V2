
package fr.sirs.theme.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.OuvrageAssocieLit;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.TronconLitAssociable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXOuvrageAssocieLitPane extends FXOuvrageAssocieLitPaneStub {

    @FXML protected ComboBox<Class> ui_typeOuvrageId;
    private final ObservableList<Class> types = FXCollections.observableArrayList();

    /**
     * Constructor. Initialize part of the UI which will not require update when 
     * element edited change.
     */
    protected FXOuvrageAssocieLitPane() {
        super();
        ui_typeOuvrageId.disableProperty().bind(disableFieldsProperty());

        final ObservableList<Preview> prev = FXCollections.observableList(
            previewRepository.getByClass(TronconLitAssociable.class));

        final List<String> classNames = new ArrayList<>();
        for(final Preview preview : prev){
            final String type = preview.getElementClass();
            if(type!=null && !classNames.contains(type)){
                classNames.add(type);
                final Class clazz;
                try {
                    clazz = Class.forName(type);
                    if(clazz!=null && TronconLitAssociable.class.isAssignableFrom(clazz)){
                        types.add(clazz);
                    }
                } catch (ClassNotFoundException ex) {
                    SIRS.LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }
    }
    
    public FXOuvrageAssocieLitPane(final OuvrageAssocieLit ouvrageAssocieLit){
        this();
        this.elementProperty().set(ouvrageAssocieLit);

    }     

    /**
     * Initialize fields at element setting.
     * @param observableElement
     * @param oldElement
     * @param newElement
     */
    @Override
    protected void initFields(ObservableValue<? extends OuvrageAssocieLit > observableElement, OuvrageAssocieLit oldElement, OuvrageAssocieLit newElement) {
        super.initFields(observableElement, oldElement, newElement);

        ui_typeOuvrageId.getSelectionModel().selectedItemProperty().addListener(new WeakChangeListener<>(new ChangeListener<Class>() {

            @Override
            public void changed(ObservableValue<? extends Class> observable, Class oldValue, Class newValue) {
                SIRS.initCombo(ui_ouvrageId, FXCollections.observableList(
            previewRepository.getByClass(newValue)),
            newElement.getOuvrageId() == null ? null : previewRepository.get(newElement.getOuvrageId()));
            }
        }));

        final Preview ouvragePreview = newElement.getOuvrageId() == null ? null : previewRepository.get(newElement.getOuvrageId());
        Class clazz = null;
        if(ouvragePreview!=null){
            try {
                clazz = Class.forName(ouvragePreview.getElementClass());
            } catch (ClassNotFoundException ex) {
                SIRS.LOGGER.log(Level.WARNING, null, ex);
            }
        }
        SIRS.initCombo(ui_typeOuvrageId, types, clazz);

    }
}
