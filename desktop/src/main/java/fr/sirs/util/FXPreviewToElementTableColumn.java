package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.Preview;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;

/**
 *
 * A table column for quick access to an element from its corresponding validitySummary.
 * 
 * @author Samuel Andrés (Geomatys)
 */
public class FXPreviewToElementTableColumn  extends TableColumn<Preview, Preview> {

    public FXPreviewToElementTableColumn() {
        super("Détail");
        setEditable(false);
        setSortable(false);
        setResizable(true);
        setPrefWidth(70);

        setCellValueFactory((TableColumn.CellDataFeatures<Preview, Preview> param) -> {
            return new SimpleObjectProperty<>(param.getValue());
        });

        setCellFactory((TableColumn<Preview, Preview> param) -> {
            return new FXPreviewToElementButtonTableCell();
        });
    }

    private class FXPreviewToElementButtonTableCell extends ButtonTableCell<Preview, Preview> {

        public FXPreviewToElementButtonTableCell() {
            super(false, null, 
                    (Preview t) -> true, 
                    (Preview t) -> {
                        Injector.getSession().showEditionTab(t);
                        return t;
                    });
        }

        @Override
        protected void updateItem(Preview item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                button.setGraphic(new ImageView(SIRS.ICON_EYE_BLACK));
            }
        }
    }
}
