package fr.sirs.plugin.reglementaire;

import fr.sirs.SIRS;
import fr.sirs.core.component.DocumentChangeEmiter;
import fr.sirs.core.component.DocumentListener;
import fr.sirs.core.component.EtapeObligationReglementaireRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.EtapeObligationReglementaire;
import fr.sirs.plugin.reglementaire.ui.EtapesPojoTable;
import fr.sirs.plugin.reglementaire.ui.ObligationsCalendarView;
import fr.sirs.plugin.reglementaire.ui.ObligationsPojoTable;
import fr.sirs.ui.calendar.CalendarView;
import fr.sirs.Injector;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import fr.sirs.util.SimpleFXEditMode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
        final Tab obligationsTab = new Tab("Obligations réglementaires");
        obligationsTab.setClosable(false);
        // Gestion du bouton consultation / édition pour la pojo table
        final Separator separatorOR = new Separator();
        separatorOR.setVisible(false);
        final SimpleFXEditMode editModeOR = new SimpleFXEditMode();
        final HBox topORPane = new HBox(separatorOR, editModeOR);
        HBox.setHgrow(separatorOR, Priority.ALWAYS);
        final ObligationsPojoTable obligationsPojoTable = new ObligationsPojoTable();
        obligationsPojoTable.editableProperty().bind(editModeOR.editionState());
        obligationsTab.setContent(new BorderPane(obligationsPojoTable, topORPane, null, null, null));

        // Onglet liste des étapes obligations réglementaires
        final Tab etapesTab = new Tab("Etapes d'obligations réglementaires");
        etapesTab.setClosable(false);
        // Gestion du bouton consultation / édition pour la pojo table
        final Separator separatorEtape = new Separator();
        separatorEtape.setVisible(false);
        final SimpleFXEditMode editEtapeMode = new SimpleFXEditMode();
        final HBox topEtapePane = new HBox(separatorEtape, editEtapeMode);
        HBox.setHgrow(separatorEtape, Priority.ALWAYS);
        final EtapeObligationReglementaireRepository eorrRepo = Injector.getBean(EtapeObligationReglementaireRepository.class);
        final ObservableList<EtapeObligationReglementaire> allEtapes = FXCollections.observableList(eorrRepo.getAll());
        // Ajoute un listener sur tous les ajouts/suppression d'étapes d'obligations pour mettre à jour la liste et donc la table.
        final EtapeDocumentListener etapeListener = new EtapeDocumentListener(allEtapes);
        Injector.getBean(DocumentChangeEmiter.class).addListener(etapeListener);
        final EtapesPojoTable etapesPojoTable = new EtapesPojoTable(tabPane);
        etapesPojoTable.setTableItems(() -> (ObservableList) allEtapes);
        etapesPojoTable.editableProperty().bind(editEtapeMode.editionState());
        etapesTab.setContent(new BorderPane(etapesPojoTable, topEtapePane, null, null, null));

        // Onglet calendrier des obligations reglémentaires
        final Tab calendarTab = new Tab("Calendrier");
        calendarTab.setClosable(false);
        final CalendarView calendarView = new ObligationsCalendarView(allEtapes);
        calendarView.getStylesheets().add(SIRS.CSS_PATH_CALENDAR);
        calendarView.setShowTodayButton(false);
        calendarView.getCalendar().setTime(new Date());
        calendarTab.setContent(calendarView);

        // Ajout des onglets
        tabPane.getTabs().add(obligationsTab);
        tabPane.getTabs().add(etapesTab);
        tabPane.getTabs().add(calendarTab);
        borderPane.setCenter(tabPane);
        return borderPane;
    }

    /**
     * Ecouteur d'ajouts et suppressions d'étapes d'obligations réglementaires sur la base, pour mettre à jour les vues
     * montrant ces objets.
     */
    private class EtapeDocumentListener implements DocumentListener {
        private final ObservableList<EtapeObligationReglementaire> list;

        public EtapeDocumentListener(final ObservableList<EtapeObligationReglementaire> list) {
            this.list = list;
        }

        /**
         * A la création de documents, mise à jour de la liste en conséquence.
         *
         * @param added Nouveaux éléments à ajouter.
         */
        @Override
        public void documentCreated(Map<Class, List<Element>> added) {
            final List addedObl = added.get(EtapeObligationReglementaire.class);
            if (addedObl == null || addedObl.isEmpty()) {
                return;
            }
            // On enlève les éléments déjà présents dans la liste de base, pour ne garder que les nouveaux
            // et ne pas les ajouter plusieurs fois dans la liste.
            addedObl.removeAll(list);
            final Runnable addRun = () -> list.addAll(addedObl);
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(addRun);
            } else {
                addRun.run();
            }
        }

        @Override
        public void documentChanged(Map<Class, List<Element>> changed) {
        }

        /**
         * Suppression des objets dans la liste.
         *
         * @param deletedObject Liste d'éléments à supprimer de la liste.
         */
        @Override
        public void documentDeleted(Map<Class, List<Element>> deletedObject) {
            final List deletedObj = deletedObject.get(EtapeObligationReglementaire.class);
            if (deletedObj == null || deletedObj.isEmpty()) {
                return;
            }
            final Runnable delRun = () -> list.removeAll(deletedObj);
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(delRun);
            } else {
                delRun.run();
            }
        }
    }
}
