package fr.sirs.theme.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.AvecBornesTemporelles;
import java.time.LocalDate;
import java.util.function.Predicate;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;

/**
 * A simple editor for edition of temporal bornes of an object.
 * @author Alexis Manin (Geomatys)
 */
public class FXValidityPeriodPane extends BorderPane {

    @FXML private DatePicker uiDateDebut;
    @FXML private DatePicker uiDateFin;

    private final SimpleObjectProperty<AvecBornesTemporelles> target = new SimpleObjectProperty<>();

    private final SimpleBooleanProperty disableFieldsProperty = new SimpleBooleanProperty(false);

    public FXValidityPeriodPane() {
        super();
        SIRS.loadFXML(this);
        target.addListener(this::targetChanged);

        uiDateDebut.disableProperty().bind(disableFieldsProperty);
        uiDateFin.disableProperty().bind(disableFieldsProperty);

        uiDateDebut.setDayCellFactory((p) -> new FilteredDayCell((date) -> {
            final LocalDate fin = uiDateFin.valueProperty().get();
            return fin == null? true : (fin.isAfter(date) || fin.isEqual(date));
        }));

        uiDateFin.setDayCellFactory((p) -> new FilteredDayCell((date) -> {
            final LocalDate debut = uiDateDebut.valueProperty().get();
            return debut == null? true : (debut.isBefore(date) || debut.isEqual(date));
        }));
    }

    private void targetChanged(ObservableValue<? extends AvecBornesTemporelles> observable, AvecBornesTemporelles oldTarget, AvecBornesTemporelles newTarget) {
        if (oldTarget != null) {
            uiDateDebut.valueProperty().unbindBidirectional(oldTarget.date_debutProperty());
            uiDateFin.valueProperty().unbindBidirectional(oldTarget.date_finProperty());
        }

        if (newTarget != null) {
            uiDateDebut.valueProperty().bindBidirectional(newTarget.date_debutProperty());
            uiDateFin.valueProperty().bindBidirectional(newTarget.date_finProperty());
        }
    }

    public BooleanProperty disableFieldsProperty(){
        return disableFieldsProperty;
    }

    public SimpleObjectProperty<AvecBornesTemporelles> targetProperty() {
        return target;
    }

    /**
     * A cell activating only if a date matches input predicate.
     */
    private static class FilteredDayCell extends DateCell {

        private final Predicate<LocalDate> filter;

        public FilteredDayCell(final Predicate<LocalDate> p) {
            this.filter = p;
        }

        @Override
        public void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !filter.test(item)) {
                setDisable(true);
            }
        }
    }
}
