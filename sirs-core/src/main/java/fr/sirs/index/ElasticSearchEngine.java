
package fr.sirs.index;

import fr.sirs.core.SirsCore;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Callable;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.index.IndexResponse;
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
import org.geotoolkit.gui.javafx.util.TaskManager;

/**
 * TODO : Improve configuration to allow nested documents (Crete, Photo, etc.) as return type. 
 * @author Johann Sorel (Geomatys)
 */
public class ElasticSearchEngine implements Closeable {
    
    // TODO : put elastic search configuration in a properties file.
    private static final HashMap<String, String> DEFAULT_CONFIGURATION = new HashMap<>();
    static {
        // We are forced to escape backslashes, or elastic search crash on windows platforms.
        DEFAULT_CONFIGURATION.put("path.home", SirsCore.ELASTIC_SEARCH_PATH.toString().replace("\\", "\\\\"));
        DEFAULT_CONFIGURATION.put("index.mapping.ignore_malformed", "true");
    }
    
    /**
     * List of fields to embed in search hits.
     */
    private static final String[] HIT_FIELDS = new String[]{"designation", "@class", "libelle"};
    
    private final Node node;
    private final Client client;
    public final String currentDbName;
    private final String adName = "_river";
    
    public ElasticSearchEngine(final String dbHost, final int dbPort, String dbName, String user, String password) throws IOException {
        String riverConfig = readConfig("/fr/sirs/db/riverConfig.json");
        String indexConfig = readConfig("/fr/sirs/db/indexConfig.json");
        riverConfig = riverConfig.replace("$DC", DEFAULT_CONFIGURATION.get("path.home"));
        riverConfig = riverConfig.replace("$dbHost", dbHost);
        riverConfig = riverConfig.replace("$dbPort", ""+dbPort);
        riverConfig = riverConfig.replace("$dbName", dbName);
        riverConfig = riverConfig.replace("$user", user);
        riverConfig = riverConfig.replace("$password", password);
        final String cstConfig = riverConfig;

        this.node = nodeBuilder().settings(ImmutableSettings.settingsBuilder().put(DEFAULT_CONFIGURATION)).local(true).node();
        this.client = node.client();        
        currentDbName = dbName;
        
        TaskManager.INSTANCE.submit("Mise à jour des index", new Callable<IndexResponse>() {

            public IndexResponse call() {
                IndicesExistsResponse res = client.admin().indices().exists(Requests.indicesExistsRequest(adName)).actionGet();
                if(res.isExists()){
                    client.admin().indices().close(Requests.closeIndexRequest(adName)).actionGet();
                    client.admin().indices().delete(Requests.deleteIndexRequest(adName)).actionGet();
                }
                client.admin().indices().create(Requests.createIndexRequest(adName).settings(indexConfig)).actionGet();
                client.index(Requests.indexRequest(adName).type(dbName).id("_meta").source(cstConfig)).actionGet();
                return null;
            }
        });
    }

    /**
     * Perform a search on the current database index, using given query.
     * @param query The query to execute.
     * @return ElasticSearch response, never null.
     */
    public SearchResponse search(final QueryBuilder query) {
        return client.prepareSearch(adName)
                .setTypes(currentDbName)
                .addFields(HIT_FIELDS)
                .setQuery(query)
                .setSize(Integer.MAX_VALUE)
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

    private static String readConfig(String path) throws IOException {
        final InputStream is = ElasticSearchEngine.class.getResourceAsStream(path);
        final StringBuilder sb = new StringBuilder();
        final BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        try{
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }finally{
            br.close();
        }
        return sb.toString();
    }

}
