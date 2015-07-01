package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefTypeObligationReglementaire;
import fr.sirs.plugin.reglementaire.DocumentsTheme;
import fr.sirs.ui.calendar.CalendarEvent;
import fr.sirs.ui.calendar.CalendarView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.stage.Modality;
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

        final AbstractSIRSRepository<ObligationReglementaire> orr = Injector.getSession().getRepositoryForClass(ObligationReglementaire.class);
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

    /**
     * Affiche une fenêtre présentant les choix possibles pour cet évènement sur le calendrier.
     *
     * @param calendarEvent Evènement du calendrier concerné.
     * @param parent Noeud parent sur lequel la fenêtre sera accrochée.
     */
    @Override
    public void showCalendarPopupForEvent(final CalendarEvent calendarEvent, final Node parent) {
        final ObligationsCalendarPopupEvent popup = new ObligationsCalendarPopupEvent(calendarEvent);
        popup.initModality(Modality.NONE);
        popup.setIconified(false);
        popup.setMaximized(false);
        popup.setResizable(false);
        popup.focusedProperty().addListener((obs,old,newVal) -> {
            if (popup.isShowing() && !newVal) {
                // Close the popup if the focus is lost
                popup.hide();
            }
        });
        popup.getIcons().add(SIRS.ICON);
        final Point2D popupPos = parent.localToScreen(20, 40);
        if (popupPos != null) {
            popup.sizeToScene();
            popup.setX(popupPos.getX());
            popup.setY(popupPos.getY());
        }
        popup.show();
    }
}
