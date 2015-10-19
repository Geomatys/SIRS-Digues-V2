package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Preview;
import java.util.Optional;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;

/**
 *
 * A table column for quick access to an element from its corresponding validitySummary.
 * 
 * @author Samuel Andrés (Geomatys)
 */
public class FXPreviewToElementDesignationTableColumn  extends TableColumn<Preview, Preview> {

    public FXPreviewToElementDesignationTableColumn(final String title) {
        super(title);
        setEditable(true);
        setSortable(true);
        setResizable(true);
        setPrefWidth(70);

        setCellValueFactory((TableColumn.CellDataFeatures<Preview, Preview> param) ->  new SimpleObjectProperty<>(param.getValue()));

        setCellFactory((TableColumn<Preview, Preview> param) -> new FXPreviewToElementDesignationTableCell());
    }

    private class FXPreviewToElementDesignationTableCell extends TableCell<Preview, Preview> {
        private final TextField field = new TextField();

    public FXPreviewToElementDesignationTableCell() {
        setGraphic(field);
        setAlignment(Pos.CENTER);
        setContentDisplay(ContentDisplay.CENTER);
        field.setOnAction(event -> {
            final Preview p = getItem();
            p.setDesignation(field.getText());
            commitEdit(p);
        });
    }

    @Override
    public void startEdit() {
        field.setText(getItem().getDesignation());
        super.startEdit();
        setText(null);
        setGraphic(field);
    }

    @Override
    public void commitEdit(Preview newValue) {
        setItem(newValue);
        final Optional<? extends Element> element = Injector.getSession().getElement(newValue);
        if(element.isPresent()){
            final Element elt = element.get();
            elt.setDesignation(newValue.getDesignation());

            final Element elementDocument = elt.getCouchDBDocument();
            if (elementDocument == null) {
                final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Un élément ne peut être sauvegardé sans document valide.", ButtonType.OK);
                alert.setResizable(true);
                alert.show();
                return;
            }

            ((AbstractSIRSRepository) Injector.getSession().getRepositoryForClass(elementDocument.getClass())).update(elementDocument);

        }
        super.commitEdit(newValue);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        updateItem(getItem(), false);
    }

    @Override
    protected void updateItem(Preview item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(null);
        if(item==null || empty){
            setText(null);
        }
        else {
            field.setText(item.getDesignation());
            setText(item.getDesignation());
        }
    }
    }
}
