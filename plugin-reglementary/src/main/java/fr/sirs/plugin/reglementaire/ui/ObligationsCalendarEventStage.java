package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.EtapeObligationReglementaireRepository;
import fr.sirs.core.component.ObligationReglementaireRepository;
import fr.sirs.core.component.ObligationReglementaireRepository2;
import fr.sirs.core.component.PlanificationObligationReglementaireRepository;
import fr.sirs.core.component.RefEcheanceRappelObligationReglementaireRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.EtapeObligationReglementaire;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.PlanificationObligationReglementaire;
import fr.sirs.core.model.RefEcheanceRappelObligationReglementaire;
import fr.sirs.ui.calendar.CalendarEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;


/**
 * Popup affichée lorsque l'utilisateur clique sur un évènement de calendrier.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class ObligationsCalendarEventStage extends Stage {
    private static final String CSS_CALENDAR_EVENT_POPUP = "calendar-event-popup";
    private static final String CSS_CALENDAR_EVENT_POPUP_BUTTON = "calendar-event-popup-button";

    private static final Image ICON_DELETE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TRASH_O, 16,
            FontAwesomeIcons.DEFAULT_COLOR), null);
    private static final Image ICON_REPORT = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CALENDAR, 16,
            FontAwesomeIcons.DEFAULT_COLOR), null);
    private static final Image ICON_ALERT = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_BELL, 16,
            FontAwesomeIcons.DEFAULT_COLOR), null);

    /**
     * Prépare une popup pour afficher les choix possibles au clic sur un évènement du calendrier.
     *
     * @param calendarEvent Evènement du calendrier concerné
     * @param etapes Liste des étapes d'obligations.
     */
    public ObligationsCalendarEventStage(final CalendarEvent calendarEvent, final ObservableList<EtapeObligationReglementaire> etapes) {
        super();

        setTitle(calendarEvent.getTitle());
        // Main box containing the whole popup
        final VBox mainBox = new VBox();
        mainBox.getStyleClass().add(CSS_CALENDAR_EVENT_POPUP);

        final Button buttonDelete = new Button();
        buttonDelete.setText("Supprimer");
        buttonDelete.setGraphic(new ImageView(ICON_DELETE));
        buttonDelete.setMaxWidth(Double.MAX_VALUE);
        buttonDelete.setAlignment(Pos.CENTER_LEFT);
        buttonDelete.getStyleClass().add(CSS_CALENDAR_EVENT_POPUP_BUTTON);
        buttonDelete.setOnAction(event -> delete(calendarEvent, etapes));
        mainBox.getChildren().add(buttonDelete);

        final Button buttonReport = new Button();
        buttonReport.setText("Reporter");
        buttonReport.setAlignment(Pos.CENTER_LEFT);
        buttonReport.setGraphic(new ImageView(ICON_REPORT));
        buttonReport.setMaxWidth(Double.MAX_VALUE);
        buttonReport.getStyleClass().add(CSS_CALENDAR_EVENT_POPUP_BUTTON);
        buttonReport.setOnAction(event -> {
            if (!calendarEvent.isAlert()) {
                switchToDateStage(calendarEvent);
            } else {
                switchToEcheanceListStage(calendarEvent);
            }
        });
        mainBox.getChildren().add(buttonReport);

        if (!calendarEvent.isAlert()) {
            final Button buttonAlert = new Button();
            buttonAlert.setText("Gérer l'alerte");
            buttonAlert.setAlignment(Pos.CENTER_LEFT);
            buttonAlert.setGraphic(new ImageView(ICON_ALERT));
            buttonAlert.setMaxWidth(Double.MAX_VALUE);
            buttonAlert.getStyleClass().add(CSS_CALENDAR_EVENT_POPUP_BUTTON);
            buttonAlert.setOnAction(event -> switchToEcheanceListStage(calendarEvent));
            mainBox.getChildren().add(buttonAlert);
        }

        final Scene scene = new Scene(mainBox, 250, 100);
        scene.getStylesheets().add("/fr/sirs/plugin/reglementaire/ui/popup-calendar.css");
        setScene(scene);
    }

    /**
     * Supprime un évènement du calendrier ou une alerte de rappel.
     *
     * @param calendarEvent Evènement du calendrier concerné.
     * @param etapes Liste des obligations.
     */
    private void delete(final CalendarEvent calendarEvent, final ObservableList<EtapeObligationReglementaire> etapes) {
        final Alert alertDelConfirm = new Alert(Alert.AlertType.CONFIRMATION,"Confirmer la suppression de l'alerte ?",
                ButtonType.NO, ButtonType.YES);
        alertDelConfirm.setResizable(true);

        final ButtonType res = alertDelConfirm.showAndWait().get();
        if(ButtonType.YES != res) return;

        final EtapeObligationReglementaireRepository eorr = Injector.getBean(EtapeObligationReglementaireRepository.class);

        final Element parent = calendarEvent.getParent();
        if (parent instanceof EtapeObligationReglementaire) {
            final EtapeObligationReglementaire etape = (EtapeObligationReglementaire) parent;
            if (!calendarEvent.isAlert()) {
                // Une obligation a été fournie et ce n'est pas une alerte de rappel, donc on peut supprimer directement l'obligation.
                eorr.remove(etape);
                etapes.remove(etape);
            } else {
                // Une obligation a été fournie et c'est une alerte de rappel, donc on doit mettre à jour la date de
                // rappel de l'échéance car ce n'est plus valide.
                if (etape.getEcheanceId() != null) {
                    etape.setEcheanceId(null);
                    eorr.update(etape);
                }
            }
        }

        hide();
    }

    /**
     * Modifie la popup actuellement affichée pour montrer un {@linkplain DatePicker calendrier} permettant
     * de modifier la date de réalisation de l'obligation.
     *
     * @param event L'évènement du calendrier concerné.
     */
    private void switchToDateStage(final CalendarEvent event) {
        final VBox vbox = new VBox();
        vbox.getStyleClass().add(CSS_CALENDAR_EVENT_POPUP);
        vbox.setSpacing(15);

        final HBox hbox = new HBox();
        hbox.setMaxWidth(Double.MAX_VALUE);
        hbox.setMaxHeight(30);
        final Label lbl = new Label("Nouvelle date : ");
        lbl.setAlignment(Pos.CENTER_LEFT);
        lbl.setMaxHeight(Double.MAX_VALUE);
        hbox.getChildren().add(lbl);
        final DatePicker dp = new DatePicker();
        final EtapeObligationReglementaire etape = (EtapeObligationReglementaire) event.getParent();
        if (etape.getDateRealisation() != null) {
            dp.setValue(etape.getDateRealisation());
        } else {
            dp.setValue(etape.getDateEcheance());
        }
        hbox.getChildren().add(dp);
        vbox.getChildren().add(hbox);

        final BorderPane borderPane = new BorderPane();
        borderPane.setMaxWidth(Double.MAX_VALUE);
        final Button okButton = new Button("Valider");
        okButton.setPrefWidth(80);
        okButton.setMaxWidth(Region.USE_PREF_SIZE);
        okButton.setTextAlignment(TextAlignment.CENTER);
        okButton.setOnAction(e -> {
            if (etape.getDateRealisation() != null) {
                etape.setDateRealisation(dp.getValue());
            } else {
                etape.setDateEcheance(dp.getValue());
            }
            Injector.getBean(EtapeObligationReglementaireRepository.class).update(etape);
            hide();
        });
        borderPane.setCenter(okButton);
        vbox.getChildren().add(borderPane);

        final Scene newScene = new Scene(vbox, 350, 100);
        newScene.getStylesheets().add("/fr/sirs/plugin/reglementaire/ui/popup-calendar.css");
        setScene(newScene);
    }

    /**
     * Modifie la popup actuellement affichée pour montrer une liste déroulante des choix possibles de
     * rappels d'échéance pour l'obligation pointée par l'évènement.
     *
     * @param event L'évènement du calendrier concerné.
     */
    private void switchToEcheanceListStage(final CalendarEvent event) {
        final VBox vbox = new VBox();
        vbox.getStyleClass().add(CSS_CALENDAR_EVENT_POPUP);
        vbox.setSpacing(15);

        final HBox hbox = new HBox();
        hbox.setMaxWidth(Double.MAX_VALUE);
        hbox.setMaxHeight(30);
        final Label lbl = new Label("Echéance : ");
        lbl.setAlignment(Pos.CENTER_LEFT);
        lbl.setMaxHeight(Double.MAX_VALUE);
        hbox.getChildren().add(lbl);

        // Récupération de l'obligation
        if (!(event.getParent() instanceof EtapeObligationReglementaire)) {
            return;
        }
        final EtapeObligationReglementaire etape = (EtapeObligationReglementaire) event.getParent();

        // Génération de la liste déroulante des échéances possibles, avec l'ancienne valeur sélectionnée
        final RefEcheanceRappelObligationReglementaireRepository rerorr = Injector.getBean(RefEcheanceRappelObligationReglementaireRepository.class);
        final EtapeObligationReglementaireRepository eorr = Injector.getBean(EtapeObligationReglementaireRepository.class);
        final ComboBox<RefEcheanceRappelObligationReglementaire> comboEcheanceBox = new ComboBox<>();
        SIRS.initCombo(comboEcheanceBox, FXCollections.observableArrayList(rerorr.getAll()),
                etape.getEcheanceId() == null ? null : rerorr.get(etape.getEcheanceId()));
        hbox.getChildren().add(comboEcheanceBox);
        vbox.getChildren().add(hbox);

        final BorderPane borderPane = new BorderPane();
        borderPane.setMaxWidth(Double.MAX_VALUE);
        final Button okButton = new Button("Valider");
        okButton.setPrefWidth(80);
        okButton.setMaxWidth(Region.USE_PREF_SIZE);
        okButton.setTextAlignment(TextAlignment.CENTER);
        okButton.setOnAction(e -> {
            if (etape.getEcheanceId() != null) {
                etape.setEcheanceId(comboEcheanceBox.getValue().getId());
                eorr.update(etape);
                hide();
            }
        });
        borderPane.setCenter(okButton);
        vbox.getChildren().add(borderPane);

        final Scene newScene = new Scene(vbox, 350, 100);
        newScene.getStylesheets().add("/fr/sirs/plugin/reglementaire/ui/popup-calendar.css");
        setScene(newScene);
    }

}
