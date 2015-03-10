
package fr.sirs.index;

import fr.sirs.core.SirsCore;
import org.geotoolkit.gui.javafx.util.TaskManager;
import java.io.Closeable;
import java.util.HashMap;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

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
    public final String currentDbName;
    
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
        "        \"index\" : \"_river\",\n" +
        "        \"type\" : \""+dbName+"\"\n" +
        "    }\n" +
        "}";        
        this.node = nodeBuilder().settings(ImmutableSettings.settingsBuilder().put(DEFAULT_CONFIGURATION)).local(true).node();
        this.client = node.client();        
        currentDbName = dbName;
        
        TaskManager.INSTANCE.submit("Mise à jour des index", () -> client.index(
                Requests.indexRequest("_river").type(dbName).id("_meta").source(config)).actionGet());
    }

    /**
     * Perform a search on the current database index, using given query.
     * @param query The query to execute.
     * @return ElasticSearch response, never null.
     */
    public SearchResponse search(final QueryBuilder query) {
        return client.prepareSearch("_river").setTypes(currentDbName).setQuery(query)
                .execute().actionGet();
    }
    
    @Override
    public void close(){
        node.close();
    }
}
