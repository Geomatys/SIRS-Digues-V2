/**
 * This file is part of SIRS-Digues 2.
 * <p>
 * Copyright (C) 2016, FRANCE-DIGUES,
 * <p>
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */

package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Preview;
import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import org.apache.sis.util.ArgumentChecks;

import java.util.List;
import java.util.stream.Collectors;

public class CheckboxMenuButton extends MenuButton {

    public CheckboxMenuButton() {
        super();
    }

    public void setItems(final Class<? extends Element> c) {
        setItems(c, null);
    }

    public void setItems(final Class<? extends Element> c, final InvalidationListener parameterListener) {
        final Previews previews = Injector.getBean(Session.class).getPreviews();

        ReferenceCheckBox cb;
        CustomMenuItem item;
        SirsStringConverter converter = new SirsStringConverter();
        this.getItems().clear();
        for (Preview refSuiteApporter : previews.getByClass(c)) {
            cb = new ReferenceCheckBox(refSuiteApporter, converter);
            if (parameterListener != null) cb.selectedProperty().addListener(parameterListener);
            item = new CustomMenuItem(cb);
            item.setHideOnClick(false);
            this.getItems().add(item);
        }
    }

    public void addListenerToItems(final InvalidationListener parameterListener) {
        ArgumentChecks.ensureNonNull("parameterListener", parameterListener);
        for (MenuItem item : getItems()) {
            if (item instanceof CustomMenuItem) {
                final Node content = ((CustomMenuItem) item).getContent();
                if (CheckBox.class.isAssignableFrom(content.getClass())) {
                    ((CheckBox) content).selectedProperty().addListener(parameterListener);
                }
            }
        }
    }

    public void updateTextsToSirsPreferences(final SirsStringConverter converter){
        for (MenuItem item : this.getItems()) {
            final ReferenceCheckBox content = ((ReferenceCheckBox) ((CustomMenuItem) item).getContent());
            content.updateText(converter);
        }
    }

    public List<String> getCheckedItems() {
        return this.getItems().stream()
                .map(i -> ((ReferenceCheckBox) ((CustomMenuItem) i).getContent()))
                .filter(CheckBox::isSelected)
                .map(cb -> cb.preview.getElementId())
                .collect(Collectors.toList());
    }

    private static class ReferenceCheckBox extends CheckBox {
        private final Preview preview;

        protected ReferenceCheckBox(final Preview preview, final SirsStringConverter converter) {
            this.preview = preview;
            updateText(converter);
        }

        protected void updateText(final SirsStringConverter converter) {
            if (converter == null) this.setText(preview.getLibelle());
            else this.setText(converter.toString(preview));
        }
    }

}
