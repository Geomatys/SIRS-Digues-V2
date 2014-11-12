
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import java.awt.Color;
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
        setText(null);
        setGraphic(null);
        if(empty)return;
        
        if (item != null) {
            setGraphic(new ImageView(ICON_LINK));
            if(item instanceof SystemeReperageBorne){
                final SystemeReperageBorne srb = (SystemeReperageBorne) item;
                final Session session = Injector.getBean(Session.class);
                item = session.getBorneDigueRepository().get(srb.getBorneId());
            }
            
            String text = null;
            if(item instanceof Digue){
                text = ((Digue)item).getLibelle();
            }else if(item instanceof TronconDigue){
                text = ((TronconDigue)item).getNom();
            }else if(item instanceof BorneDigue){
                text = ((BorneDigue)item).getNom();
            }else if(item instanceof SystemeReperage){
                text = ((SystemeReperage)item).getNom();
            }
            
            setText(text);
        }
    }
    
}
