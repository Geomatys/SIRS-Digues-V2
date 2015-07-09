package fr.sirs.plugin.reglementaire;

import fr.sirs.SIRS;
import fr.sirs.plugin.reglementaire.ui.ObligationsCalendarView;
import fr.sirs.plugin.reglementaire.ui.ObligationsPojoTable;
import fr.sirs.ui.calendar.CalendarView;
import fr.sirs.Injector;
import fr.sirs.core.component.ObligationReglementaireRepository;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.SimpleFXEditMode;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

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

        // Onglet liste des obligations réglementaires
        final Tab listTab = new Tab("Liste");
        listTab.setClosable(false);
        // Gestion du bouton consultation / édition pour la pojo table
        final Separator separator = new Separator();
        separator.setVisible(false);
        final SimpleFXEditMode editMode = new SimpleFXEditMode();
        final HBox topPane = new HBox(separator, editMode);
        HBox.setHgrow(separator, Priority.ALWAYS);
        final ObligationsPojoTable obligationsPojoTable = new ObligationsPojoTable(Injector.getSession()
                .getRepositoryForClass(ObligationReglementaire.class), tabPane);
        obligationsPojoTable.editableProperty().bind(editMode.editionState());
        listTab.setContent(new BorderPane(obligationsPojoTable, topPane, null, null, null));

        // Onglet calendrier des obligations reglémentaires
        final Tab calendarTab = new Tab("Calendrier");
        calendarTab.setClosable(false);
        final CalendarView calendarView = new ObligationsCalendarView(obligationsPojoTable.getUiTable().itemsProperty());
        calendarView.getStylesheets().add(SIRS.CSS_PATH_CALENDAR);
        calendarView.setShowTodayButton(false);
        calendarView.getCalendar().setTime(new Date());
        calendarTab.setContent(calendarView);

        // Ajout des onglets
        tabPane.getTabs().add(listTab);
        tabPane.getTabs().add(calendarTab);
        borderPane.setCenter(tabPane);
        return borderPane;
    }
}
