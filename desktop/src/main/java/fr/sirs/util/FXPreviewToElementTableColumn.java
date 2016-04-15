/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.Preview;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;

/**
 *
 * A table column for quick access to an element from its corresponding validitySummary.
 * 
 * @author Samuel Andrés (Geomatys)
 */
public class FXPreviewToElementTableColumn  extends TableColumn<Preview, Preview> {

    public FXPreviewToElementTableColumn() {
        super("Détail");
        setEditable(false);
        setSortable(false);
        setResizable(true);
        setPrefWidth(70);

        setCellValueFactory((TableColumn.CellDataFeatures<Preview, Preview> param) -> {
            return new SimpleObjectProperty<>(param.getValue());
        });

        setCellFactory((TableColumn<Preview, Preview> param) -> {
            return new FXPreviewToElementButtonTableCell();
        });
    }

    private class FXPreviewToElementButtonTableCell extends ButtonTableCell<Preview, Preview> {

        public FXPreviewToElementButtonTableCell() {
            super(false, null, 
                    (Preview t) -> true, 
                    (Preview t) -> {
                        Injector.getSession().showEditionTab(t);
                        return t;
                    });
        }

        @Override
        protected void updateItem(Preview item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                button.setGraphic(new ImageView(SIRS.ICON_EYE_BLACK));
            }
        }
    }
}
