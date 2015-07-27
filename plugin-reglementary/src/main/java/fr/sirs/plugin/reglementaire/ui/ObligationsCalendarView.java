package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.component.RappelObligationReglementaireRepository;
import fr.sirs.core.component.RefEcheanceRappelObligationReglementaireRepository;
import fr.sirs.core.component.RefFrequenceObligationReglementaireRepository;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RappelObligationReglementaire;
import fr.sirs.core.model.RefEcheanceRappelObligationReglementaire;
import fr.sirs.core.model.RefFrequenceObligationReglementaire;
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

import java.time.LocalDate;
import java.util.List;

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
    private final ObjectProperty<ObservableList<ObligationReglementaire>> obligationsProperty;

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
            for (final ObligationReglementaire obl : c.getAddedSubList()) {
                attachPropertyListener(obl);
            }

            for (final ObligationReglementaire obl : c.getRemoved()) {
                removePropertyListener(obl);
            }
        }
    };

    /**
     * Vue calendrier pour les obligations réglementaires, permettant d'afficher les évènements.
     *
     * @param obligationsProperty propriété pointant sur la liste des obligations réglementaires filtrées pour le calendrier.
     */
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

        final ObservableList<ObligationReglementaire> obligations = obligationsProperty.get();
        if (obligations != null && !obligations.isEmpty()) {
            final RappelObligationReglementaireRepository rorr = Injector.getBean(RappelObligationReglementaireRepository.class);
            final RefEcheanceRappelObligationReglementaireRepository rerorr = Injector.getBean(RefEcheanceRappelObligationReglementaireRepository.class);
            final RefFrequenceObligationReglementaireRepository rforr = Injector.getBean(RefFrequenceObligationReglementaireRepository.class);

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

                        // Gestion des alertes pour les rappels de cette obligation
                        final List<RappelObligationReglementaire> rappels = rorr.getByObligation(obligation);
                        if (rappels != null && !rappels.isEmpty()) {
                            for (final RappelObligationReglementaire rappel : rappels) {
                                LocalDate candidDate = LocalDate.from(firstDateRappel);
                                if (rappel.getFrequenceId() != null) {
                                    final RefFrequenceObligationReglementaire freq = rforr.get(rappel.getFrequenceId());
                                    // Génère des alertes pour les rappels sur les 10 ans à venir
                                    while (candidDate.getYear() - firstDateRappel.getYear() < 10) {
                                        if (candidDate.compareTo(firstDateRappel) != 0) {
                                            getCalendarEvents().add(new CalendarEvent(rappel, true, candidDate, sb.toString(), ICON_ALERT));
                                        }
                                        candidDate = candidDate.plusMonths(freq.getNbMois());
                                    }
                                }
                            }
                        }
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
        final ObligationsCalendarEventStage stage = new ObligationsCalendarEventStage(calendarEvent, obligationsProperty.get());
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
