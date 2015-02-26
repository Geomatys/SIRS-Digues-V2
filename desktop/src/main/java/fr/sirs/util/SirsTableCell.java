
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.model.SystemeReperageBorne;
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
public class SirsTableCell<S> extends FXTableCell<S, Object> {
    
    public static final Image ICON_LINK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_LINK,16,FontAwesomeIcons.DEFAULT_COLOR),null);

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
    public void commitEdit(Object newValue) {
        super.commitEdit(newValue);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        if(empty ||item == null){
            setText(null);
            setGraphic(null);
        }
        else {
            setGraphic(new ImageView(ICON_LINK));
            if(item instanceof SystemeReperageBorne){
                final SystemeReperageBorne srb = (SystemeReperageBorne) item;
                final Session session = Injector.getBean(Session.class);
                item = session.getBorneDigueRepository().get(srb.getBorneId());
            } else{
                // On essaye de récupérer le préview label : si le résultat n'est pas nul, c'est que l'item est bien un id
                final String res = Injector.getSession().getPreviewLabelRepository().getPreview((String) item);
                if(res!=null) item = res;
                
                // Si le résultat n'était pas null, alors c'est que l'item n'était certainement pas un id, mais déjà un libellé issu de preview label.
            }
            setText(new SirsStringConverter().toString(item));
        }
    }
    
}
