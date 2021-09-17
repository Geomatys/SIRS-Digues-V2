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
package fr.sirs.plugin.dependance.ui;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.AbstractTheme;
import fr.sirs.theme.TronconTheme;
import fr.sirs.theme.ui.ForeignParentPojoTable;
import fr.sirs.theme.ui.pojotable.ElementCopier;
import fr.sirs.util.SimpleFXEditMode;
import fr.sirs.util.SirsStringConverter;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Maxime Gavens (Geomatys)
 */
public class FXDependanceThemePane extends BorderPane {

    private final StringProperty linearIdProperty = new SimpleStringProperty();
    private final Session session = Injector.getBean(Session.class);

    public StringProperty linearIdProperty(){return linearIdProperty;}

    public FXDependanceThemePane(ComboBox<TronconDigue> uiLinearChoice, TronconTheme.ThemeManager theme) {
        setCenter(createContent(theme));
        final List<TronconDigue> linearPreviews = session.getRepositoryForClass(TronconDigue.class).getAll();
        uiLinearChoice.setItems(FXCollections.observableList(linearPreviews));
        uiLinearChoice.setConverter(new SirsStringConverter());

        uiLinearChoice.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) -> {
            linearIdProperty.set(newValue.getId());
        });

        if(!linearPreviews.isEmpty()){
            uiLinearChoice.getSelectionModel().select(linearPreviews.get(0));
        }
    }

    protected class DependanceThemePojoTable<T extends AvecForeignParent> extends ForeignParentPojoTable<T>{

        private final TronconTheme.ThemeManager<T> group;

        public DependanceThemePojoTable(TronconTheme.ThemeManager<T> group, final ObjectProperty<? extends Element> container) {
            super(group.getDataClass(), group.getTableTitle(), container);
            foreignParentIdProperty.addListener(this::updateTable);
            this.group = group;
            this.elementCopier = new ElementCopier(this.pojoClass, container, this.session, this.repo, TronconDigue.class);
        }

        private void updateTable(ObservableValue<? extends String> observable, String oldValue, String newValue){
            if(newValue==null || group==null) {
                setTableItems(FXCollections::emptyObservableList);
            } else {
                setTableItems(() -> (ObservableList) group.getExtractor().apply(newValue));
            }
        }

        public BooleanProperty getEditableProperty() {
            return editableProperty;
        }
    }

    protected Parent createContent(AbstractTheme.ThemeManager manager) {
        final Separator separator = new Separator();
        separator.setVisible(false);
        final SimpleFXEditMode editMode = new SimpleFXEditMode();
        final HBox topPane = new HBox(separator, editMode);
        HBox.setHgrow(separator, Priority.ALWAYS);

        final DependanceThemePojoTable table = new DependanceThemePojoTable(manager, (ObjectProperty<? extends Element>) null);
        table.setDeletor(manager.getDeletor());
        table.getEditableProperty().bind(editMode.editionState());
        table.foreignParentProperty().bindBidirectional(linearIdProperty);

        return new BorderPane(table, topPane, null, null, null);
    }
}
