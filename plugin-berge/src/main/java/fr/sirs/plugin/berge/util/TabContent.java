
package fr.sirs.plugin.berge.util;

/**
 *
 * @author guilhem
 */
public class TabContent {
    
    public String tabName;
    public String tableName;
    public Class tableClass;
    
    public TabContent(String tabName, String tableName, Class tableClass) {
        this.tabName    = tabName;
        this.tableName  = tableName;
        this.tableClass = tableClass;
    }
}
