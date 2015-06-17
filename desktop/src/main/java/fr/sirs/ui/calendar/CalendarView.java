package fr.sirs.ui.calendar;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A calendar control
 *
 * @author Christian Schudt
 */
public class CalendarView extends VBox {

    private static final String CSS_CALENDAR_FOOTER = "calendar-footer";
    private static final String CSS_CALENDAR = "calendar";
    private static final String CSS_CALENDAR_TODAY_BUTTON = "calendar-today-button";

    /**
     * Initializes a calendar with the default locale.
     */
    public CalendarView() {
        this(Locale.getDefault());
    }

    /**
     * Initializes a calendar with the given locale.
     * E.g. if the locale is en-US, the calendar starts the days on Sunday.
     * If it is de-DE the calendar starts the days on Monday.
     * <p/>
     * Note that the Java implementation only knows {@link java.util.GregorianCalendar} and sun.util.BuddhistCalendar.
     *
     * @param locale The locale.
     */
    public CalendarView(final Locale locale) {
        this(locale, Calendar.getInstance(locale));

        // When the locale changes, also change the calendar.
        this.locale.addListener(observable -> {
            calendar.set(Calendar.getInstance(localeProperty().get()));
        });
    }

    /**
     * Initializes the control with the given locale and the given calendar.
     * <p/>
     * This way, you can pass a custom calendar (e.g. you could implement the Hijri Calendar for the arabic world).
     * Or you can use an American style calendar (starting with Sunday as first day of the week)
     * together with another language.
     * <p/>
     * The locale determines the date format.
     *
     * @param locale   The locale.
     * @param calendar The calendar
     */
    public CalendarView(final Locale locale, final Calendar calendar) {

        this.locale.set(locale);
        this.calendar.set(calendar);

        getStyleClass().add(CSS_CALENDAR);

        setMaxWidth(Control.USE_PREF_SIZE);

        currentlyViewing.set(Calendar.MONTH);

        calendarDate.addListener(observable -> {
            calendar.setTime(calendarDate.get());
        });
        this.calendarDate.set(new Date());
        currentDate.addListener(observable -> {
            Date date = new Date();
            if (currentDate.get() != null) {
                date = currentDate.get();
            }
            calendarDate.set(date);
        });
        MainStackPane mainStackPane = new MainStackPane(this);
        //VBox.setVgrow(mainStackPane, Priority.ALWAYS);
        mainNavigationPane = new MainNavigationPane(this);

        todayButtonBox = new HBox();
        todayButtonBox.getStyleClass().add(CSS_CALENDAR_FOOTER);

        Button todayButton = new Button();
        todayButton.textProperty().bind(todayButtonText);
        todayButton.getStyleClass().add(CSS_CALENDAR_TODAY_BUTTON);
        todayButton.setOnAction(actionEvent -> {
            Calendar calendar1 = calendarProperty().get();
            calendar1.setTime(new Date());
            calendar1.set(Calendar.HOUR_OF_DAY, 0);
            calendar1.set(Calendar.MINUTE, 0);
            calendar1.set(Calendar.SECOND, 0);
            calendar1.set(Calendar.MILLISECOND, 0);
            selectedDate.set(calendar1.getTime());
        });
        todayButtonBox.setAlignment(Pos.CENTER);
        todayButtonBox.getChildren().add(todayButton);

        getChildren().addAll(mainNavigationPane, mainStackPane);

        showTodayButton.addListener(observable -> {
            if (showTodayButton.get()) {
                getChildren().add(todayButtonBox);
            } else {
                getChildren().remove(todayButtonBox);
            }
        });
        showTodayButton.set(true);

    }

    private HBox todayButtonBox;


    /**
     * Gets or sets the locale.
     *
     * @return The property.
     */
    public ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    private ObjectProperty<Locale> locale = new SimpleObjectProperty<Locale>();

    public Locale getLocale() {
        return locale.get();
    }

    public void setLocale(Locale locale) {
        this.locale.set(locale);
    }


    /**
     * Gets or sets the calendar.
     *
     * @return The property.
     */
    public ObjectProperty<Calendar> calendarProperty() {
        return calendar;
    }

    private ObjectProperty<Calendar> calendar = new SimpleObjectProperty<Calendar>();

    public Calendar getCalendar() {
        return calendar.get();
    }

    public void setCalendar(Calendar calendar) {
        this.calendar.set(calendar);
    }


    /**
     * Gets the list of disabled week days.
     * E.g. if you add <code>Calendar.WEDNESDAY</code>, Wednesday will be disabled.
     *
     * @return The list.
     */
    public ObservableList<Integer> getDisabledWeekdays() {
        return disabledWeekdays;
    }

    private ObservableList<Integer> disabledWeekdays = FXCollections.observableArrayList();


    /**
     * Gets the list of disabled dates.
     * You can add specific date, in order to disable them.
     *
     * @return The list.
     */
    public ObservableList<Date> getDisabledDates() {
        return disabledDates;
    }

    private ObservableList<Date> disabledDates = FXCollections.observableArrayList();


    /**
     * Gets the selected date.
     *
     * @return The property.
     */
    public ReadOnlyObjectProperty<Date> selectedDateProperty() {
        return selectedDate;
    }

    private ObjectProperty<Date> currentDate = new SimpleObjectProperty<Date>();

    public ObjectProperty<Date> currentDateProperty() {
        return currentDate;
    }


    /**
     * Indicates, whether the today button should be shown.
     *
     * @return The property.
     */
    public BooleanProperty showTodayButtonProperty() {
        return showTodayButton;
    }

    private BooleanProperty showTodayButton = new SimpleBooleanProperty();

    public boolean getShowTodayButton() {
        return showTodayButton.get();
    }

    public void setShowTodayButton(boolean showTodayButton) {
        this.showTodayButton.set(showTodayButton);
    }

    /**
     * The text of the today button
     *
     * @return The property.
     */
    public StringProperty todayButtonTextProperty() {
        return todayButtonText;
    }

    private StringProperty todayButtonText = new SimpleStringProperty("Today");

    public String getTodayButtonText() {
        return todayButtonText.get();
    }

    public void setTodayButtonText(String todayButtonText) {
        this.todayButtonText.set(todayButtonText);
    }


    /**
     * Indicates, whether the week numbers are shown.
     *
     * @return The property.
     */
    public BooleanProperty showWeeksProperty() {
        return showWeeks;
    }

    private BooleanProperty showWeeks = new SimpleBooleanProperty(false);

    public boolean getShowWeeks() {
        return showWeeks.get();
    }

    public void setShowWeeks(boolean showWeeks) {
        this.showWeeks.set(showWeeks);
    }

    /**
     * Return an empty list for calendar events. Subclasses should override this method and return
     * the wished list of events.
     *
     * @return By default an empty list.
     */
    public ObservableList<CalendarEvent> getCalendarEvents() {
        return FXCollections.observableArrayList();
    }

    /**
     * Get all calendar events defined for the choosen date.
     *
     * @see #getCalendarEvents()
     * @param calendar Calendar date.
     * @param events All available events.
     * @return Events list for this date.
     */
    public final ObservableList<CalendarEvent> getCalendarEventsForCalendarDate(final Calendar calendar,
                                                                                final ObservableList<CalendarEvent> events) {
        final ObservableList<CalendarEvent> finalEvents = FXCollections.observableArrayList();
        for (final CalendarEvent event : events) {
            final LocalDateTime d = event.getDate();
            if (d != null && d.getDayOfMonth() == calendar.get(Calendar.DAY_OF_MONTH) && d.getMonthValue() == calendar.get(Calendar.MONTH)+1 &&
                    d.getYear() == calendar.get(Calendar.YEAR)) {
                finalEvents.add(event);
            }
        }
        return finalEvents;
    }

    /**
     * Show actions popup when clicking on a calendar event. By default do nothing.
     * Subclasses should override this method to do something.
     *
     * @param calendarEvent Calendar event on which user has clicked. Should not be {@code null}.
     * @param parent Parent node on which to attach the popup. Should not be {@code null}.
     */
    public void showCalendarPopupForEvent(final CalendarEvent calendarEvent, final Node parent) {}

    /**
     * Package internal properties.
     */
    MainNavigationPane mainNavigationPane;
    /**
     * Counts the current transitions. As long as an animation is going, the panels should not move left and right.
     */
    IntegerProperty ongoingTransitions = new SimpleIntegerProperty(0);
    ObjectProperty<Date> selectedDate = new SimpleObjectProperty<>();
    ObjectProperty<Date> calendarDate = new SimpleObjectProperty<>();
    IntegerProperty currentlyViewing = new SimpleIntegerProperty();
    StringProperty title = new SimpleStringProperty();
}
