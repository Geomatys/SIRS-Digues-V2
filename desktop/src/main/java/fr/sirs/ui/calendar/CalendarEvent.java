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

import fr.sirs.core.model.Element;
import javafx.scene.image.Image;
import java.time.LocalDate;

/**
 * Event on the calendar to display.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class CalendarEvent {
    /**
     * Parent element responsible for this event.
     */
    private final Element parent;

    /**
     * Defines if this calendar event is an alert or just an event. By default {@code false} means an event.
     */
    private final boolean isAlert;

    /**
     * Event date.
     */
    private final LocalDate date;

    /**
     * Event title.
     */
    private final String title;

    /**
     * Event image, might be {@code null}.
     */
    private final Image image;

    /**
     * Generates a calendar event.
     *
     * @see #CalendarEvent(Element, boolean, LocalDate, String, Image)
     */
    public CalendarEvent(final Element parent, final LocalDate date, final String title, final Image image) {
        this(parent, false, date, title, image);
    }

    /**
     * Generates the calendar event, which may be an alert.
     *
     * @param parent The source element responsible for this event.
     * @param isAlert {@code True} to define it is an alert, {@code false} for just an event.
     * @param date The specific date of this event.
     * @param title Title to display for this event.
     * @param image Icon to display for this event, might be {@code null}.
     */
    public CalendarEvent(final Element parent, final boolean isAlert, final LocalDate date, final String title, final Image image) {
        this.parent = parent;
        this.isAlert = isAlert;
        this.date = date;
        this.title = title;
        this.image = image;
    }

    public Element getParent() {
        return parent;
    }

    public boolean isAlert() {
        return isAlert;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public Image getImage() {
        return image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CalendarEvent that = (CalendarEvent) o;

        if (isAlert != that.isAlert) return false;
        if (!date.equals(that.date)) return false;
        return title.equals(that.title);

    }

    @Override
    public int hashCode() {
        int result = (isAlert ? 1 : 0);
        result = 31 * result + date.hashCode();
        result = 31 * result + title.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CalendarEvent{" +
                "parent=" + parent +
                ", isAlert=" + isAlert +
                ", date=" + date +
                ", title='" + title + '\'' +
                ", image=" + image +
                '}';
    }
}
