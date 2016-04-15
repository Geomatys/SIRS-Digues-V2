/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.ui.calendar;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.GridPane;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstract base class for the {@link MonthView}, {@link YearView} and {@link DecadesView}.
 *
 * @author Christian Schudt
 */
abstract class DatePane extends GridPane {

    /**
     * Sets basic stuff
     *
     * @param calendarView The calendar view.
     */
    protected DatePane(final CalendarView calendarView) {
        this.calendarView = calendarView;

        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        // When the date changed, update the days.
        calendarView.calendarDate.addListener(observable -> {
            updateContent();
        });

        // Every time the calendar changed, rebuild the pane and update the content.
        calendarView.calendarProperty().addListener(observable -> {
            getChildren().clear();
            buildContent();
            updateContent();
        });

        buildContent();
        updateContent();
    }

    /**
     * The calendar view.
     */
    protected CalendarView calendarView;

    /**
     * This is the date, this pane operates on.
     *
     * @param date The date.
     */
    protected void setDate(Date date) {
        calendarView.getCalendar().setTime(date);
        updateContent();
        // Restore
        calendarView.getCalendar().setTime(calendarView.calendarDate.get());
    }

    /**
     * Builds the content.
     */
    protected abstract void buildContent();

    /**
     * Updates the content.
     */
    protected abstract void updateContent();

    protected StringProperty title = new SimpleStringProperty();

    /**
     * The title property which is defined by the pane.
     *
     * @return The property.
     */
    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    /**
     * Gets the date format, associated with the current calendar.
     *
     * @param format The date format as String.
     * @return The date format.
     */
    protected DateFormat getDateFormat(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format, calendarView.localeProperty().get());
        dateFormat.setCalendar(calendarView.getCalendar());
        return dateFormat;
    }
}
