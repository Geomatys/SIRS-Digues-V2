
package fr.sirs.index;

import fr.sirs.core.SirsCore;
import org.geotoolkit.gui.javafx.util.TaskManager;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;

/**
 * TODO : Improve configuration to allow nested documents (Crete, Photo, etc.) as return type. 
 * @author Johann Sorel (Geomatys)
 */
public class ElasticSearchEngine implements Closeable {
        
    // TODO : put elastic search configuration in a properties file.
    private static final HashMap<String, String> DEFAULT_CONFIGURATION = new HashMap<>();
    static {
        DEFAULT_CONFIGURATION.put("path.home", SirsCore.ELASTIC_SEARCH_PATH.toString());
    }
    
    /**
     * List of fields to embed in search hits.
     */
    private static final String[] HIT_FIELDS = new String[]{"designation", "@class", "libelle"};
    
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
        return client.prepareSearch("_river").setTypes(currentDbName).addFields(HIT_FIELDS).setQuery(query)
                .execute().actionGet();
    }
    
    /**
     * Search for documents, and return their types and Ids.
     * @param query The query to execute.
     * @return A map whose keys are types of found document and value is the list 
     * of matching documents for a given type. Never null, but can be empty.
     */
    public HashMap<String, HashSet<String> > searchByClass(final QueryBuilder query) {
        final HashMap<String, HashSet<String>> result = new HashMap<>();
        SearchResponse response = search(query);
        final SearchHits hits = response.getHits();

        final Iterator<SearchHit> ite = hits.iterator();
        while (ite.hasNext()) {
            SearchHit hit = ite.next();
            final SearchHitField fieldClass = hit.field("@class");
            if (fieldClass != null && (fieldClass.getValue() instanceof String)) {
                final String clazz = fieldClass.getValue();
                HashSet<String> ids = result.get(clazz);
                if (ids == null) {
                    ids = new HashSet<>();
                    result.put(clazz, ids);
                }
                ids.add(hit.id());
            }
        }

        return result;
    }
    
    @Override
    public void close(){
        node.close();
    }
}
