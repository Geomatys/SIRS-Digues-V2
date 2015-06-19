package fr.sirs.plugins;

import fr.sirs.SIRS;
import fr.sirs.theme.ui.ObligationsCalendarView;
import fr.sirs.ui.calendar.CalendarView;
import fr.sirs.Injector;
import fr.sirs.core.component.ObligationReglementaireRepository;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import fr.sirs.theme.ui.PojoTable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

import java.util.Date;

/**
 * Panneau regroupant les fonctionnalités de suivi de documents.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class DocumentsTheme extends AbstractPluginsButtonTheme {
    private static final Image BUTTON_IMAGE = new Image(
            DocumentsTheme.class.getResourceAsStream("images/suivi_doc.png"));

    public DocumentsTheme() {
        super("Suivi des documents", "Suivi des documents", BUTTON_IMAGE);
    }

    /**
     * Panneau déployé au clic sur le bouton du menu correspondant à ce thème.
     */
    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();
        final TabPane tabPane = new TabPane();

        final Tab listTab = buildListTab();
        final Tab calendarTab = buildCalendarTab();
        tabPane.getTabs().add(listTab);
        tabPane.getTabs().add(calendarTab);

        borderPane.setCenter(tabPane);
        return borderPane;
    }

    /**
     * Génère l'onglet présentant la liste des obligations réglementaires.
     */
    private Tab buildListTab() {
        final Tab listTab = new Tab("Liste");
        listTab.setClosable(false);
        final PojoTable obligationsPojoTable = new PojoTable(Injector.getSession().getRepositoryForClass(ObligationReglementaire.class), "Liste des obligations réglementaires");
        listTab.setContent(obligationsPojoTable);
        return listTab;
    }

    /**
     * Génère l'onglet présentant le calendrier.
     */
    private Tab buildCalendarTab() {
        final Tab calendarTab = new Tab("Calendrier");
        calendarTab.setClosable(false);
        final CalendarView calendarView = new ObligationsCalendarView();
        calendarView.getStylesheets().add(SIRS.CSS_PATH_CALENDAR);
        calendarView.setShowTodayButton(false);
        calendarView.getCalendar().setTime(new Date());
        calendarTab.setContent(calendarView);
        return calendarTab;
    }
}
