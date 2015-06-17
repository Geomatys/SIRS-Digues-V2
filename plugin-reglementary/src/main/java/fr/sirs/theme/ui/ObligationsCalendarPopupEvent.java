package fr.sirs.theme.ui;

import fr.sirs.ui.calendar.CalendarEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

import java.awt.*;


/**
 * Popup affichée lorsque l'utilisateur clique sur un évènement de calendrier.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class ObligationsCalendarPopupEvent extends Popup {
    private static final String CSS_CALENDAR_EVENT_POPUP = "calendar-event-popup";
    private static final String CSS_CALENDAR_EVENT_POPUP_HEADER = "calendar-event-popup-header";
    private static final String CSS_CALENDAR_EVENT_POPUP_TITLE = "calendar-event-popup-title";
    private static final String CSS_CALENDAR_EVENT_POPUP_BUTTON = "calendar-event-popup-button";

    private static final Image ICON_CROSS = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TIMES, 16,
            Color.GRAY), null);
    private static final Image ICON_DELETE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TRASH_O, 16,
            FontAwesomeIcons.DEFAULT_COLOR), null);
    private static final Image ICON_REPORT = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CALENDAR_O, 16,
            FontAwesomeIcons.DEFAULT_COLOR), null);
    private static final Image ICON_ALERT = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_BELL, 16,
            FontAwesomeIcons.DEFAULT_COLOR), null);

    public ObligationsCalendarPopupEvent(final CalendarEvent calendarEvent) {
        super();

        // Main box containing the whole popup
        final VBox mainBox = new VBox();
        mainBox.getStyleClass().add(CSS_CALENDAR_EVENT_POPUP);

        final HBox header = new HBox();
        header.getStyleClass().add(CSS_CALENDAR_EVENT_POPUP_HEADER);
        final Label lblTitle = new Label(calendarEvent.getTitle());
        lblTitle.getStyleClass().add(CSS_CALENDAR_EVENT_POPUP_TITLE);
        header.getChildren().add(lblTitle);
        final Label emptylbl = new Label();
        emptylbl.setMaxWidth(Double.MAX_VALUE);
        header.getChildren().add(emptylbl);
        final ImageView imageView = new ImageView();
        imageView.setImage(ICON_CROSS);
        imageView.setOnMouseClicked(event -> hide());
        header.getChildren().add(imageView);
        HBox.setHgrow(emptylbl, Priority.ALWAYS);
        mainBox.getChildren().add(header);

        final Button buttonDelete = new Button();
        buttonDelete.setText("Supprimer");
        buttonDelete.setGraphic(new ImageView(ICON_DELETE));
        buttonDelete.setMaxWidth(Region.USE_PREF_SIZE);
        buttonDelete.getStyleClass().add(CSS_CALENDAR_EVENT_POPUP_BUTTON);
        mainBox.getChildren().add(buttonDelete);

        final Button buttonReport = new Button();
        buttonReport.setText("Reporter");
        buttonReport.setGraphic(new ImageView(ICON_REPORT));
        buttonReport.setMaxWidth(Region.USE_PREF_SIZE);
        buttonReport.getStyleClass().add(CSS_CALENDAR_EVENT_POPUP_BUTTON);
        mainBox.getChildren().add(buttonReport);

        final Button buttonAlert = new Button();
        buttonAlert.setText("Gérer l'alerte");
        buttonAlert.setGraphic(new ImageView(ICON_ALERT));
        buttonAlert.setMaxWidth(Region.USE_PREF_SIZE);
        buttonAlert.getStyleClass().add(CSS_CALENDAR_EVENT_POPUP_BUTTON);
        mainBox.getChildren().add(buttonAlert);

        getContent().add(mainBox);
    }
}
