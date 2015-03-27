
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.model.SystemeReperageBorne;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.util.FXTableCell;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class SirsTableCell<S, T> extends FXTableCell<S, T> {
    
    public static final Image ICON_LINK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_LINK,16,FontAwesomeIcons.DEFAULT_COLOR),null);
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    public SirsTableCell() {
        setAlignment(Pos.CENTER);
        setContentDisplay(ContentDisplay.LEFT);
        setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    public void terminateEdit() {
    }

    @Override
    public void startEdit() {
        super.startEdit();
    }

    @Override
    public void commitEdit(T newValue) {
        super.commitEdit(newValue);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
    }

    @Override
    protected void updateItem(final T item, final boolean empty) {
        super.updateItem(item, empty);
        if(empty ||item == null){
            setText(null);
            setGraphic(null);
        }
        else {
            setGraphic(new ImageView(ICON_LINK));
            THREAD_POOL.submit(() -> {
                final String toDisplay;
                if (item instanceof SystemeReperageBorne) {
                    final SystemeReperageBorne srb = (SystemeReperageBorne) item;
                    final Session session = Injector.getBean(Session.class);
                    toDisplay = session.getBorneDigueRepository().get(srb.getBorneId()).getLibelle();
                } else if (item instanceof String) {
                    // On essaye de récupérer le preview label : si le résultat n'est pas nul, c'est que l'item est bien un id
                    final String tmpPreview = Injector.getSession().getPreviewLabelRepository().getPreview((String) item);
                    if (tmpPreview != null) {
                        toDisplay = tmpPreview;
                    } else {
                        // Si le résultat n'était pas null, alors c'est que l'item n'était certainement pas un id, mais déjà un libellé issu de preview label.
                        toDisplay = (String) item;
                    }
                } else {
                    toDisplay = new SirsStringConverter().toString(item);
                }
                Platform.runLater(() -> setText(toDisplay));
            });
        }
    }
    
}
