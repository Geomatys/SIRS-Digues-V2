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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlertItem alertItem = (AlertItem) o;

        if (title != null ? !title.equals(alertItem.title) : alertItem.title != null) return false;
        if (date != null ? !date.equals(alertItem.date) : alertItem.date != null) return false;
        return level == alertItem.level;

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (level != null ? level.hashCode() : 0);
        return result;
    }
}
