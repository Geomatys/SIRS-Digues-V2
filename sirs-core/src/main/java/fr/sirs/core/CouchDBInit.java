
package fr.sirs.core;

import fr.sirs.core.component.DocumentChangeEmiter;
import fr.sirs.index.ElasticSearchEngine;
import fr.sirs.index.SearchEngine;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.geotoolkit.internal.io.IOUtilities;
import org.geotoolkit.internal.io.Installation;
import org.geotoolkit.referencing.operation.transform.NTv2Transform;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Utilitaire de creation de la connection a la base CouchDB.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class CouchDBInit {
    
    public static final String DB_CONNECTOR = "connector";
    public static final String SEARCH_ENGINE = "searchEngine";
    public static final String ELASTIC_ENGINE = "elasticEngine";
    public static final String CHANGE_EMITTER = "docChangeEmitter";
    
    static {
        final File directory = Installation.NTv2.directory(true);
        if (!new File(directory, NTv2Transform.RGF93).isFile()) {
            directory.mkdirs();
            final File out = new File(directory, NTv2Transform.RGF93);
            try {
                out.createNewFile();
                IOUtilities.copy(CouchDBInit.class.getResourceAsStream("/fr/sirs/ntv2/ntf_r93.gsb"), new FileOutputStream(out));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    public static ClassPathXmlApplicationContext create(String databaseUrl, String databaseName, 
            String configFile, boolean createIfNotExists, boolean setupListener) throws MalformedURLException, IOException {
        
        //http://user:password@address.com
        final String userpass = databaseUrl.split("@")[0].substring(7);
        final String user = userpass.split(":")[0];
        final String password = userpass.split(":")[1];
        
        final HttpClient httpClient = new StdHttpClient.Builder().url(databaseUrl).build();
        final CouchDbInstance couchsb = new StdCouchDbInstance(httpClient);
        final CouchDbConnector connector = couchsb.createConnector(databaseName,createIfNotExists);
        
        DocumentChangeEmiter changeEmmiter = null;
        SearchEngine searchEngine = null;
        ElasticSearchEngine elasticEngine = null;
        if(setupListener){
            changeEmmiter = new DocumentChangeEmiter(connector);
            searchEngine = new SearchEngine(databaseName, connector, changeEmmiter);
            elasticEngine = new ElasticSearchEngine(connector, databaseName, user, password);
            changeEmmiter.start();
        }
        
        final ClassPathXmlApplicationContext applicationContextParent = new ClassPathXmlApplicationContext();
        applicationContextParent.refresh();
        applicationContextParent.getBeanFactory().registerSingleton(DB_CONNECTOR, connector);
        if(setupListener){
            applicationContextParent.getBeanFactory().registerSingleton(SEARCH_ENGINE, searchEngine);
            applicationContextParent.getBeanFactory().registerSingleton(ELASTIC_ENGINE, elasticEngine);
            applicationContextParent.getBeanFactory().registerSingleton(CHANGE_EMITTER, changeEmmiter);
        }

        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext( new String[]{
            configFile}, applicationContextParent);

        return applicationContext;
    }
    
}
