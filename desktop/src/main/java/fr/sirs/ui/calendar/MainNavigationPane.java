package fr.sirs.ui.calendar;

import javafx.beans.binding.BooleanBinding;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

import java.util.Calendar;

/**
 * The main navigation pane.
 *
 * @author Christian Schudt
 */
final class MainNavigationPane extends HBox {
    private static final ImageView ICON_PREVIOUS  = new ImageView(SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_ANGLE_LEFT, 16,
            FontAwesomeIcons.DEFAULT_COLOR), null));
    private static final ImageView ICON_NEXT  = new ImageView(SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_ANGLE_RIGHT, 16,
            FontAwesomeIcons.DEFAULT_COLOR), null));

    private static final String CSS_CALENDAR_NAVIGATION_BUTTON = "calendar-navigation-button";
    private static final String CSS_CALENDAR_NAVIGATION_TITLE = "calendar-navigation-title";
    private static final String CSS_CALENDAR_HEADER = "calendar-header";

    private CalendarView calendarView;
    Button titleButton;

    public MainNavigationPane(final CalendarView calendarView) {

        this.calendarView = calendarView;


        titleButton = new Button();
        titleButton.getStyleClass().add(CSS_CALENDAR_NAVIGATION_TITLE);
        titleButton.textProperty().bind(calendarView.title);

        titleButton.setOnAction(actionEvent -> {
            switch (calendarView.currentlyViewing.get()) {
                case Calendar.MONTH:
                    calendarView.currentlyViewing.set(Calendar.YEAR);
                    break;
                case Calendar.YEAR:
                    calendarView.currentlyViewing.set(Calendar.ERA);
            }
        });
        titleButton.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(calendarView.ongoingTransitions, calendarView.currentlyViewing);
            }

            @Override
            protected boolean computeValue() {
                return calendarView.currentlyViewing.get() == Calendar.ERA || calendarView.ongoingTransitions.get() > 0;
            }
        });
        HBox buttonBox = new HBox();
        buttonBox.getChildren().add(titleButton);
        buttonBox.setAlignment(Pos.CENTER);

        HBox.setHgrow(buttonBox, Priority.ALWAYS);

        getChildren().add(getNavigationButton(-1));
        getChildren().add(buttonBox);
        getChildren().add(getNavigationButton(1));

        getStyleClass().add(CSS_CALENDAR_HEADER);
    }

    /**
     * Gets a navigation button.
     *
     * @param direction Either -1 (for left) or 1 (for right).
     * @return The button.
     */
    private Button getNavigationButton(final int direction) {

        Button button = new Button();

        button.setOnAction(actionEvent -> {
            Calendar calendar = calendarView.getCalendar();
            switch (calendarView.currentlyViewing.get()) {
                case Calendar.MONTH:
                    calendar.add(Calendar.MONTH, 1 * direction);
                    break;
                case Calendar.YEAR:
                    calendar.add(Calendar.YEAR, 1 * direction);
                    break;
                case Calendar.ERA:
                    calendar.add(Calendar.YEAR, 20 * direction);
                    break;
            }

            calendarView.calendarDate.set(calendar.getTime());
        });

        button.setMaxWidth(Control.USE_PREF_SIZE);
        button.setMaxHeight(Control.USE_PREF_SIZE);
        button.setGraphic(direction > 0 ? ICON_NEXT : ICON_PREVIOUS);

        button.getStyleClass().add(CSS_CALENDAR_NAVIGATION_BUTTON);
        return button;
    }

}
