package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.core.component.ObligationReglementaireRepository;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.ui.calendar.CalendarEvent;
import fr.sirs.ui.calendar.CalendarView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

import java.util.List;

/**
 * @author Cédric Briançon (Geomatys)
 */
public final class ObligationsCalendarView extends CalendarView {
    private static final ImageView ICON_DOC  = new ImageView(SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_FILE, 16,
            FontAwesomeIcons.DEFAULT_COLOR), null));

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
            calEvents.add(new CalendarEvent(obligation.getDateRealisation(), obligation.getDesignation(),
                    "doc", ICON_DOC));
        }
        return calEvents;
    }
}
