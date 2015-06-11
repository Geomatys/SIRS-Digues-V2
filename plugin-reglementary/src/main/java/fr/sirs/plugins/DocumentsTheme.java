package fr.sirs.plugins;

import fr.sirs.Injector;
import fr.sirs.core.component.ObligationReglementaireRepository;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import fr.sirs.theme.ui.ObligationsPojoTable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

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

    private Tab buildListTab() {
        final Tab listTab = new Tab("Liste");
        final ObligationReglementaireRepository orr = new ObligationReglementaireRepository(Injector.getSession().getConnector());
        final ObligationsPojoTable obligationsPojoTable = new ObligationsPojoTable(orr);
        listTab.setContent(obligationsPojoTable);
        return listTab;
    }

    private Tab buildCalendarTab() {
        final Tab calendarTab = new Tab("Calendrier");
        return calendarTab;
    }
}
