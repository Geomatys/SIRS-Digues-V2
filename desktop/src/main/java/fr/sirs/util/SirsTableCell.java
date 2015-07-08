
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.theme.ui.PojoTable.PropertyColumn;
import fr.sirs.util.property.Reference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.util.FXTableCell;

/**
 * Cellule pour les liens dans la pojo table.
 * 
 * @author Johann Sorel (Geomatys)
 * @param <S>
 * @param <T>
 */
public class SirsTableCell<S, T> extends FXTableCell<S, T> {
    
    public static final Image ICON_LINK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_LINK,16,FontAwesomeIcons.DEFAULT_COLOR),null);
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    private final ChoiceBox<Preview> links = new ChoiceBox();
    private final ChangeListener linkChange = new ChangeListener() {

        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            if(isEditing()) terminateEdit();
        }
    };

    public SirsTableCell() {
        setAlignment(Pos.CENTER);
        setContentDisplay(ContentDisplay.LEFT);
        setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    public void terminateEdit() {
        final Preview preview = links.getValue();
        links.valueProperty().removeListener(linkChange);
        if(preview==null){
            commitEdit(null);
        }else{
            commitEdit((T) preview.getElementId());
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        final PropertyColumn col = (PropertyColumn) getTableColumn();
        links.setItems(col.getReferencesList());
        links.setConverter(new SirsStringConverter());

        for(Preview p : col.getReferencesList()){
            if(p.getElementId().equals(getItem())){
                links.valueProperty().set(p);
                break;
            }
        }
        links.valueProperty().addListener(linkChange);

        setText(null);
        setGraphic(links);
    }

    @Override
    public void commitEdit(T newValue) {
        links.valueProperty().removeListener(linkChange);
        super.commitEdit(newValue);
    }

    @Override
    public void cancelEdit() {
        links.valueProperty().removeListener(linkChange);
        super.cancelEdit();
    }

    @Override
    protected void updateItem(final T item, final boolean empty) {
        super.updateItem(item, empty);

        setGraphic(null);
        setText(null);

        if(item!=null){
            setGraphic(new ImageView(ICON_LINK));

            THREAD_POOL.submit(() -> {
                final String text;
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

                Platform.runLater(() -> {
                    if(getGraphic()!=links)setText(text);
                });
            });
        }
    }
    
}
