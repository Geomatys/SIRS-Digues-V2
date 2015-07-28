package fr.sirs.ui;


import static fr.sirs.ui.AlertItem.AlertItemLevel.NORMAL;
import java.time.LocalDate;

/**
 *
 * @author Cédric Briançon (Geomatys)
 */
public class AlertItem {
    /**
     * Titre de l'alerte.
     */
    private final String title;

    /**
     * Date de l'alerte.
     */
    private final LocalDate date;

    /**
     * Objet déclenchant l'alerte.
     */
    private final Object parent;

    /**
     * Niveau de l'alerte.
     */
    private final AlertItemLevel level;

    /**
     * Niveaux possibles d'alerte.
     */
    public enum AlertItemLevel{HIGH, NORMAL, WARNING, INFORMATION}

    /**
     * Création d'une alerte.
     *
     * @param title  Titre de l'alerte
     * @param date   Date de l'alerte
     * @param parent Parent, objet déclenchant l'alerte
     * @param level  Niveau de l'alerte
     */
    public AlertItem(final String title, final LocalDate date, final Object parent, final AlertItemLevel level) {
        this.title = title;
        this.date = date;
        this.level = level;
        this.parent = parent;
    }

    /**
     * Création d'une alerte. Par défaut au niveau {@linkplain AlertItem.AlertItemLevel#NORMAL normal}.
     *
     * @param title  Titre de l'alerte
     * @param date   Date de l'alerte
     * @param parent Parent, objet déclenchant l'alerte
     */
    public AlertItem(final String title, final LocalDate date, final Object parent) {
        this(title, date, parent, NORMAL);
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

    public Object getParent() {
        return parent;
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
