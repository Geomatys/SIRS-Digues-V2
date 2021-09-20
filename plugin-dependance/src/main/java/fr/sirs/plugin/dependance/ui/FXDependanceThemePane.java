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
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.AmenagementHydraulique;
import fr.sirs.core.model.DescriptionAmenagementHydraulique;
import fr.sirs.core.model.Element;
import fr.sirs.theme.AbstractTheme;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.SimpleFXEditMode;
import fr.sirs.util.SirsStringConverter;
import java.util.List;
import java.util.logging.Level;
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

    private final StringProperty ahIdProperty = new SimpleStringProperty();
    private final Session session = Injector.getBean(Session.class);

    public StringProperty ahIdProperty(){return ahIdProperty;}

    public FXDependanceThemePane(ComboBox<AmenagementHydraulique> uiAhChoice, AbstractTheme.ThemeManager theme) {
        setCenter(createContent(theme));
        final List<AmenagementHydraulique> ahPreviews = session.getRepositoryForClass(AmenagementHydraulique.class).getAll();
        uiAhChoice.setItems(FXCollections.observableList(ahPreviews));
        uiAhChoice.setConverter(new SirsStringConverter());

        uiAhChoice.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends AmenagementHydraulique> observable, AmenagementHydraulique oldValue, AmenagementHydraulique newValue) -> {
            ahIdProperty.set(newValue.getId());
        });

        if(!ahPreviews.isEmpty()){
            uiAhChoice.getSelectionModel().select(ahPreviews.get(0));
        }
    }

    protected class DependanceThemePojoTable<T extends DescriptionAmenagementHydraulique> extends PojoTable{

        protected final StringProperty ahIdProperty = new SimpleStringProperty();
        private final AbstractTheme.ThemeManager<T> group;

        public void setAhIdProperty(final String ahId) {
            ahIdProperty.set(ahId);
        }

        public String getAhIdProperty() {
            return ahIdProperty.get();
        }

        public StringProperty ahIdProperty() {
            return ahIdProperty;
        }

        public DependanceThemePojoTable(AbstractTheme.ThemeManager<T> group, final ObjectProperty<? extends Element> container) {
            super(group.getDataClass(), group.getTableTitle(), container);
            ahIdProperty.addListener(this::updateTable);
            this.group = group;

            // Réécrire le copy des éléments, demander au client ce qu'il attend
            // this.elementCopier = new ElementCopier(this.pojoClass, container, this.session, this.repo, TronconDigue.class);
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

        @Override
        protected T createPojo() {
            T created = null;
            if (repo != null) {
                created = (T) repo.create();
            } else if (pojoClass != null) {
                try {
                    created = (T) session.getElementCreator().createElement(pojoClass);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (created != null) {
                created.setAmenagementHydrauliqueId(getAhIdProperty());
                
                try {
                    repo.add(created);
                } catch (NullPointerException e) {
                    SIRS.LOGGER.log(Level.WARNING, "Repository introuvable", e);
                }
                getAllValues().add(created);
            }
            return created;
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
        table.ahIdProperty().bindBidirectional(ahIdProperty);

        return new BorderPane(table, topPane, null, null, null);
    }
}
