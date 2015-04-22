package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.ValiditySummary;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;

/**
 *
 * A table column for quick access to an element from its corresponding validitySummary.
 * 
 * @author Samuel Andrés (Geomatys)
 */
public class FXValiditySummaryToElementTableColumn  extends TableColumn<ValiditySummary, ValiditySummary> {

    public FXValiditySummaryToElementTableColumn() {
        super("Détail");
        setEditable(false);
        setSortable(false);
        setResizable(true);
        setPrefWidth(70);

        setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValiditySummary, ValiditySummary>, ObservableValue<ValiditySummary>>() {
            @Override
            public ObservableValue<ValiditySummary> call(TableColumn.CellDataFeatures<ValiditySummary, ValiditySummary> param) {
                return new SimpleObjectProperty<>(param.getValue());
            }
        });

        setCellFactory(new Callback<TableColumn<ValiditySummary, ValiditySummary>, TableCell<ValiditySummary, ValiditySummary>>() {

            @Override
            public TableCell<ValiditySummary, ValiditySummary> call(TableColumn<ValiditySummary, ValiditySummary> param) {

                return new FXValiditySummaryToElementButtonTableCell();
            }
        });
    }

    private class FXValiditySummaryToElementButtonTableCell extends ButtonTableCell<ValiditySummary, ValiditySummary> {

        public FXValiditySummaryToElementButtonTableCell() {
            super(false, null, (ValiditySummary t) -> true, new Function<ValiditySummary, ValiditySummary>() {
                @Override
                public ValiditySummary apply(ValiditySummary t) {
                    Injector.getSession().showEditionTab(t);
                    return t;
                }
            });
        }

        @Override
        protected void updateItem(ValiditySummary item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                button.setGraphic(new ImageView(SIRS.ICON_EYE));
            }
        }
    }
}
