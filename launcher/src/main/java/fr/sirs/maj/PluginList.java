
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
