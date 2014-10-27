
package fr.symadrem.sirs.core;

import java.net.MalformedURLException;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Utilitaire de creation de la connection a la base CouchDB.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class CouchDBInit {
    
    public static final String DB_CONNECTOR = "connector";
    
    public static ClassPathXmlApplicationContext create(String databaseUrl, String databaseName, String configFile) throws MalformedURLException {
        
        final HttpClient httpClient = new StdHttpClient.Builder().url(databaseUrl).build();
        final CouchDbInstance couchsb = new StdCouchDbInstance(httpClient);
        final CouchDbConnector connector = couchsb.createConnector(databaseName,false);
        
        final ClassPathXmlApplicationContext applicationContextParent = new ClassPathXmlApplicationContext();
        applicationContextParent.refresh();
        applicationContextParent.getBeanFactory().registerSingleton(DB_CONNECTOR, connector);

        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext( new String[]{
            configFile}, applicationContextParent);

        return applicationContext;
    }
    
}
