package fr.sirs.ui;


import java.time.LocalDate;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class AlertItem {
    private final String title;
    private final LocalDate date;

    public AlertItem(String title, LocalDate date) {
        this.title = title;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDate() {
        return date;
    }
}
