package fr.sirs.plugins.synchro.common;

import fr.sirs.util.SirsStringConverter;
import javafx.scene.control.ListCell;

/**
 * Cell to display label of input element.
 *
 * @author Alexis Manin (Geomatys)
 */
public class TextCell extends ListCell {

    final SirsStringConverter strConverter = new SirsStringConverter();

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || isEmpty()) {
                setText(null);
            } else {
                setText(strConverter.toString(item));
            }
        }

}
