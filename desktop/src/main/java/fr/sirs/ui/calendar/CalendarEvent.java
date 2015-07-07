package fr.sirs.ui.calendar;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.Identifiable;
import javafx.scene.image.Image;
import java.time.LocalDate;

/**
 * Event on the calendar to display.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class CalendarEvent {
    private final Element parent;
    private final LocalDate date;
    private final String title;
    private final String type;
    private final Image image;

    public CalendarEvent(final Element parent, final LocalDate date, final String title, final String type) {
        this(parent, date, title, type, null);
    }

    public CalendarEvent(final Element parent, final LocalDate date, final String title, final String type, final Image image) {
        this.parent = parent;
        this.date = date;
        this.title = title;
        this.type = type;
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

    public String getType() {
        return type;
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
