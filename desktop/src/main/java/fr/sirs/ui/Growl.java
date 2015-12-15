package fr.sirs.ui;

import fr.sirs.FXMainFrame;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import javafx.util.Duration;


/**
 * Gestion de l'affichage d'une fenêtre d'information pendant quelques secondes.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class Growl {
    /**
     * CSS classes.
     */
    public static final String CSS_GROWL = "growl";
    public static final String CSS_GROWL_INFO = "growl-info";
    public static final String CSS_GROWL_WARNING = "growl-warning";
    public static final String CSS_GROWL_ERROR = "growl-error";

    /**
     * Type de fenêtre d'affichage.
     */
    public enum Type { INFO, WARNING, ERROR }

    /**
     * Type choisi pour cette fenêtre d'affichage.
     */
    private final Type type;

    /**
     * Texte à afficher.
     */
    private final String text;

    public Growl(final Type type, final String text) {
        this.type = type;
        this.text = text;
    }

    /**
     * Affiche la fenêtre d'information pendant 5 secondes.
     */
    public void showAndFade() {
        show(Duration.millis(5000));
    }

    /**
     * Affiche la fenêtre d'information durant le temps souhaité.
     *
     * @param duration Durée d'affichage de la fenêtre.
     */
    public void show(final Duration duration) {
        final Popup popup = new Popup();
        final Label textLabel = new Label(text);
        textLabel.setPrefWidth(200);
        textLabel.setMaxWidth(200);
        textLabel.setAlignment(Pos.CENTER);
        textLabel.setTextOverrun(OverrunStyle.CLIP);
        textLabel.setWrapText(true);

        final BorderPane content = new BorderPane(textLabel);
        content.getStylesheets().add(SIRS.CSS_PATH);
        content.getStyleClass().add(CSS_GROWL);
        switch (type) {
            case INFO: content.getStyleClass().add(CSS_GROWL_INFO); break;
            case WARNING: content.getStyleClass().add(CSS_GROWL_WARNING); break;
            case ERROR: content.getStyleClass().add(CSS_GROWL_ERROR); break;
            default: break;
        }
        popup.getContent().add(content);

        // Gestion de l'effet d'affichage et de disparition de la popup
        final KeyValue fadeOutBegin = new KeyValue(popup.opacityProperty(), 1.0);
        final KeyValue fadeOutEnd   = new KeyValue(popup.opacityProperty(), 0.0);
        final KeyFrame kfBegin = new KeyFrame(Duration.ZERO, fadeOutBegin);
        final KeyFrame kfEnd   = new KeyFrame(Duration.millis(500), fadeOutEnd);
        final Timeline timeline = new Timeline(kfBegin, kfEnd);
        timeline.setDelay(duration);
        timeline.setOnFinished(actionEvent -> Platform.runLater(() -> popup.hide()));

        // Placement de la popup par rapport à la fenêtre de l'application
        final FXMainFrame mainFrame = Injector.getSession().getFrame();
        final Point2D point = mainFrame.localToScreen(mainFrame.getWidth(), 20);
        popup.show(mainFrame.getScene().getWindow(), point.getX(), point.getY());
        popup.setX(popup.getX() - popup.getWidth() - 20);
        timeline.play();
    }
}
