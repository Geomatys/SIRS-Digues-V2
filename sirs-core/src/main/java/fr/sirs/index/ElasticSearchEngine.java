
package fr.sirs.index;

import fr.sirs.core.SirsCore;
import java.io.Closeable;
import java.util.HashMap;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.elasticsearch.river.RiverSettings;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ElasticSearchEngine implements Closeable {
    
    // TODO : put elastic search configuration in a properties file.
    private static final HashMap<String, String> DEFAULT_CONFIGURATION = new HashMap<>();
    static {
        DEFAULT_CONFIGURATION.put("path.home", SirsCore.ELASTIC_SEARCH_PATH.toString());
    }
    
    private final Node node;
    private final Client client;
    public final String indexName;
    
    public ElasticSearchEngine(final String dbHost, final int dbPort, String dbName, String user, String password) {
        
        /*
         * Elastic search configuration. We want a local index on the input 
         * couchDb database.
         */
        final String config = 
        "{\n" +
        "    \"path\" : {\n" +
        "        \"home\" : \""+DEFAULT_CONFIGURATION.toString()+"\"\n" +
        "    },\n" +
        "    \"type\" : \"couchdb\",\n" +
        "    \"couchdb\" : {\n" +
        "        \"host\" : \""+dbHost+"\",\n" +
        "        \"port\" : "+dbPort+",\n" +
        "        \"db\" : \""+dbName+"\",\n" +
        "        \"filter\" : null,\n" +
        "        \"user\" : \""+user+"\",\n" +
        "        \"password\" : \""+password+"\"\n" +
        "    },\n" +
        "    \"index\" : {\n" +
        "        \"index\" : \""+dbName+"\",\n" +
        "        \"type\" : \"*\"\n" +
        "    }\n" +
        "}";
        final ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder().loadFromSource(config);
        
        this.node = nodeBuilder().settings(ImmutableSettings.settingsBuilder().put(DEFAULT_CONFIGURATION)).local(true).node();
        this.client = node.client();
        
        // TODO : Is this sufficient to initialize river plugin ?
//        client.index(Requests.indexRequest(dbName).type(dbName).id("_meta").source(config)).actionGet();
        client.index(Requests.indexRequest("_river").type("*").id("_meta").source(config)).actionGet();
        indexName = dbName;
    }

    public Client getClient() {
        return client;
    }
    
    @Override
    public void close(){
        node.close();
    }
    
    public String getIndexName() {
        return indexName;
    }
}
