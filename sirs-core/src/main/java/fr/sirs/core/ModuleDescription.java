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
package fr.sirs.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.data.bean.BeanFeatureSupplier;
import org.geotoolkit.data.bean.BeanStore;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapItem;
import org.opengis.util.GenericName;

/**
 * A brief description of map layers which can be provided by the current database.
 * @author Alexis Manin (Geomatys)
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModuleDescription {

    public final SimpleStringProperty name = new SimpleStringProperty();

    public final SimpleStringProperty version = new SimpleStringProperty();

    public final SimpleStringProperty title = new SimpleStringProperty();

    public final ObservableList<Layer> layers = FXCollections.observableArrayList();

        public String getName() {
            return name.get();
        }

        public void setName(final String newName) {
            name.set(newName);
        }

        public String getVersion() {
            return version.get();
        }

        public void setVersion(final String newVersion) {
            version.set(newVersion);
        }

        public String getTitle() {
            return title.get();
        }

        public void setTitle(final String newTitle) {
            title.set(newTitle);
        }

        public ObservableList<Layer> getLayers() {
            return layers;
        }

        public void setLayers(final List<Layer> layers) {
            this.layers.setAll(layers);
        }

    /**
     * A simple container to describe a map item/layer.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Layer {

        public final SimpleStringProperty title = new SimpleStringProperty();

        public final SimpleStringProperty fieldToFilterOn = new SimpleStringProperty("@class");

        public final SimpleStringProperty filterValue = new SimpleStringProperty();

        public final ObservableList<Layer> children = FXCollections.observableArrayList();

        public String getTitle() {
            return title.get();
        }

        public String getFieldToFilterOn() {
            return fieldToFilterOn.get();
        }

        public String getFilterValue() {
            return filterValue.get();
        }

        public ObservableList<Layer> getChildren() {
            return children;
        }

        public void setChildren(final List<Layer> layers) {
            this.children.setAll(layers);
        }

        public void setTitle(final String newTitle) {
            title.set(newTitle);
        }

        public void setFieldToFilterOn(final String filterField) {
            fieldToFilterOn.set(filterField);
        }

        public void setFilterValue(final String value) {
            filterValue.set(value);
        }
    }

    public static Optional<Layer> getLayerDescription(final MapItem item) {
        final Layer currentLayer = new Layer();
        if (item.items() != null && !item.items().isEmpty()) {
            for (final MapItem child : item.items()) {
                getLayerDescription(child).ifPresent(computed -> currentLayer.children.add(computed));
            }
            if (!currentLayer.children.isEmpty()) {
                currentLayer.fieldToFilterOn.set(null);
            }
        } else if (item instanceof FeatureMapLayer) {
            final FeatureMapLayer fLayer = (FeatureMapLayer) item;
            final FeatureCollection c = fLayer.getCollection();
            FeatureStore featureStore = c.getSession().getFeatureStore();
            if (featureStore instanceof BeanStore) {
                final BeanStore tmpStore = (BeanStore) featureStore;
                final GenericName typeName = c.getFeatureType().getName();
                try {
                    BeanFeatureSupplier beanSupplier = tmpStore.getBeanSupplier(typeName);
                    currentLayer.filterValue.set(beanSupplier.getBeanClass().getCanonicalName());
                } catch (DataStoreException ex) {
                    SirsCore.LOGGER.log(Level.WARNING, "Cannot analyze bean feature type "+typeName.toString(), ex);
                }
            } else {
                try (FeatureIterator iterator = c.iterator()) {
                    if (iterator.hasNext()) {
                        Feature next = iterator.next();
                        if (next instanceof BeanFeature) {
                            Object bean = next.getUserData().get(BeanFeature.KEY_BEAN);
                            currentLayer.filterValue.set(bean.getClass().getCanonicalName());
                        }
                    }
                }
            }
        }

        if (currentLayer.children.isEmpty() && currentLayer.filterValue.get() == null) {
            return Optional.empty();
        } else {
            currentLayer.title.set(item.getName());
            return Optional.of(currentLayer);
        }
    }
}
