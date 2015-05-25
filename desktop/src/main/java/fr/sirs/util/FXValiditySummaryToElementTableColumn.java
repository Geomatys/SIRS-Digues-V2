package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.Preview;
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
public class FXValiditySummaryToElementTableColumn  extends TableColumn<Preview, Preview> {

    public FXValiditySummaryToElementTableColumn() {
        super("Détail");
        setEditable(false);
        setSortable(false);
        setResizable(true);
        setPrefWidth(70);

        setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Preview, Preview>, ObservableValue<Preview>>() {
            @Override
            public ObservableValue<Preview> call(TableColumn.CellDataFeatures<Preview, Preview> param) {
                return new SimpleObjectProperty<>(param.getValue());
            }
        });

        setCellFactory(new Callback<TableColumn<Preview, Preview>, TableCell<Preview, Preview>>() {

            @Override
            public TableCell<Preview, Preview> call(TableColumn<Preview, Preview> param) {

                return new FXValiditySummaryToElementButtonTableCell();
            }
        });
    }

    private class FXValiditySummaryToElementButtonTableCell extends ButtonTableCell<Preview, Preview> {

        public FXValiditySummaryToElementButtonTableCell() {
            super(false, null, (Preview t) -> true, new Function<Preview, Preview>() {
                @Override
                public Preview apply(Preview t) {
                    Injector.getSession().showEditionTab(t);
                    return t;
                }
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
