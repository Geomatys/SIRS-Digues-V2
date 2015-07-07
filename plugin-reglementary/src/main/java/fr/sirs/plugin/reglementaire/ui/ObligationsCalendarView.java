package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefTypeObligationReglementaire;
import fr.sirs.plugin.reglementaire.DocumentsTheme;
import fr.sirs.ui.calendar.CalendarEvent;
import fr.sirs.ui.calendar.CalendarView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

import java.time.LocalDateTime;

/**
 * @author Cédric Briançon (Geomatys)
 */
public final class ObligationsCalendarView extends CalendarView {
    private static final Image ICON_DOC = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_FILE, 16,
            FontAwesomeIcons.DEFAULT_COLOR), null);
    private static final Image ICON_WORK = new Image(DocumentsTheme.class.getResourceAsStream("images/roadworks.png"));

    private final ObjectProperty<ObservableList<ObligationReglementaire>> obligationsProperty;

    private final ChangeListener propChangeListener = (observable, oldValue, newValue) -> update();

    private final ListChangeListener<ObligationReglementaire> listChangeListener = c -> {
        update();
        while(c.next()) {
            for (final ObligationReglementaire obl : c.getAddedSubList()) {
                attachPropertyListener(obl);
            }

            for (final ObligationReglementaire obl : c.getRemoved()) {
                removePropertyListener(obl);
            }
        }
    };

    public ObligationsCalendarView(final ObjectProperty<ObservableList<ObligationReglementaire>> obligationsProperty) {
        super();
        this.obligationsProperty = obligationsProperty;
        obligationsProperty.addListener(new ChangeListener<ObservableList<ObligationReglementaire>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<ObligationReglementaire>> observable, ObservableList<ObligationReglementaire> oldList, ObservableList<ObligationReglementaire> newList) {
                update();
                if (newList != null) {
                    for (final ObligationReglementaire obl : newList) {
                        attachPropertyListener(obl);
                    }
                    newList.addListener(listChangeListener);
                }
                if (oldList != null) {
                    for (final ObligationReglementaire obl : oldList) {
                        removePropertyListener(obl);
                    }
                    oldList.removeListener(listChangeListener);
                }
            }
        });
    }

    private void attachPropertyListener(final ObligationReglementaire obligation) {
        obligation.dateEcheanceProperty().addListener(propChangeListener);
        obligation.dateRealisationProperty().addListener(propChangeListener);
        obligation.typeIdProperty().addListener(propChangeListener);
        obligation.systemeEndiguementIdProperty().addListener(propChangeListener);
    }

    private void removePropertyListener(final ObligationReglementaire obligation) {
        obligation.dateEcheanceProperty().removeListener(propChangeListener);
        obligation.dateRealisationProperty().removeListener(propChangeListener);
        obligation.typeIdProperty().removeListener(propChangeListener);
        obligation.systemeEndiguementIdProperty().removeListener(propChangeListener);
    }

    private void update() {
        getCalendarEvents().clear();

        final ObservableList<ObligationReglementaire> obligations = obligationsProperty.get();
        if (obligations != null && !obligations.isEmpty()) {
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
                    getCalendarEvents().add(new CalendarEvent(obligation, eventDate, sb.toString(), obligation.getTypeId(), image));
                }
            }
        }
    }

    /**
     * Affiche une fenêtre présentant les choix possibles pour cet évènement sur le calendrier.
     *
     * @param calendarEvent Evènement du calendrier concerné.
     * @param parent Noeud parent sur lequel la fenêtre sera accrochée.
     */
    @Override
    public void showCalendarPopupForEvent(final CalendarEvent calendarEvent, final Node parent) {
        final ObligationsCalendarPopupEvent popup = new ObligationsCalendarPopupEvent(calendarEvent, obligationsProperty.get());
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
