
package fr.sirs.index;

import org.ektorp.CouchDbConnector;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ElasticSearchEngine {
    
    private final CouchDbConnector connector;
    private final Node node;
    private final Client client;
    
    public ElasticSearchEngine(CouchDbConnector connector, String dbname, String user, String password) {
        this.connector = connector;
        this.node = nodeBuilder().local(true).node();
        this.client = node.client();
        
        
        //link to couchdb database
        final String config = 
        "{\n" +
        "    \"type\" : \"couchdb\",\n" +
        "    \"couchdb\" : {\n" +
        "        \"host\" : \"localhost\",\n" +
        "        \"port\" : 5984,\n" +
        "        \"db\" : \""+dbname+"\",\n" +
        "        \"filter\" : null,\n" +
        "        \"user\" : \""+user+"\",\n" +
        "        \"password\" : \""+password+"\"\n" +
        "    },\n" +
        "    \"index\" : {\n" +
        "        \"index\" : \"sirs\",\n" +
        "        \"type\" : \"sirs\",\n" +
        "        \"bulk_size\" : \"100\",\n" +
        "        \"bulk_timeout\" : \"10ms\"\n" +
        "    }\n" +
        "}";
        
        client.index(Requests.indexRequest("_river").type("sirsriver").id("_meta").source(config)).actionGet();
        
    }

    public Client getClient() {
        return client;
    }
    
    public void close(){
        node.close();
    }
    
}
