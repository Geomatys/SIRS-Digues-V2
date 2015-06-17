package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.core.component.ObligationReglementaireRepository;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefTypeObligationReglementaire;
import fr.sirs.plugins.DocumentsTheme;
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
    private static final Image ICON_WORK = new Image(DocumentsTheme.class.getResourceAsStream("images/roadworks.png"));

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
                Image image = ICON_DOC;
                if (obligation.getTypeId() != null) {
                    final RefTypeObligationReglementaire oblType =
                            Injector.getSession().getRepositoryForClass(RefTypeObligationReglementaire.class).get(obligation.getTypeId());
                    if (oblType != null) {
                        final String oblTypeAbreg = oblType.getAbrege();
                        sb.append(oblTypeAbreg);
                        if ("TRA".equalsIgnoreCase(oblTypeAbreg)) {
                            image = ICON_WORK;
                        }
                    }
                }
                if (obligation.getSystemeEndiguementId() != null) {
                    final Preview previewSE = Injector.getSession().getPreviews().get(obligation.getSystemeEndiguementId());
                    if (previewSE != null) {
                        if (!sb.toString().isEmpty()) {
                            sb.append(" - ");
                        }
                        sb.append(previewSE.getLibelle());
                    }
                }
                calEvents.add(new CalendarEvent(eventDate, sb.toString(), obligation.getTypeId(), image));
            }
        }
        return calEvents;
    }
}
