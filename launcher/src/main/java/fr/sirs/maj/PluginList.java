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
package fr.sirs.maj;

import fr.sirs.PluginInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@SuppressWarnings("serial")
public class PluginList {
    
    public final ObservableList<PluginInfo> plugins = //new SortedList(
            FXCollections.observableArrayList()/*, new PluginInfoComparator())*/;

    public PluginList() {}

    @JsonManagedReference("parent")
    public ObservableList<PluginInfo> getPlugins() {
        return this.plugins;
    }

    public void setPlugins(List<PluginInfo> plugins) {
        this.plugins.clear();
        this.plugins.addAll(plugins);
    }

    public Stream<PluginInfo> getPluginInfo(String name) {
        return plugins.stream().filter((PluginInfo p) -> {return p.getName().equalsIgnoreCase(name);});
    }
}
