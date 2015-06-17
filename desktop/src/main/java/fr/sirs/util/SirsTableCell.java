
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.SystemeReperageBorne;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.util.FXTableCell;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @param <S>
 * @param <T>
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
        THREAD_POOL.submit(() -> {
            final String text;
            final Node graphic;
            if (empty || item == null) {
                text = null;
                graphic = null;
            } else {
                graphic = new ImageView(ICON_LINK);
                    final Session session = Injector.getBean(Session.class);
                if (item instanceof SystemeReperageBorne) {
                    final SystemeReperageBorne srb = (SystemeReperageBorne) item;
                    text = session.getRepositoryForClass(BorneDigue.class).get(srb.getBorneId()).getLibelle();
                } else if (item instanceof String) {
                    // On essaye de récupérer le preview label : si le résultat n'est pas nul, c'est que l'item est bien un id
                    final Preview tmpPreview = session.getPreviews().get((String) item);
                    if (tmpPreview != null) {
                        text = tmpPreview.getLibelle();
                    } else {
                        // Si le résultat n'était pas null, alors c'est que l'item n'était certainement pas un id, mais déjà un libellé issu de preview label.
                        text = (String) item;
                    }
                } else {
                    text = new SirsStringConverter().toString(item);
                }
            }
            
            Platform.runLater(() -> {
                super.updateItem(item, empty);
                setGraphic(graphic);
                setText(text);
            });
        });
    }
    
}
