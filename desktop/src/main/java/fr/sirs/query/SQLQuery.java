
package fr.sirs.query;

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
    
}
