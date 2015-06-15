package fr.sirs.ui.calendar;

import javafx.beans.Observable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

/**
 * Responsible for displaying the days of a month.
 *
 * @author Christian Schudt
 */
final class MonthView extends DatePane {

    private static final String CSS_CALENDAR_MONTH_VIEW = "calendar-month-view";
    private static final String CSS_CALENDAR_DAY_CURRENT_MONTH = "calendar-cell-current-month";
    private static final String CSS_CALENDAR_DAY_OTHER_MONTH = "calendar-cell-other-month";
    private static final String CSS_CALENDAR_TODAY = "calendar-cell-today";
    private static final String CSS_CALENDAR_SELECTED = "calendar-cell-selected";
    private static final String CSS_CALENDAR_WEEKDAYS = "calendar-weekdays";
    private static final String CSS_CALENDAR_WEEK_NUMBER = "calendar-week-number";

    /**
     * The number of days per week.
     * I don't know if there is a culture with more or less than seven days per week, but theoretically {@link java.util.Calendar} allows it.
     * This variable will correspond to the number of columns.
     */
    private int numberOfDaysPerWeek;

    /**
     * Constructs the month view.
     *
     * @param calendarView The calendar view.
     */
    public MonthView(final CalendarView calendarView) {
        super(calendarView);

        getStyleClass().add(CSS_CALENDAR_MONTH_VIEW);

        // When the locale changed, update the weeks to the new locale.
        calendarView.localeProperty().addListener(observable -> {
            updateContent();
        });

        // When the disabled week days change, update the days.
        calendarView.getDisabledWeekdays().addListener((Observable observable) -> {
            updateDays();
        });

        // When the disabled dates change, update the days.
        calendarView.getDisabledDates().addListener((Observable observable) -> {
            updateDays();
        });

        // When the disabled dates change, update the days.
        calendarView.showWeeksProperty().addListener(observable -> {
            getChildren().clear();
            buildContent();
            updateContent();
        });
        
        calendarView.selectedDateProperty().addListener(observable -> {
            updateContent();
        });
        
        calendarView.calendarDate.addListener(observable -> {
            updateContent();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildContent() {
        Calendar calendar = calendarView.calendarProperty().get();

        // get the maximum number of days in a week for this calendar.
        numberOfDaysPerWeek = calendar.getMaximum(Calendar.DAY_OF_WEEK);

        // get the maximum number of days a month could have.
        int maxNumberOfDaysInMonth = calendar.getMaximum(Calendar.DAY_OF_MONTH);

        // assume the first row has only 1 day, then distribute the rest among the remaining weeks and add the first week.
        int numberOfRows = (int) Math.ceil((maxNumberOfDaysInMonth - 1) / (double) numberOfDaysPerWeek) + 1;

        // remove all controls
        getChildren().clear();

        int colOffset = calendarView.getShowWeeks() ? 1 : 0;

        if (calendarView.getShowWeeks()) {
            Label empty = new Label();
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.getStyleClass().add(CSS_CALENDAR_WEEKDAYS);
            add(empty, 0, 0);
        }
        // iterate through the columns
        for (int i = 0; i < numberOfDaysPerWeek; i++) {
            Label label = new Label();
            label.getStyleClass().add(CSS_CALENDAR_WEEKDAYS);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            add(label, i + colOffset, 0);
        }

        // iterate through the rows
        for (int rowIndex = 0; rowIndex < numberOfRows; rowIndex++) {

            if (calendarView.getShowWeeks()) {
                Label label = new Label();
                label.setMaxWidth(Double.MAX_VALUE);
                label.setMaxHeight(Double.MAX_VALUE);
                label.getStyleClass().add(CSS_CALENDAR_WEEK_NUMBER);
                add(label, 0, rowIndex + 1);
            }

            // iterate through the columns
            for (int colIndex = 0; colIndex < numberOfDaysPerWeek; colIndex++) {
                final Button button = new Button();
                button.setMaxWidth(Double.MAX_VALUE);
                button.setMaxHeight(Double.MAX_VALUE);

                setVgrow(button, Priority.ALWAYS);
                setHgrow(button, Priority.ALWAYS);
                button.setOnAction(actionEvent -> calendarView.selectedDate.set((Date) button.getUserData()));
                // add the button, starting at second row.
                add(button, colIndex + colOffset, rowIndex + 1);
            }
        }


    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateContent() {
        updateDays();
        updateWeekNames();
    }

    /**
     * Updates the days.
     */
    private void updateDays() {
        Calendar calendar = calendarView.getCalendar();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, calendarView.localeProperty().get());

        dateFormat.setCalendar(calendarView.getCalendar());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);


        // Set the calendar to the first day of the current month.
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int month = calendar.get(Calendar.MONTH);

        Date tmp = calendar.getTime();

        calendar.setTime(new Date());
        int todayDay = calendar.get(Calendar.DATE);
        int todayMonth = calendar.get(Calendar.MONTH);
        int todayYear = calendar.get(Calendar.YEAR);
        
        if (calendarView.selectedDate.get() != null) {
            calendar.setTime(calendarView.selectedDate.get());
        }
        int selectedDay = calendar.get(Calendar.DATE);
        int selectedMonth = calendar.get(Calendar.MONTH);
        int selectedYear = calendar.get(Calendar.YEAR);
        
        calendar.setTime(tmp);

        // Set the calendar to the end of the previous month.
        while (calendar.getFirstDayOfWeek() != calendar.get(Calendar.DAY_OF_WEEK)) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        // Prepare buttons style
        final ColumnConstraints colSmallCstr = new ColumnConstraints(Region.USE_PREF_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_PREF_SIZE,
                Priority.NEVER, HPos.RIGHT, true);
        final ColumnConstraints colLargeTxtEventCstr = new ColumnConstraints(Region.USE_PREF_SIZE, Region.USE_COMPUTED_SIZE, Double.MAX_VALUE,
                Priority.ALWAYS, HPos.LEFT, true);
        final Border btnBorder = new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, new BorderWidths(1)));

        // Ignore the week day row and the week number column
        for (int i = numberOfDaysPerWeek + (calendarView.getShowWeeks() ? 1 : 0); i < getChildren().size(); i++) {
            final Date currentDate = calendar.getTime();
            if (i % (numberOfDaysPerWeek + 1) == 0 && (calendarView.getShowWeeks())) {
                Label label = (Label) getChildren().get(i);
                label.setText(Integer.toString(calendar.get(Calendar.WEEK_OF_YEAR)));
            } else {
                Button control = (Button) getChildren().get(i);
                control.setTooltip(new Tooltip(dateFormat.format(currentDate)));
                control.setPrefWidth(150);
                control.setPrefHeight(100);
                control.setMaxWidth(Region.USE_PREF_SIZE);
                control.setMaxHeight(Region.USE_PREF_SIZE);
                final GridPane gridBtn = new GridPane();
                gridBtn.add(new Label(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH))), 2, 0);
                gridBtn.getColumnConstraints().add(colSmallCstr);
                gridBtn.getColumnConstraints().add(colLargeTxtEventCstr);
                gridBtn.getColumnConstraints().add(colSmallCstr);
                control.setGraphic(gridBtn);
                control.setBackground(Background.EMPTY);
                control.setBorder(btnBorder);

                boolean disabled = calendarView.getDisabledWeekdays().contains(calendar.get(Calendar.DAY_OF_WEEK));

                for (Date disabledDate : calendarView.getDisabledDates()) {
                    Calendar clone = (Calendar) calendar.clone();
                    clone.setTime(disabledDate);
                    if (calendar.get(Calendar.YEAR) == clone.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) == clone.get(Calendar.MONTH) && calendar.get(Calendar.DAY_OF_MONTH) == clone.get(Calendar.DAY_OF_MONTH)) {
                        disabled = true;
                    }
                }

                control.setDisable(disabled);

                control.getStyleClass().remove(CSS_CALENDAR_DAY_CURRENT_MONTH);
                control.getStyleClass().remove(CSS_CALENDAR_DAY_OTHER_MONTH);
                control.getStyleClass().remove(CSS_CALENDAR_TODAY);
                control.getStyleClass().remove(CSS_CALENDAR_SELECTED);

                if (calendar.get(Calendar.MONTH) == month) {
                    control.getStyleClass().add(CSS_CALENDAR_DAY_CURRENT_MONTH);
                } else {
                    control.getStyleClass().add(CSS_CALENDAR_DAY_OTHER_MONTH);
                }

                if (calendar.get(Calendar.YEAR) == todayYear && calendar.get(Calendar.MONTH) == todayMonth && calendar.get(Calendar.DATE) == todayDay) {
                    control.getStyleClass().add(CSS_CALENDAR_TODAY);
                }
                if (calendar.get(Calendar.YEAR) == selectedYear && calendar.get(Calendar.MONTH) == selectedMonth && calendar.get(Calendar.DATE) == selectedDay) {
                    control.getStyleClass().add(CSS_CALENDAR_SELECTED);
                }


                control.setUserData(calendar.getTime());
                calendar.add(Calendar.DATE, 1);
            }
        }

        // Restore original date
        calendar.setTime(calendarView.calendarDate.get());
    }

    /**
     * Updates the week names, when the locale changed.
     */
    private void updateWeekNames() {
        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(calendarView.localeProperty().get());
        String[] weekDays = dateFormatSymbols.getShortWeekdays();

        // Start with 1 instead of 0, since the first element in the array is empty.
        for (int i = 1; i < weekDays.length; i++) {
            // Get the first character only.
            
            // FIX submitted by Masayuki Ueki for Japanese language as they only have one character
            String shortWeekDay = "";
            if (weekDays[i].length() > 2) {
                shortWeekDay = weekDays[i].substring(0, 2);
            } else {
                shortWeekDay = weekDays[i];
            }

            // Shift the index according to the first day of week.
            int j = i - calendarView.getCalendar().getFirstDayOfWeek();
            if (j < 0) {
                j += weekDays.length - 1;
            }

            Label label = (Label) getChildren().get(j + (calendarView.getShowWeeks() ? 1 : 0));

            label.setText(shortWeekDay);
        }
        title.set(getDateFormat("MMMM yyyy").format(calendarView.getCalendar().getTime()));
    }
}
