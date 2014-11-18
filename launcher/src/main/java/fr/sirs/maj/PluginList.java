
package fr.sirs.maj;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@SuppressWarnings("serial")
public class PluginList {
    
    public ObservableList<PluginInfo> plugins = FXCollections.observableArrayList() ;
    
    public PluginList(){
        
    }
    
    @JsonManagedReference("parent")
    public List<PluginInfo> getPlugins(){
        return this.plugins;
    }

    public void setPlugins(List<PluginInfo> plugins){
        this.plugins.clear();
        this.plugins.addAll(plugins);
    }
    
    public static PluginList read(URL url) throws IOException{
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(url, PluginList.class);
    }
    
}
