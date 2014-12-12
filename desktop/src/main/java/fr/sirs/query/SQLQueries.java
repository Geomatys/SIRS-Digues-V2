
package fr.sirs.query;

import fr.sirs.core.SirsCore;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

/**
 *
 * @author Johann Sorel
 */
public class SQLQueries {
    
    public static List<SQLQuery> getQueries() throws FileNotFoundException, IOException{
        final File queryFile = new File(SirsCore.getConfigFolder(),"queries.properties");
        final Properties props = new Properties();
        if(queryFile.exists()){
            try (FileInputStream in = new FileInputStream(queryFile)) {
                props.load(in);
            }
        }
        
        final List<SQLQuery> queries = new ArrayList<>();
        for(Entry entry : props.entrySet()){
            queries.add(new SQLQuery((String)entry.getKey(), (String)entry.getValue()));
        }
        
        return queries;
    }
    
    public static void saveQueries(List<SQLQuery> queries) throws FileNotFoundException, IOException{
        final File queryFile = new File(SirsCore.getConfigFolder(),"queries.properties");
        final Properties props = new Properties();
        for(SQLQuery query : queries){
            props.put(query.name.get(), query.getValueString());
        }
        try (FileOutputStream out = new FileOutputStream(queryFile)) {
            props.store(out, "");
        }
    }
    
    
}
