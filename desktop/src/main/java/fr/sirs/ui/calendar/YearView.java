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

import javafx.scene.control.Button;
import javafx.scene.layout.Priority;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * The year view shows the months.
 *
 * @author Christian Schudt
 */
final class YearView extends DatePane {

    private static final String CSS_CALENDAR_YEAR_VIEW = "calendar-year-view";
    private static final String CSS_CALENDAR_MONTH_BUTTON = "calendar-month-button";


    public YearView(final CalendarView calendarView) {
        super(calendarView);

        getStyleClass().add(CSS_CALENDAR_YEAR_VIEW);

        // When the locale changes, update the contents (month names).
        calendarView.localeProperty().addListener(observable -> {
            updateContent();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildContent() {

        // Get the number of months. I read, there are some lunar calendars, with more than 12 months.
        int numberOfMonths = calendarView.getCalendar().getMaximum(Calendar.MONTH) + 1;

        int numberOfColumns = 3;

        for (int i = 0; i < numberOfMonths; i++) {
            final int j = i;
            Button button = new Button();
            button.getStyleClass().add(CSS_CALENDAR_MONTH_BUTTON);

            // Make the button stretch.
            button.setMaxWidth(Double.MAX_VALUE);
            button.setMaxHeight(Double.MAX_VALUE);
            setVgrow(button, Priority.ALWAYS);
            setHgrow(button, Priority.ALWAYS);

            button.setOnAction(actionEvent -> {
                if (calendarView.currentlyViewing.get() == Calendar.YEAR) {
                    calendarView.getCalendar().set(Calendar.MONTH, j);
                    calendarView.currentlyViewing.set(Calendar.MONTH);
                    calendarView.calendarDate.set(calendarView.getCalendar().getTime());
                }
            });
            int rowIndex = i % numberOfColumns;
            int colIndex = (i - rowIndex) / numberOfColumns;
            add(button, rowIndex, colIndex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateContent() {
        DateFormatSymbols symbols = new DateFormatSymbols(calendarView.localeProperty().get());
        String[] monthNames = symbols.getShortMonths();
        for (int i = 1; i < monthNames.length; i++) {
            Button button = (Button) getChildren().get(i - 1);
            button.setText(monthNames[i - 1]);
        }
        title.set(getDateFormat("yyyy").format(calendarView.getCalendar().getTime()));
    }
}
