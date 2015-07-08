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
     * Generates the calendar event.
     *
     * @param parent The source element responsible for this event.
     * @param date The specific date of this event.
     * @param title Title to display for this event.
     * @param image Icon to display for this event, might be {@code null}.
     */
    public CalendarEvent(final Element parent, final LocalDate date, final String title, final Image image) {
        this.parent = parent;
        this.date = date;
        this.title = title;
        this.image = image;
    }

    public Element getParent() {
        return parent;
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

        if (!title.equals(that.title)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + date.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CalendarEvent{" +
                "date=" + date +
                ", title='" + title + '\'' +
                '}';
    }
}
