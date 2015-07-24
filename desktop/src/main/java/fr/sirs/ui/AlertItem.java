package fr.sirs.ui;


import static fr.sirs.ui.AlertItem.AlertItemLevel.NORMAL;
import java.time.LocalDate;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class AlertItem {
    private final String title;
    private final LocalDate date;
    private final AlertItemLevel level;
    
    public enum AlertItemLevel{HIGHT, NORMAL, WARNING, INFORMATION}

    public AlertItem(String title, LocalDate date, AlertItemLevel level) {
        this.title = title;
        this.date = date;
        this.level = level;
    }
    
    public AlertItem(String title, LocalDate date) {
        this(title, date, NORMAL);
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDate() {
        return date;
    }
    
    public AlertItemLevel getLevel(){
        return level;
    }
}
