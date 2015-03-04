
package fr.sirs.core.model;

import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Johann Sorel
 */
public class SQLQuery {
    
    private static final char SEPARATOR = '§';
    
    public final StringProperty name = new SimpleStringProperty("");
    public final StringProperty desc = new SimpleStringProperty("");
    public final StringProperty sql = new SimpleStringProperty("");

    public SQLQuery() {
    }

    public SQLQuery(String name, String desc, String sql) {
        this.name.set(name);
        this.desc.set(desc);
        this.sql.set(sql);
    }
    
    public SQLQuery(String name, String value) {
        this.name.set(name);
        final int index = value.indexOf(SEPARATOR);
        if(index<=0){
            this.sql.set(value);
            this.desc.set("");
        }else{
            this.sql.set(value.substring(0, index));
            this.desc.set(value.substring(index+1));
        }
    }
    
    public String getValueString(){
        return sql.get()+SEPARATOR+desc.get();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.name);
        hash = 71 * hash + Objects.hashCode(this.desc);
        hash = 71 * hash + Objects.hashCode(this.sql);
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
        final SQLQuery other = (SQLQuery) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.desc, other.desc)) {
            return false;
        }
        if (!Objects.equals(this.sql, other.sql)) {
            return false;
        }
        return true;
    }
    
    
}
