package fr.sirs.ui;

import java.time.LocalDateTime;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class AlertItem {
    private final String title;
    private final LocalDateTime date;

    public AlertItem(String title, LocalDateTime date) {
        this.title = title;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
