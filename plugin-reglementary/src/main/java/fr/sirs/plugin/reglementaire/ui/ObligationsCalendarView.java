package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.RefEcheanceRappelObligationReglementaireRepository;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefEcheanceRappelObligationReglementaire;
import fr.sirs.core.model.RefTypeObligationReglementaire;
import fr.sirs.plugin.reglementaire.DocumentsTheme;
import fr.sirs.ui.calendar.CalendarEvent;
import fr.sirs.ui.calendar.CalendarView;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

import java.time.LocalDate;

/**
 * Vue calendrier présentant les évènements construits à partir des obligations réglementaires.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class ObligationsCalendarView extends CalendarView {
    private static final Image ICON_DOC = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_FILE, 16,
            FontAwesomeIcons.DEFAULT_COLOR), null);
    private static final Image ICON_ALERT = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_BELL, 16,
            FontAwesomeIcons.DEFAULT_COLOR), null);
    private static final Image ICON_WORK = new Image(DocumentsTheme.class.getResourceAsStream("images/roadworks.png"));

    /**
     * Propriété pointant sur la liste des obligations réglementaires filtrées pour le calendrier.
     */
    private final ObservableList<ObligationReglementaire> obligations;

    /**
     * En cas de changements sur une propriété d'un objet, mets à jour la vue du calendrier.
     */
    private final ChangeListener propChangeListener = (observable, oldValue, newValue) -> update();

    /**
     * Ecouteur sur les changements de la liste des obligations. En cas d'ajout ou de retrait dans cette liste,
     * ajoute ou retire des écouteurs sur les futures changements de ces obligations, de manière à pouvoir mettre
     * à jour les vues les présentant.
     */
    private final ListChangeListener<ObligationReglementaire> listChangeListener = c -> {
        update();
        while(c.next()) {
            for (final ObligationReglementaire obl : c.getRemoved()) {
                removePropertyListener(obl);
            }

            for (final ObligationReglementaire obl : c.getAddedSubList()) {
                attachPropertyListener(obl);
            }
        }
    };

    /**
     * Vue calendrier pour les obligations réglementaires, permettant d'afficher les évènements.
     *
     * @param obligations propriété pointant sur la liste des obligations réglementaires filtrées pour le calendrier.
     */
    public ObligationsCalendarView(final ObservableList<ObligationReglementaire> obligations) {
        super();
        this.obligations = obligations;
        obligations.addListener(listChangeListener);
        update();
        for (final ObligationReglementaire obl : obligations) {
            attachPropertyListener(obl);
        }
    }

    /**
     * Attache un écouteur de changements sur l'obligation reglémentaire.
     *
     * @param obligation L'obligation réglementaire.
     */
    private void attachPropertyListener(final ObligationReglementaire obligation) {
        obligation.dateEcheanceProperty().addListener(propChangeListener);
        obligation.dateRealisationProperty().addListener(propChangeListener);
        obligation.typeIdProperty().addListener(propChangeListener);
        obligation.systemeEndiguementIdProperty().addListener(propChangeListener);
        obligation.echeanceIdProperty().addListener(propChangeListener);
    }

    /**
     * Retire un écouteur de changements sur l'obligation reglémentaire.
     *
     * @param obligation L'obligation réglementaire.
     */
    private void removePropertyListener(final ObligationReglementaire obligation) {
        obligation.dateEcheanceProperty().removeListener(propChangeListener);
        obligation.dateRealisationProperty().removeListener(propChangeListener);
        obligation.typeIdProperty().removeListener(propChangeListener);
        obligation.systemeEndiguementIdProperty().removeListener(propChangeListener);
        obligation.echeanceIdProperty().removeListener(propChangeListener);
    }

    /**
     * Met à jour les évènements sur le calendrier.
     */
    private void update() {
        getCalendarEvents().clear();

        if (obligations != null && !obligations.isEmpty()) {
            final RefEcheanceRappelObligationReglementaireRepository rerorr = Injector.getBean(RefEcheanceRappelObligationReglementaireRepository.class);

            for (final ObligationReglementaire obligation : obligations) {
                final LocalDate eventDate = obligation.getDateRealisation() != null ? obligation.getDateRealisation() :
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
                    getCalendarEvents().add(new CalendarEvent(obligation, eventDate, sb.toString(), image));

                    // Si l'obligation a une date de rappel d'échéance de configurée, on ajoute une alerte à cette date
                    if (obligation.getEcheanceId() != null) {
                        LocalDate firstDateRappel = LocalDate.from(eventDate);
                        final RefEcheanceRappelObligationReglementaire period = rerorr.get(obligation.getEcheanceId());
                        firstDateRappel = firstDateRappel.minusMonths(period.getNbMois());
                        sb.append(" - ").append(period.getLibelle());
                        getCalendarEvents().add(new CalendarEvent(obligation, true, firstDateRappel, sb.toString(), ICON_ALERT));
                    }
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
        final ObligationsCalendarEventStage stage = new ObligationsCalendarEventStage(calendarEvent, obligations);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setIconified(false);
        stage.setMaximized(false);
        stage.setResizable(false);
        stage.getIcons().add(SIRS.ICON);
        stage.initOwner(Injector.getSession().getFrame().getScene().getWindow());
        final Point2D popupPos = parent.localToScreen(20, 40);
        if (popupPos != null) {
            stage.sizeToScene();
            stage.setX(popupPos.getX());
            stage.setY(popupPos.getY());
        }
        stage.show();
    }
}
