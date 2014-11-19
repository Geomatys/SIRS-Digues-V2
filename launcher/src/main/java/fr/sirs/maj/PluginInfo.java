
package fr.sirs.maj;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Description d'un plugin.
 * 
 * @author Johann Sorel (Geomatys)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@SuppressWarnings("serial")
public class PluginInfo {
        
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty versionMajor = new SimpleIntegerProperty(1);
    private final IntegerProperty versionMinor = new SimpleIntegerProperty(0);

    public PluginInfo() {
    }
    
    public StringProperty nameProperty() {
       return name;
    }
    
    public String getName(){
        return this.name.get();
    }
    
    public void setName(String name){
        this.name.set(name);
    }  
    
    public StringProperty descriptionProperty() {
       return description;
    }
    
    public String getDescription(){
        return this.description.get();
    }
    
    public void setDescription(String desc){
        this.description.set(desc);
    }  
    
    public IntegerProperty versionMajorProperty() {
       return versionMajor;
    }
    
    public int getVersionMajor(){
        return this.versionMajor.get();
    }
    
    public void setVersionMajor(int version){
        this.versionMajor.set(version);
    }  
    
    public IntegerProperty versionMinorProperty() {
       return versionMinor;
    }
    
    public int getVersionMinor(){
        return this.versionMinor.get();
    }
    
    public void setVersionMinor(int version){
        this.versionMinor.set(version);
    }  
    
    @JsonIgnore
    public boolean isOlderOrSame(PluginInfo info){
        return getVersionMajor() < info.getVersionMajor() ||                        
                (getVersionMajor() == info.getVersionMajor() &&
                 getVersionMinor() < info.getVersionMinor());
    }
    
    @JsonIgnore
    public URL bundleURL(URL serverURL) throws MalformedURLException{
        return new URL(serverURL.toString() +"/"+name+"_"+getVersionMajor()+"-"+getVersionMinor()+".zip");
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PluginInfo other = (PluginInfo) obj;
        return Objects.equals(this.name, other.name);
    }
    
}
