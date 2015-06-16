package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.core.component.ObligationReglementaireRepository;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.ui.calendar.CalendarEvent;
import fr.sirs.ui.calendar.CalendarView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Cédric Briançon (Geomatys)
 */
public final class ObligationsCalendarView extends CalendarView {
    private static final Image ICON_DOC = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_FILE, 16,
            FontAwesomeIcons.DEFAULT_COLOR), null);
    // TODO: ajouter l'icone pour les travaux quand ce type d'obligations pourra être choisi.
    //private static final ImageView ICON_WORK  = new ImageView(new Image(""));

    public ObligationsCalendarView() {
        super();
    }

    /**
     * Retourne la liste des évènements de calendriers typés Obligations.
     */
    @Override
    public ObservableList<CalendarEvent> getCalendarEvents() {
        final ObservableList<CalendarEvent> calEvents = FXCollections.observableArrayList();

        final ObligationReglementaireRepository orr = new ObligationReglementaireRepository(Injector.getSession().getConnector());
        final List<ObligationReglementaire> obligations = orr.getAll();
        for (final ObligationReglementaire obligation : obligations) {
            final LocalDateTime eventDate = obligation.getDateRealisation() != null ? obligation.getDateRealisation() :
                    obligation.getDateEcheance();
            if (eventDate != null) {
                final StringBuilder sb = new StringBuilder();
                if (obligation.getTypeId() != null) {
                    sb.append(obligation.getTypeId());
                }
                if (obligation.getSystemeEndiguementId() != null) {
                    sb.append(" - ").append(obligation.getSystemeEndiguementId());
                }
                calEvents.add(new CalendarEvent(eventDate, sb.toString(), obligation.getTypeId(), ICON_DOC));
            }
        }
        return calEvents;
    }
}
