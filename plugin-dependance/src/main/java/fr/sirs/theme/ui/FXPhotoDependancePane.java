
package fr.sirs.theme.ui;

import fr.sirs.core.model.PhotoDependance;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXPhotoDependancePane extends FXPhotoDependancePaneStub {

    // Propriétés de Positionable
    @FXML protected FXPositionablePane uiPositionable;

    @FXML protected ScrollPane ui_scroll_pane;
    @FXML protected StackPane ui_photo_stack;
    @FXML protected HBox ui_hbox_container;

    /**
     * Constructor. Initialize part of the UI which will not require update when element edited change.
     */
    private FXPhotoDependancePane() {
        super();
        ui_scroll_pane.setMinWidth(USE_PREF_SIZE);
        ui_scroll_pane.setPrefWidth(USE_COMPUTED_SIZE);
        ui_scroll_pane.setBorder(Border.EMPTY);

        ui_hbox_container.setFillHeight(true);

        ui_photo.setPreserveRatio(true);
        ui_chemin.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue != null) {
                ui_photo.setImage(new Image(ui_chemin.getURI().toString()));
                ui_photo.minWidth(0);
                ui_photo.minHeight(0);
                // Compute height when parent size or padding change.
                ui_photo.fitHeightProperty().bind(Bindings.createDoubleBinding(() -> {
                    double height = ui_hbox_container.getHeight();
                    final Insets padding = ui_photo_stack.getPadding();
                    if (padding != null) {
                        height -= padding.getBottom() + padding.getTop();
                    }
                    return Math.max(0, height);
                }, ui_hbox_container.heightProperty(), ui_photo_stack.paddingProperty()));

                // Compute height when parent size or padding change.
                ui_photo.fitWidthProperty().bind(Bindings.createDoubleBinding(() -> {
                    double width = getWidth() - ui_scroll_pane.getWidth() - ui_hbox_container.getSpacing();
                    final Insets padding = ui_photo_stack.getPadding();
                    if (padding != null) {
                        width -= padding.getBottom() + padding.getTop();
                    }
                    return Math.max(0, width);
                }, widthProperty(), ui_scroll_pane.widthProperty(), ui_hbox_container.spacingProperty()));
            } else {
                ui_photo.setImage(null);
            }
        });
    }

    public FXPhotoDependancePane(final PhotoDependance photo) {
        this();
        this.elementProperty().set(photo);
    }
}
