package fr.sirs.ui.calendar;

import javafx.scene.image.ImageView;

import java.time.LocalDateTime;

/**
 * Event on the calendar to display.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class CalendarEvent {
    private final LocalDateTime date;
    private final String title;
    private final String type;
    private final ImageView image;

    public CalendarEvent(final LocalDateTime date, final String title, final String type) {
        this(date, title, type, null);
    }

    public CalendarEvent(final LocalDateTime date, final String title, final String type, final ImageView image) {
        this.date = date;
        this.title = title;
        this.type = type;
        this.image = image;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public ImageView getImage() {
        return image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CalendarEvent that = (CalendarEvent) o;

        if (!title.equals(that.title)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CalendarEvent{" +
                "date=" + date +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
