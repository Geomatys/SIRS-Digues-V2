
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.core.component.AbstractPositionDocumentRepository;
import fr.sirs.core.model.Convention;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXConventionPane extends FXConventionPaneStub {

    // Surcharge du tableau des positions
    final PositionConventionPojoTable positionDocumentTable;

    /**
     * Constructor. Initialize part of the UI which will not require update when element edited change.
     */
    private FXConventionPane() {

        super();
        positionDocumentTable = new PositionConventionPojoTable(null);
        positionDocumentTable.editableProperty().bind(disableFieldsProperty().not());
        ui_positionDocument.setContent(positionDocumentTable);
        ui_positionDocument.setClosable(false);
    }
    
    public FXConventionPane(final Convention convention){
        this();
        this.elementProperty().set(convention);
//
//        organismeSignataireIdsTable.setObservableListToListen(elementProperty.get().getOrganismeSignataireIds());
//        contactSignataireIdsTable.setObservableListToListen(elementProperty.get().getContactSignataireIds());
    }


    /**
     * Initialize fields at element setting.
     * @param observableElement
     * @param oldElement
     * @param newElement
     */
    @Override
    protected void initFields(ObservableValue<? extends Convention > observableElement, Convention oldElement, Convention newElement) {

        super.initFields(observableElement, oldElement, newElement);
        positionDocumentTable.setPropertyToListen("sirsdocumentProperty", elementProperty().get().getId());
        positionDocumentTable.setTableItems(()-> (ObservableList) AbstractPositionDocumentRepository.getPositionDocumentByDocumentId(elementProperty().get().getId(), Injector.getSession()));
    }

}
